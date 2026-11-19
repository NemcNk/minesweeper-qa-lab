package core;

import model.CellContent;
import model.CellSnapshot;
import model.CellState;
import model.Coord;
import model.GameConfig;
import model.GameState;

import java.util.ArrayDeque;
import java.util.Random;

/**
 * Minesweeper rules and game state. No UI dependencies.
 *
 * <p>Lifecycle: the game starts in {@link GameState#PLAYING} with no bombs
 * placed. The first accepted left click places the bombs, then the game ends
 * in {@link GameState#WON}, {@link GameState#LOST_BOMB} or
 * {@link GameState#TIMEOUT}. {@link #start()} begins a new game.
 */
public final class Game implements BoardReader {

    private final BoardGeometry geometry;
    private final BombMap bombMap;
    private final BoardState boardState;

    private GameState state;

    /**
     * Creates a game with random bomb placement.
     *
     * @throws IllegalArgumentException if the bombs do not fit outside
     *                                  a 3x3 safe area (or the whole board,
     *                                  if it is smaller)
     */
    public Game(GameConfig config) {
        this(
                requireRoomForSafeArea(config),
                new RandomBombPlacement(new Random())
        );
    }

    // Package-private constructor for tests: the caller chooses the bomb layout
    Game(GameConfig config, BombPlacement placement) {

        geometry = new BoardGeometry(
                config.getColumns(),
                config.getRows()
        );

        bombMap = new BombMap(
                geometry,
                config.getTotalBombs(),
                placement
        );

        boardState = new BoardState(
                geometry,
                config.getTotalBombs()
        );

        start();
    }

    private static GameConfig requireRoomForSafeArea(GameConfig config) {

        long cells = (long) config.getColumns() * config.getRows();
        long safeArea = Math.min(9L, cells);

        if (config.getTotalBombs() > cells - safeArea) {
            throw new IllegalArgumentException(
                    "Invalid number of bombs"
            );
        }

        return config;
    }

    /**
     * Starts a new game: clears the board and forgets the bomb layout.
     */
    public void start() {

        bombMap.reset();
        boardState.start();

        state = GameState.PLAYING;
    }

    public GameState getState() {
        return state;
    }

    @Override
    public int getColumns() {
        return geometry.getColumns();
    }

    @Override
    public int getRows() {
        return geometry.getRows();
    }

    /**
     * @return {@code true} once the first click has placed the bombs
     */
    public boolean isStarted() {
        return bombMap.isGenerated();
    }

    /**
     * @return bombs minus flags placed; may be zero but never negative,
     *         because flags are limited by the bomb count
     */
    public int getRemainingBombs() {

        return bombMap.getTotalBombs()
                - boardState.getFlaggedCount();
    }

    /**
     * Returns a snapshot of a cell.
     *
     * <p>Before the first click every cell has content
     * {@link CellContent#ZERO}, because bombs are not placed yet. After the
     * start the snapshot carries the real content even for closed cells, so
     * a renderer must not show the content of cells that are not opened.
     *
     * @throws IndexOutOfBoundsException if {@code coord} is {@code null}
     *                                   or outside the board
     */
    @Override
    public CellSnapshot getCell(Coord coord) {

        if (!geometry.inRange(coord)) {
            throw new IndexOutOfBoundsException(
                    "Coordinate outside board"
            );
        }

        CellContent content = CellContent.ZERO;

        if (isStarted()) {
            content = bombMap.get(coord);
        }

        return new CellSnapshot(
                content,
                boardState.get(coord)
        );
    }

