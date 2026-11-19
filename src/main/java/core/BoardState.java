package core;

import model.CellState;
import model.Coord;

/**
 * Stores and manages the current state of all cells on the board.
 * Also tracks the number of opened safe cells and placed flags.
 */
final class BoardState {

    private final BoardGeometry geometry;
    private final int totalBombs;

    private Matrix<CellState> stateMap;

    private int openedSafeCount;
    private int flaggedCount;

    /**
     * Creates a new board state and initializes all cells.
     *
     * @param geometry board geometry used to access cell coordinates
     * @param totalBombs total number of bombs on the board
     */
    BoardState(BoardGeometry geometry, int totalBombs) {
        this.geometry = geometry;
        this.totalBombs = totalBombs;

        start();
    }

    /**
     * Resets the board state.
     * All cells become closed and all counters are reset.
     */
    void start() {
        stateMap = new Matrix<>(
                geometry,
                () -> CellState.CLOSED
        );

        openedSafeCount = 0;
        flaggedCount = 0;
    }

    /**
     * Returns the current state of the specified cell.
     *
     * @param coord cell coordinates
     * @return current cell state
     */
    CellState get(Coord coord) {
        return stateMap.get(coord);
    }

    /**
     * Opens a cell if it is currently closed.
     * Increments the opened safe cell counter.
     *
     * @param coord cell coordinates
     */
    void setOpened(Coord coord) {
        if (stateMap.get(coord) == CellState.CLOSED) {
            stateMap.set(coord, CellState.OPENED);
            openedSafeCount++;
        }
    }

    /**
     * Toggles a flag on the specified cell.
     * A new flag can only be placed if the flag limit is not reached.
     *
     * @param coord cell coordinates
     */
    void toggleFlag(Coord coord) {
        switch (stateMap.get(coord)) {
            case FLAGGED -> {
                stateMap.set(coord, CellState.CLOSED);
                flaggedCount--;
            }

            case CLOSED -> {
                if (flaggedCount < totalBombs) {
                    stateMap.set(coord, CellState.FLAGGED);
                    flaggedCount++;
                }
            }

            default -> {
                // Do nothing if the cell is already opened or exploded.
            }
        }
    }

    /**
     * Marks the specified cell as exploded.
     *
     * @param coord cell coordinates
     */
    void setExploded(Coord coord) {
        stateMap.set(coord, CellState.EXPLODED);
    }

    /**
     * Opens a bomb cell if it is currently closed.
     *
     * @param coord cell coordinates
     */
    void openBomb(Coord coord) {
        if (stateMap.get(coord) == CellState.CLOSED) {
            stateMap.set(coord, CellState.OPENED);
        }
    }

    /**
     * Marks a flag as incorrect.
     *
     * @param coord cell coordinates
     */
    void markWrongFlag(Coord coord) {
        if (stateMap.get(coord) == CellState.FLAGGED) {
            stateMap.set(coord, CellState.WRONG_FLAG);
        }
    }

    /**
     * Returns the current number of placed flags.
     *
     * @return number of flagged cells
     */
    int getFlaggedCount() {
        return flaggedCount;
    }

    /**
     * Returns the number of opened safe cells.
     *
     * @return number of opened safe cells
     */
    int getOpenedSafeCount() {
        return openedSafeCount;
    }

    /**
     * Counts flagged cells surrounding the specified cell.
     *
     * @param coord center cell coordinates
     * @return number of flagged neighboring cells
     */
    int getFlaggedBoxesAround(Coord coord) {
        int count = 0;

        for (Coord around : geometry.getCoordsAround(coord)) {
            if (stateMap.get(around) == CellState.FLAGGED) {
                count++;
            }
        }

        return count;
    }
}



