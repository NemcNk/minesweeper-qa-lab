package core;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Random;

import model.CellContent;
import model.CellSnapshot;
import model.CellState;
import model.Coord;
import model.GameConfig;
import model.GameState;

/**
 * Implements Minesweeper game rules and manages the game state.
 *
 * <p>The game starts in the PLAYING state without generated bombs.
 * The first accepted left click generates the bomb layout.
 * The game ends when the player wins, hits a bomb, or runs out of time.
 *
 * <p>Key events (start, flags, end) are reported to a
 * {@link GameEventListener}.
 */
public final class Game implements BoardReader {

    private final BoardGeometry geometry;
    private final BombMap bombMap;
    private final BoardState boardState;
    private final GameEventListener listener;

    private GameState state;

    /**
     * Creates a new game with randomly generated bomb placement.
     *
     * @param config game configuration
     * @throws IllegalArgumentException if bombs do not fit outside
     *                                  the initial safe area
     */
    public Game(GameConfig config) {
        this(config, GameEventListener.NONE);
    }

    /**
     * Creates a new game with randomly generated bomb placement
     * that reports its events to a listener.
     *
     * @param config game configuration
     * @param listener receiver of game events
     * @throws IllegalArgumentException if bombs do not fit outside
     *                                  the initial safe area
     */
    public Game(GameConfig config, GameEventListener listener) {
        this(
                requireRoomForSafeArea(config),
                new RandomBombPlacement(new Random()),
                listener
        );
    }

    /**
     * Creates a game with a custom bomb placement strategy.
     *
     * <p>This constructor is package-private and intended for tests.
     *
     * @param config game configuration
     * @param placement bomb placement strategy
     */
    Game(GameConfig config, BombPlacement placement) {
        this(config, placement, GameEventListener.NONE);
    }

