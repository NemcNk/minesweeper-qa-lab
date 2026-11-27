package core;

import model.CellState;
import model.Coord;

/** Stores and manages the current state of all cells on the board.
 * Tracks the number of opened safe cells and placed flags.
 */
final class BoardState {

    /** Provides access to the board geometry and cell coordinates. */
    private final BoardGeometry geometry;

    /** Defines the maximum number of flags that can be placed. */
    private final int totalBombs;

    /** Stores the current state of each cell on the board. */
    private Matrix<CellState> stateMap;

    /** Tracks the number of opened safe cells. */
    private int openedSafeCount;

    /** Tracks the current number of placed flags. */
    private int flaggedCount;

    /** Creates a new board state and initializes all cells.
     *
     * @param geometry the geometry of the board
     * @param totalBombs the total number of bombs on the board
     */
    BoardState(BoardGeometry geometry, int totalBombs) {
        this.geometry = geometry;
        this.totalBombs = totalBombs;

        start();
    }

    /** Resets the board to its initial state.
     * All cells are closed, and both counters are reset to zero.
     */
    void start() {
        stateMap = new Matrix<>(
                geometry,
                () -> CellState.CLOSED
        );

        openedSafeCount = 0;
        flaggedCount = 0;
    }

    /** Returns the current state of the specified cell.
     *
     * @param coord the coordinates of the cell
     * @return the current state of the cell.*/
    CellState get(Coord coord) {
        return stateMap.get(coord);
    }

    /** Opens the specified cell if it is currently closed.
     * Increments the opened safe cell counter.
     *
     * @param coord the coordinates of the cell to open.*/
    void setOpened(Coord coord) {
        if (stateMap.get(coord) == CellState.CLOSED) {
            stateMap.set(coord, CellState.OPENED);
            openedSafeCount++;
        }
    }

    /** Toggles the flag state of the specified cell.
     * Removes an existing flag or places a new one if the flag limit
     * has not been reached.
     *
     * @param coord the coordinates of the cell.*/
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
                // No action is required for cells that cannot be flagged.
            }
        }
    }

    /** Marks the specified cell as exploded.
     *
     * @param coord the coordinates of the exploded cell.*/
    void setExploded(Coord coord) {
        stateMap.set(coord, CellState.EXPLODED);
    }

    /** Opens the specified bomb cell if it is currently closed.
     *
     * @param coord the coordinates of the bomb cell.*/
    void openBomb(Coord coord) {
        if (stateMap.get(coord) == CellState.CLOSED) {
            stateMap.set(coord, CellState.OPENED);
        }
    }

    /** Marks the specified flag as incorrect.
     * @param coord the coordinates of the incorrectly flagged cell.*/
    void markWrongFlag(Coord coord) {
        if (stateMap.get(coord) == CellState.FLAGGED) {
            stateMap.set(coord, CellState.WRONG_FLAG);
        }
    }

    /** Returns the current number of placed flags.
     * @return the number of flagged cells.*/
    int getFlaggedCount() {
        return flaggedCount;
    }

    /** Returns the number of opened safe cells.
     * @return the number of opened safe cells.*/
    int getOpenedSafeCount() {
        return openedSafeCount;
    }

    /** Counts the flagged cells surrounding the specified cell.
     * @param coord the coordinates of the center cell
     * @return the number of flagged neighboring cells.*/
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




