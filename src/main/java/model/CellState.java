package model;

/** Represents the possible states of a Minesweeper cell.*/
public enum CellState {

    /** Indicates that the cell is closed and has not been revealed. */
    CLOSED,

    /** Indicates that the cell has been opened and revealed. */
    OPENED,

    /** Indicates that the cell has been marked with a flag. */
    FLAGGED,

    /** Indicates that the cell contains a mine that has been triggered. */
    EXPLODED,

    /** Indicates that the cell is incorrectly marked with a flag. */
    WRONG_FLAG
}