    /**
     * Creates a game with a custom bomb placement strategy
     * and an event listener.
     *
     * <p>This constructor is package-private and intended for tests.
     *
     * @param config game configuration
     * @param placement bomb placement strategy
     * @param listener receiver of game events
     */
    Game(
            GameConfig config,
            BombPlacement placement,
            GameEventListener listener
    ) {
        this.listener = Objects.requireNonNull(listener);

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

    /**
     * Validates that enough cells are available for the initial safe area.
     *
     * <p>The safe area contains up to nine cells. On smaller boards,
     * the entire board is treated as the safe area.
     *
     * @param config game configuration
     * @return validated configuration
     * @throws IllegalArgumentException if too many bombs are configured
     */
    private static GameConfig requireRoomForSafeArea(
            GameConfig config
    ) {
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
     * Starts a new game and resets the board.
     *
     * <p>The previous bomb layout is discarded and will be generated
     * again after the first accepted left click.
     */
    public void start() {
        bombMap.reset();
        boardState.start();

        state = GameState.PLAYING;
    }

    /**
     * Returns the current game state.
     *
     * @return current game state
     */
    public GameState getState() {
        return state;
    }

    /**
     * Returns the number of board columns.
     *
     * @return number of columns
     */
    @Override
    public int getColumns() {
        return geometry.getColumns();
    }

    /**
     * Returns the number of board rows.
     *
     * @return number of rows
     */
    @Override
    public int getRows() {
        return geometry.getRows();
    }

    /**
     * Checks whether the bomb layout has already been generated.
     *
     * @return true if bombs have been placed
     */
    public boolean isStarted() {
        return bombMap.isGenerated();
    }

    /**
     * Returns the number of bombs that have not been flagged yet.
     *
     * @return remaining bomb count
     */
    public int getRemainingBombs() {
        return bombMap.getTotalBombs()
                - boardState.getFlaggedCount();
    }

    /**
     * Returns a snapshot of the specified cell.
     *
     * <p>Before the first click, every cell contains ZERO because
     * the bomb layout has not been generated yet.
     *
     * <p>After generation, snapshots contain the actual cell content,
     * including for closed cells. The renderer must avoid displaying
     * hidden content.
     *
     * @param coord cell coordinates
     * @return immutable cell snapshot
     * @throws IndexOutOfBoundsException if coordinates are null
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
     * Handles a left mouse button click.
     *
     * <p>The first accepted click generates bombs while preserving
     * the initial safe area.
     *
     * <p>Clicking a closed cell opens it. Empty cells trigger
     * flood-fill expansion. Clicking an opened number performs
     * a chord operation when the surrounding flag count matches
     * the number.
     *
     * <p>Flagged cells cannot be opened.
     *
     * @param coord clicked cell coordinates
     */
    public void pressLeftButton(Coord coord) {
        if (state != GameState.PLAYING
                || !geometry.inRange(coord)) {
            return;
        }

        // Generate the bomb layout on the first accepted click.
        if (!isStarted()) {
            if (boardState.get(coord) != CellState.CLOSED) {
                return;
            }

            bombMap.start(coord);
            listener.gameStarted(coord);
        }

        openBox(coord);
        // Check whether all safe cells have been opened.
        checkWinner();

        // The guard at the top guarantees the game was PLAYING on entry,
        // so a changed state means this click ended the game.
        if (state != GameState.PLAYING) {
            listener.gameFinished(state);
        }
    }

    /**
     * Handles a right mouse button click.
     *
     * <p>Toggles a flag on a closed or flagged cell.
     * The number of flags cannot exceed the total bomb count.
     *
     * @param coord clicked cell coordinates
     */
    public void pressRightButton(Coord coord) {
        if (state != GameState.PLAYING
                || !geometry.inRange(coord)) {
            return;
        }

        CellState before = boardState.get(coord);

        boardState.toggleFlag(coord);

        CellState after = boardState.get(coord);

        // Report only real changes: opened cells and the flag limit
        // leave the cell as it was.
        if (before != after) {
            listener.flagToggled(coord, after == CellState.FLAGGED);
        }
    }

    /**
     * Ends the game because the time limit has expired.
     *
     * <p>The board is revealed only if bomb placement has already
     * occurred and the game is still in progress.
     */
    public void timeout() {
        if (state != GameState.PLAYING || !isStarted()) {
            return;
        }

        state = GameState.TIMEOUT;

        revealBoard(null);

        listener.gameFinished(state);
    }

    /**
     * Opens a cell according to its current state.
     *
     * @param coord cell coordinates
     */
    private void openBox(Coord coord) {
        switch (boardState.get(coord)) {
            case OPENED -> openClosedBoxesAroundNumber(coord);
            case CLOSED -> openClosedBox(coord);
            default -> {
                // Flagged and revealed cells cannot be opened.
            }
        }
    }

    /**
     * Opens a closed cell or handles a bomb or empty cell.
     *
     * @param coord cell coordinates
     */
    private void openClosedBox(Coord coord) {
        CellContent content = bombMap.get(coord);

        if (content == CellContent.BOMB) {
            openBombs(coord);
        } else if (content == CellContent.ZERO) {
            openEmptyArea(coord);
        } else {
            boardState.setOpened(coord);
        }
    }

    /**
     * Opens a connected empty area and its numbered border.
     *
     * <p>Uses breadth-first search to avoid recursive calls
     * and potential stack overflow on large boards.
     *
     * @param start starting cell coordinates
     */
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

            // Expand only from empty cells.
            if (content == CellContent.ZERO) {
                for (Coord around : geometry.getCoordsAround(coord)) {
                    if (boardState.get(around) == CellState.CLOSED) {
                        queue.addLast(around);
                    }
                }
            }
        }
    }

    /**
     * Opens all closed neighbours of an opened numbered cell
     * when the surrounding flag count matches its number.
     *
     * <p>A wrongly placed flag can cause the player to hit a bomb.
     *
     * @param coord opened numbered cell coordinates
     */
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

        // Open each closed neighbour until the game ends.
        for (Coord around : geometry.getCoordsAround(coord)) {
            if (boardState.get(around) == CellState.CLOSED) {
                openBox(around);

                if (state != GameState.PLAYING) {
                    return;
                }
            }
        }
    }

    /**
     * Ends the game after the player opens a bomb.
     *
     * @param bombedCoord coordinates of the exploded bomb
     */
    private void openBombs(Coord bombedCoord) {
        state = GameState.LOST_BOMB;

        revealBoard(bombedCoord);
    }

    /**
     * Reveals the entire board after the game ends.
     *
     * <p>The exploded bomb is marked separately. Other bombs
     * are revealed, and incorrectly flagged safe cells are marked.
     *
     * @param bombedCoord coordinates of the exploded bomb,
     *                    or null when the game ends by timeout
     */
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

    /**
     * Checks whether every safe cell has been opened.
     *
     * <p>The game is won only while it is still in progress.
     */
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