    /**
     * Handles a left click.
     *
     * <p>The first accepted click places the bombs. Behaviour by cell state:
     * <ul>
     *   <li>closed: the cell is opened; an empty cell also opens the whole
     *       connected empty area and its border; a bomb ends the game with
     *       {@link GameState#LOST_BOMB};</li>
     *   <li>opened number ("chord"): if the number of flags around equals the
     *       number, all closed neighbours are opened (a wrongly placed flag
     *       can therefore lose the game);</li>
     *   <li>flagged: ignored.</li>
     * </ul>
     * Opening the last safe cell sets {@link GameState#WON}.
     *
     * <p>The call is silently ignored if the game is not in
     * {@link GameState#PLAYING}, if {@code coord} is {@code null} or outside
     * the board, or if the game has not started and the cell is not closed.
     */
    public void pressLeftButton(Coord coord) {

        if (state != GameState.PLAYING
                || !geometry.inRange(coord)) {
            return;
        }

        if (!isStarted()) {

            if (boardState.get(coord) != CellState.CLOSED) {
                return;
            }

            bombMap.start(coord);
        }

        openBox(coord);

        checkWinner();
    }

    /**
     * Toggles a flag on a closed or flagged cell. A new flag is not placed
     * once the number of flags equals the number of bombs. Works before the
     * first left click.
     *
     * <p>Silently ignored if the game is not in {@link GameState#PLAYING},
     * or if {@code coord} is {@code null} or outside the board.
     */
    public void pressRightButton(Coord coord) {

        if (state != GameState.PLAYING
                || !geometry.inRange(coord)) {
            return;
        }

        boardState.toggleFlag(coord);
    }

    /**
     * Ends a started game as a loss by time and reveals the board.
     * Ignored if the game is not in progress or has not started.
     */
    public void timeout() {

        if (state != GameState.PLAYING || !isStarted()) {
            return;
        }

        state = GameState.TIMEOUT;

        revealBoard(null);
    }

    private void openBox(Coord coord) {

        switch (boardState.get(coord)) {

            case OPENED:
                openClosedBoxesAroundNumber(coord);
                return;

            case CLOSED:

                CellContent content = bombMap.get(coord);

                if (content == CellContent.BOMB) {
                    openBombs(coord);
                    return;
                }

                if (content == CellContent.ZERO) {
                    openEmptyArea(coord);
                } else {
                    boardState.setOpened(coord);
                }

                return;

            default:
                return;
        }
    }

    private void openEmptyArea(Coord start) {

        ArrayDeque<Coord> queue = new ArrayDeque<>();

        queue.addLast(start);

        while (!queue.isEmpty()
                && state == GameState.PLAYING) {

            Coord coord = queue.removeFirst();

            if (boardState.get(coord) != CellState.CLOSED) {
                continue;
            }

            CellContent content = bombMap.get(coord);

            boardState.setOpened(coord);

            if (content == CellContent.ZERO) {

                for (Coord around :
                        geometry.getCoordsAround(coord)) {

                    if (boardState.get(around) == CellState.CLOSED) {
                        queue.addLast(around);
                    }
                }
            }
        }
    }

    private void openClosedBoxesAroundNumber(Coord coord) {

        CellContent content = bombMap.get(coord);

        if (!content.isNumber()
                || content == CellContent.ZERO) {
            return;
        }

        int flaggedCount = boardState.getFlaggedBoxesAround(coord);

        if (flaggedCount != content.getNumber()) {
            return;
        }

        for (Coord around : geometry.getCoordsAround(coord)) {

            if (boardState.get(around) == CellState.CLOSED) {

                openBox(around);

                if (state != GameState.PLAYING) {
                    return;
                }
            }
        }
    }

    // Loss caused by opening a bomb
    private void openBombs(Coord bombedCoord) {

        state = GameState.LOST_BOMB;

        revealBoard(bombedCoord);
    }

    // Reveals the whole board after a loss
    private void revealBoard(Coord bombedCoord) {

        for (Coord coord : geometry.getAllCoords()) {

            if (coord.equals(bombedCoord)) {
                boardState.setExploded(coord);
                continue;
            }

            if (bombMap.get(coord) == CellContent.BOMB) {
                boardState.openBomb(coord);
            } else {
                boardState.markWrongFlag(coord);
            }
        }
    }

    private void checkWinner() {

        int safeCells =
                getColumns() * getRows()
                        - bombMap.getTotalBombs();

        if (state == GameState.PLAYING
                && boardState.getOpenedSafeCount() == safeCells) {

            state = GameState.WON;
        }
    }
}


