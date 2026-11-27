package core;

import java.util.Objects;
import java.util.Set;

import model.CellContent;
import model.Coord;

/** Stores the hidden state of the game board, including bomb positions
 * and the number of bombs surrounding each cell.
 * The map is generated when the player makes the first click.
 */
final class BombMap {

    /** The geometry defining the dimensions and coordinates of the board. */
    private final BoardGeometry geometry;

    /** The total number of bombs to be placed on the board. */
    private final int totalBombs;

    /** The strategy responsible for placing bombs on the board. */
    private final BombPlacement placement;

    /** The matrix containing the generated content of each cell. */
    private Matrix<CellContent> contents;

    /** Creates a bomb map with the specified board geometry
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

    /** Generates the bomb map while ensuring that the specified coordinate
     * does not contain a bomb.
     *
     * @param safeCoord the coordinate that must remain free of bombs
     * @throws IllegalStateException if the placement strategy returns an
     *                               incorrect number of bombs
     * @throws IndexOutOfBoundsException if a bomb is placed outside the board
     */
    void start(Coord safeCoord) {
        Set<Coord> bombs = Objects.requireNonNull(
                placement.place(geometry, totalBombs, safeCoord)
        );

        // Ensure that the placement strategy returns the expected number of bombs.
        if (bombs.size() != totalBombs) {
            throw new IllegalStateException(
                    "Placement returned " + bombs.size()
                            + " bombs, expected " + totalBombs
            );
        }

        // Initialize the board with empty cells containing zero neighboring bombs.
        Matrix<CellContent> result = new Matrix<>(
                geometry,
                () -> CellContent.ZERO
        );

        // Place bombs at the coordinates returned by the placement strategy.
        for (Coord coord : bombs) {
            result.set(coord, CellContent.BOMB);
        }

        // Update the neighboring cells to reflect the number of surrounding bombs.
        for (Coord coord : bombs) {
            incrementNumbersAround(result, coord);
        }

        // Assign the completed map only after all initialization steps succeed.
        contents = result;
    }

    /** Clears the generated bomb map.
     * A new map will be generated when the player makes the next first click.
     */
    void reset() {
        contents = null;
    }

    /** Checks whether the bomb map has already been generated.
     *
     * @return true if the map has been generated, otherwise false
     */
    boolean isGenerated() {
        return contents != null;
    }

    /** Returns the content of the specified cell.
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

    /** Returns the configured total number of bombs.
     * @return the total number of bombs.*/
    int getTotalBombs() {
        return totalBombs;
    }

    /** Increments the bomb counters of all neighboring cells.
     * @param matrix the matrix containing the current cell contents
     * @param bomb the coordinate of the bomb.*/
    private void incrementNumbersAround(
            Matrix<CellContent> matrix,
            Coord bomb
    ) {
        // Iterate over all neighboring coordinates of the specified bomb.
        for (Coord around : geometry.getCoordsAround(bomb)) {
            CellContent content = matrix.get(around);

            // Increment numbered cells without modifying cells containing bombs.
            if (content.isNumber()) {
                matrix.set(around, content.getNextNumber());
            }
        }
    }
}




