package core;

import model.Coord;

import java.util.Objects;
import java.util.function.Supplier;

public final class Matrix<T> {

    private final BoardGeometry geometry;
    private final Object[][] matrix;

    public Matrix(
            BoardGeometry geometry,
            Supplier<T> defaultValue
    ) {

        this.geometry = Objects.requireNonNull(geometry);

        Objects.requireNonNull(defaultValue);

        matrix = new Object[
                geometry.getColumns()
                ][
                geometry.getRows()
                ];

        for (Coord coord : geometry.getAllCoords()) {
            matrix[coord.getX()][coord.getY()] =
                    Objects.requireNonNull(defaultValue.get());
        }
    }

    @SuppressWarnings("unchecked")
    public T get(Coord coord) {

        checkCoordinate(coord);

        return (T) matrix[coord.getX()][coord.getY()];
    }

    public void set(Coord coord, T value) {

        checkCoordinate(coord);

        matrix[coord.getX()][coord.getY()] =
                Objects.requireNonNull(value);
    }

    private void checkCoordinate(Coord coord) {

        if (!geometry.inRange(coord)) {
            throw new IndexOutOfBoundsException(
                    "Coordinate outside board"
            );
        }
    }
}

