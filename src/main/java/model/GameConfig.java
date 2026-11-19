package model;

/**
 * Stores the board dimensions and the total number of bombs.
 *
 * <p>Only basic configuration invariants are checked here: dimensions must
 * be positive, and at least one bomb-free cell must remain.
 * The rule that the first click and its neighbours are safe
 * is handled by bomb placement, not by this configuration class.
 */
public final class GameConfig {

    /** The number of columns on the board. */
    private final int columns;

    /** The number of rows on the board. */
    private final int rows;

    /** The total number of bombs on the board. */
    private final int totalBombs;

    /**
     * Creates a game configuration with the specified dimensions and bomb count.
     *
     * @param columns the number of columns
     * @param rows the number of rows
     * @param totalBombs the total number of bombs
     * @throws IllegalArgumentException if a dimension is not positive,
     *                                  or the bomb count is negative
     *                                  or not smaller than the cell count
     */
    public GameConfig(int columns, int rows, int totalBombs) {
        // Validate board dimensions.
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException(
                    "Board dimensions must be positive"
            );
        }

        // Use long to prevent integer overflow when calculating cell count.
        long totalCells = (long) columns * rows;

        // Ensure that the bomb count is valid and at least one cell is safe.
        if (totalBombs < 0 || totalBombs >= totalCells) {
            throw new IllegalArgumentException(
                    "Invalid number of bombs"
            );
        }

        this.columns = columns;
        this.rows = rows;
        this.totalBombs = totalBombs;
    }

    /**
     * Returns the number of columns.
     *
     * @return the board width
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Returns the number of rows.
     *
     * @return the board height
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the total number of bombs.
     *
     * @return the total bomb count
     */
    public int getTotalBombs() {
        return totalBombs;
    }
}
