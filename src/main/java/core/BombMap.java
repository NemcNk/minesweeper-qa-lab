package core;

import model.CellContent;
import model.Coord;

import java.util.Objects;
import java.util.Set;

/**
 * Hidden truth about the board: where the bombs are and the number
 * of bombs around every other cell. Generated on the first click.
 */
final class BombMap {

    private final BoardGeometry geometry;
    private final int totalBombs;
    private final BombPlacement placement;

    private Matrix<CellContent> contents;

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
     * Generates the map.
     *
     * @throws IllegalStateException if the placement returns a wrong number
     *                               of bombs
     * @throws IndexOutOfBoundsException if the placement returns a cell
     *                                   outside the board
     */
    void start(Coord safeCoord) {

        Set<Coord> bombs = Objects.requireNonNull(
                placement.place(geometry, totalBombs, safeCoord)
        );

        if (bombs.size() != totalBombs) {
            throw new IllegalStateException(
                    "Placement returned " + bombs.size()
                            + " bombs, expected " + totalBombs
            );
        }

        Matrix<CellContent> result = new Matrix<>(
                geometry,
                () -> CellContent.ZERO
        );

        for (Coord coord : bombs) {
            result.set(coord, CellContent.BOMB);
        }

        for (Coord coord : bombs) {
            incrementNumbersAround(result, coord);
        }

        contents = result;
    }

    void reset() {
        contents = null;
    }

    boolean isGenerated() {
        return contents != null;
    }

    /**
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

    int getTotalBombs() {
        return totalBombs;
    }

    private void incrementNumbersAround(
            Matrix<CellContent> matrix,
            Coord bomb
    ) {

        for (Coord around : geometry.getCoordsAround(bomb)) {

            CellContent content = matrix.get(around);

            if (content.isNumber()) {
                matrix.set(around, content.getNextNumber());
            }
        }
    }
}


