package core;

import model.Coord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BoardGeometry {

    private final int columns;
    private final int rows;

    private final List<Coord> allCoords;

    public BoardGeometry(int columns, int rows) {

        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException(
                    "Board dimensions must be positive"
            );
        }

        this.columns = columns;
        this.rows = rows;

        List<Coord> coords = new ArrayList<>();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                coords.add(new Coord(x, y));
            }
        }

        allCoords = Collections.unmodifiableList(coords);
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public List<Coord> getAllCoords() {
        return allCoords;
    }

    public boolean inRange(Coord coord) {

        return coord != null
                && coord.getX() >= 0
                && coord.getX() < columns
                && coord.getY() >= 0
                && coord.getY() < rows;
    }

    public List<Coord> getCoordsAround(Coord coord) {

        if (!inRange(coord)) {
            throw new IndexOutOfBoundsException(
                    "Coordinate outside board"
            );
        }

        List<Coord> result = new ArrayList<>();

        for (int y = coord.getY() - 1;
             y <= coord.getY() + 1; y++) {

            for (int x = coord.getX() - 1;
                 x <= coord.getX() + 1; x++) {

                Coord around = new Coord(x, y);

                if (inRange(around) && !around.equals(coord)) {
                    result.add(around);
                }
            }
        }

        return result;
    }

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

