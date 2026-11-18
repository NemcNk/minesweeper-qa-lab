package model;

/**
 * Board size and number of bombs.
 *
 * <p>Only the basic invariants are checked here: positive dimensions and at
 * least one bomb-free cell. The rule "the first click and its neighbours are
 * safe" belongs to bomb placement, not to the configuration.
 */
public final class GameConfig {

    private final int columns;
    private final int rows;
    private final int totalBombs;

    /**
     * @throws IllegalArgumentException if a dimension is not positive,
     *                                  or the bomb count is negative
     *                                  or not smaller than the cell count
     */
    public GameConfig(int columns, int rows, int totalBombs) {

        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException(
                    "Board dimensions must be positive"
            );
        }

        long totalCells = (long) columns * rows;

        if (totalBombs < 0 || totalBombs >= totalCells) {
            throw new IllegalArgumentException(
                    "Invalid number of bombs"
            );
        }

        this.columns = columns;
        this.rows = rows;
        this.totalBombs = totalBombs;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public int getTotalBombs() {
        return totalBombs;
    }
}
