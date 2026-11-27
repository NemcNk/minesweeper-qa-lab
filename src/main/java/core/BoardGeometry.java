package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Coord;

/** Represents the geometry of a rectangular game board.
 * Provides methods for retrieving board coordinates,
 * checking coordinate boundaries, and finding neighboring cells.
 */
public final class BoardGeometry {

    /** Number of columns on the board. */
    private final int columns;

    /** Number of rows on the board. */
    private final int rows;

    /** Immutable list containing all board coordinates. */
    private final List<Coord> allCoords;

    /** Creates a board geometry with the specified dimensions.
     *
     * @param columns number of columns
     * @param rows number of rows
     * @throws IllegalArgumentException if dimensions are not positive
     */
    public BoardGeometry(int columns, int rows) {
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException(
                    "Board dimensions must be positive"
            );
        }

        this.columns = columns;
        this.rows = rows;

        List<Coord> coords = new ArrayList<>();

        // Generate coordinates for every cell on the board.
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                coords.add(new Coord(x, y));
            }
        }

        allCoords = Collections.unmodifiableList(coords);
    }

    /** Returns the number of columns on the board.
     * @return number of columns.*/
    public int getColumns() {
        return columns;
    }

    /** Returns the number of rows on the board.
     * @return number of rows.*/
    public int getRows() {
        return rows;
    }

    /** Returns an immutable list of all board coordinates.
     * @return list of all coordinates.*/
    public List<Coord> getAllCoords() {
        return allCoords;
    }

    /** Checks whether a coordinate belongs to the board.
     * @param coord coordinate to validate
     * @return true if the coordinate is inside the board, false otherwise
     */
    public boolean inRange(Coord coord) {
        return coord != null
                && coord.getX() >= 0
                && coord.getX() < columns
                && coord.getY() >= 0
                && coord.getY() < rows;
    }

    /** Returns all valid neighboring coordinates surrounding a cell.
     * Diagonal neighbors are included.
     * @param coord center coordinate
     * @return list of neighboring coordinates
     * @throws IndexOutOfBoundsException if the coordinate is outside the board
     */
    public List<Coord> getCoordsAround(Coord coord) {
        if (!inRange(coord)) {
            throw new IndexOutOfBoundsException(
                    "Coordinate outside board"
            );
        }

        List<Coord> result = new ArrayList<>();

        int minY = coord.getY() - 1;
        int maxY = coord.getY() + 1;
        int minX = coord.getX() - 1;
        int maxX = coord.getX() + 1;

        // Check all eight possible neighboring positions.
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                Coord around = new Coord(x, y);

                if (inRange(around) && !around.equals(coord)) {
                    result.add(around);
                }
            }
        }

        return result;
    }

    /** Returns the safe area containing the center cell
     * and all its valid neighboring cells.
     * @param center center coordinate of the safe area
     * @return list containing the center and its neighbors
     * @throws IndexOutOfBoundsException if the center is outside the board
     */
    public List<Coord> getSafeArea(Coord center) {
        if (!inRange(center)) {
            throw new IndexOutOfBoundsException(
                    "Coordinate outside board"
            );
        }

        List<Coord> result = new ArrayList<>();

        result.add(center);
        result.addAll(getCoordsAround(center));

        return result;
    }
}


