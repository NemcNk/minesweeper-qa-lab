package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import model.Coord;

/**
 * Randomly places bombs while keeping the clicked cell
 * and its neighbours free from bombs.
 */
final class RandomBombPlacement implements BombPlacement {

    /** Random generator used to shuffle candidate cells. */
    private final Random random;

    /**
     * Creates a random bomb placement strategy.
     *
     * @param random random number generator
     */
    RandomBombPlacement(Random random) {
        this.random = random;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if there are not enough
     *         available cells after excluding the safe area
     */
    @Override
    public Set<Coord> place(
            BoardGeometry geometry,
            int bombs,
            Coord safeCoord
    ) {
        // Get all board coordinates as potential bomb locations.
        List<Coord> candidates = new ArrayList<>(
                geometry.getAllCoords()
        );

        // Exclude the clicked cell and its neighbouring cells.
        candidates.removeAll(geometry.getSafeArea(safeCoord));

        // Ensure that enough candidate cells are available.
        if (candidates.size() < bombs) {
            throw new IllegalArgumentException(
                    "Not enough cells for bombs"
            );
        }

        // Shuffle candidates to ensure random bomb distribution.
        Collections.shuffle(candidates, random);

        // Select the requested number of cells and return them as a set.
        return new HashSet<>(candidates.subList(0, bombs));
    }
}

