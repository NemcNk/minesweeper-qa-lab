package model;

/**
 * Represents the possible contents of a Minesweeper cell.
 * Each content has an associated numeric value.
 */
public enum CellContent {

    ZERO(0),
    NUM1(1),
    NUM2(2),
    NUM3(3),
    NUM4(4),
    NUM5(5),
    NUM6(6),
    NUM7(7),
    NUM8(8),
    BOMB(-1);

    private final int number;

    /**
     * Creates a cell content with the specified numeric value.
     *
     * @param number the numeric value of the cell content
     */
    CellContent(int number) {
        this.number = number;
    }

    /**
     * Returns the numeric value of the cell content.
     *
     * @return the numeric value
     */
    public int getNumber() {
        return number;
    }

    /**
     * Checks whether the cell contains a number rather than a bomb.
     *
     * @return true if the cell contains a number, false otherwise
     */
    public boolean isNumber() {
        return number >= 0;
    }

    /**
     * Returns the next numeric cell content.
     *
     * @return the next numeric cell content
     * @throws IllegalStateException if the current content is NUM8 or BOMB
     */
    public CellContent getNextNumber() {
        return switch (this) {
            case ZERO -> NUM1;
            case NUM1 -> NUM2;
            case NUM2 -> NUM3;
            case NUM3 -> NUM4;
            case NUM4 -> NUM5;
            case NUM5 -> NUM6;
            case NUM6 -> NUM7;
            case NUM7 -> NUM8;
            default -> throw new IllegalStateException(
                    "Cannot increment cell content: " + this
            );
        };
    }
}

