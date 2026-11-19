package core;

import java.util.Objects;
import java.util.function.Supplier;

import model.Coord;

/**
 * Represents a generic two-dimensional matrix based on board geometry.
 *
 * @param <T> the type of elements stored in the matrix
 */
public final class Matrix<T> {

    private final BoardGeometry geometry;
    private final Object[][] matrix;

    /**
     * Creates a matrix and initializes all cells with default values.
     *
     * @param geometry the geometry defining the matrix dimensions
     * @param defaultValue the supplier providing initial values for cells
     * @throws NullPointerException if geometry, defaultValue,
     *                              or a supplied value is null
     */
    public Matrix(
            BoardGeometry geometry,
            Supplier<T> defaultValue
    ) {
        this.geometry = Objects.requireNonNull(geometry);
        Objects.requireNonNull(defaultValue);

        // Allocate the matrix using the dimensions defined by the geometry.
        matrix = new Object[
                geometry.getColumns()
                ][
                geometry.getRows()
                ];

        // Initialize every valid coordinate with a default value.
        for (Coord coord : geometry.getAllCoords()) {
            matrix[coord.getX()][coord.getY()] =
                    Objects.requireNonNull(defaultValue.get());
        }
    }

    /**
     * Returns the value stored at the specified coordinate.
     *
     * @param coord the coordinate of the requested cell
     * @return the value stored at the coordinate
     * @throws IndexOutOfBoundsException if the coordinate is outside the board
     */
    @SuppressWarnings("unchecked")
    public T get(Coord coord) {
        checkCoordinate(coord);

        return (T) matrix[coord.getX()][coord.getY()];
    }

    /**
     * Updates the value at the specified coordinate.
     *
     * @param coord the coordinate of the cell to update
     * @param value the new value to store
     * @throws IndexOutOfBoundsException if the coordinate is outside the board
     * @throws NullPointerException if the coordinate or value is null
     */
    public void set(Coord coord, T value) {
        checkCoordinate(coord);

        matrix[coord.getX()][coord.getY()] =
                Objects.requireNonNull(value);
    }

    /**
     * Checks whether the specified coordinate belongs to the board.
     *
     * @param coord the coordinate to validate
     * @throws NullPointerException if the coordinate is null
     * @throws IndexOutOfBoundsException if the coordinate is outside the board
     */
    private void checkCoordinate(Coord coord) {
        if (!geometry.inRange(coord)) {
            throw new IndexOutOfBoundsException(
                    "Coordinate outside board"
            );
        }
    }
}


