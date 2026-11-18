package core;

import model.Coord;

import java.util.Set;

/**
 * Strategy that decides where the bombs are.
 *
 * <p>The game uses {@link RandomBombPlacement}; tests can supply a fixed
 * layout to make scenarios deterministic.
 */
interface BombPlacement {

    /**
     * Chooses bomb positions.
     *
     * @param geometry  the board
     * @param bombs     how many bombs to place
     * @param safeCoord the cell of the first click
     * @return exactly {@code bombs} distinct cells inside the board
     */
    Set<Coord> place(BoardGeometry geometry, int bombs, Coord safeCoord);
}
