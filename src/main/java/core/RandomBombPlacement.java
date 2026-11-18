package core;

import model.Coord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Random placement that keeps the clicked cell and its neighbours free.
 */
final class RandomBombPlacement implements BombPlacement {

    private final Random random;

    RandomBombPlacement(Random random) {
        this.random = random;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if there are fewer free cells
     *                                  than bombs once the safe area is excluded
     */
    @Override
    public Set<Coord> place(
            BoardGeometry geometry,
            int bombs,
            Coord safeCoord
    ) {

        List<Coord> candidates = new ArrayList<>(geometry.getAllCoords());

        candidates.removeAll(geometry.getSafeArea(safeCoord));

        if (candidates.size() < bombs) {
            throw new IllegalArgumentException(
                    "Not enough cells for bombs"
            );
        }

        Collections.shuffle(candidates, random);

        return new HashSet<>(candidates.subList(0, bombs));
    }
}
