package core;

import java.util.Objects;
import java.util.Set;

import model.CellContent;
import model.Coord;

/**
 * Stores the hidden state of the board, including bomb positions
 * and the number of bombs surrounding each cell.
 * The map is generated when the player makes the first click.
 */
final class BombMap {

    private final BoardGeometry geometry;
    private final int totalBombs;
    private final BombPlacement placement;

    private Matrix<CellContent> contents;

    /**
     * Creates a bomb map using the specified board geometry
     * and bomb placement strategy.
     *
     * @param geometry the geometry of the game board
     * @param totalBombs the total number of bombs
     * @param placement the strategy used to place bombs
     */
    BombMap(
            BoardGeometry geometry,
            int totalBombs,
            BombPlacement placement
    ) {
        this.geometry = geometry;
        this.totalBombs = totalBombs;
        this.placement = placement;
    }

    /**
     * Generates the bomb map using the specified safe coordinate.
     *
     * @param safeCoord the coordinate that must not contain a bomb
     * @throws IllegalStateException if the placement returns an incorrect
     *                               number of bombs
     * @throws IndexOutOfBoundsException if a bomb is placed outside the board
     */
    void start(Coord safeCoord) {
        Set<Coord> bombs = Objects.requireNonNull(
                placement.place(geometry, totalBombs, safeCoord)
        );

        // Verify that the placement strategy returned the expected number of bombs.
        if (bombs.size() != totalBombs) {
            throw new IllegalStateException(
                    "Placement returned " + bombs.size()
                            + " bombs, expected " + totalBombs
            );
        }

        // Initialize every cell with zero bombs around it.
        Matrix<CellContent> result = new Matrix<>(
                geometry,
                () -> CellContent.ZERO
        );

        // Place bombs on the board.
        for (Coord coord : bombs) {
            result.set(coord, CellContent.BOMB);
        }

        // Calculate the number of bombs surrounding each cell.
        for (Coord coord : bombs) {
            incrementNumbersAround(result, coord);
        }

        // Store the generated map only after all calculations are complete.
        contents = result;
    }

    /**
     * Clears the generated bomb map.
     * The map will be generated again on the next first click.
     */
    void reset() {
        contents = null;
    }

    /**
     * Checks whether the bomb map has already been generated.
     *
     * @return true if the map has been generated, otherwise false
     */
    boolean isGenerated() {
        return contents != null;
    }

    /**
     * Returns the content of the specified cell.
     *
     * @param coord the coordinate of the cell
     * @return the content of the specified cell
     * @throws IllegalStateException if the map has not been generated
     */
    CellContent get(Coord coord) {
        if (contents == null) {
            throw new IllegalStateException(
                    "Bomb map has not been generated"
            );
        }

        return contents.get(coord);
    }

    /**
     * Returns the configured total number of bombs.
     *
     * @return the total number of bombs
     */
    int getTotalBombs() {
        return totalBombs;
    }

    /**
     * Increments the bomb counters of all neighboring cells.
     *
     * @param matrix the matrix containing the current cell contents
     * @param bomb the coordinate of the bomb
     */
    private void incrementNumbersAround(
            Matrix<CellContent> matrix,
            Coord bomb
    ) {
        // Get all neighboring coordinates around the bomb.
        for (Coord around : geometry.getCoordsAround(bomb)) {
            CellContent content = matrix.get(around);

            // Increment only numbered cells, leaving other bombs unchanged.
            if (content.isNumber()) {
                matrix.set(around, content.getNextNumber());
            }
        }
    }
}



