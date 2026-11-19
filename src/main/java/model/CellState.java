package model;

/**
 * Represents the possible states of a Minesweeper cell.
 */
public enum CellState {

    CLOSED,
    OPENED,
    FLAGGED,
    EXPLODED,
    WRONG_FLAG
}
