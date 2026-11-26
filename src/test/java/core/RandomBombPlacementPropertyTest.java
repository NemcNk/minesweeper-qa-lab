package core;

import model.Coord;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for {@link RandomBombPlacement}.
 *
 * <p>Verifies bomb count, uniqueness, board boundaries
 * and safe area exclusion.
 */
class RandomBombPlacementPropertyTest {

    /**
     * Verifies that bomb placement satisfies all basic invariants.
     *
     * @param columns number of columns
     * @param rows number of rows
     * @param x safe coordinate column
     * @param y safe coordinate row
     */
    @Property(tries = 200)
    void shouldPlaceCorrectNumberOfBombs(
            @ForAll @IntRange(min = 4, max = 30) int columns,
            @ForAll @IntRange(min = 4, max = 30) int rows,
            @ForAll @IntRange(min = 0, max = 29) int x,
            @ForAll @IntRange(min = 0, max = 29) int y
    ) {
        BoardGeometry geometry = new BoardGeometry(columns, rows);

        Coord safe = new Coord(x % columns, y % rows);

        Set<Coord> safeArea = Set.copyOf(
                geometry.getSafeArea(safe)
        );

        int available = columns * rows - safeArea.size();

        int bombs = Math.min(available, 10);

        RandomBombPlacement placement =
                new RandomBombPlacement(new Random(42));

        Set<Coord> result = placement.place(
                geometry, bombs, safe
        );

        assertThat(result).hasSize(bombs);

        assertThat(result)
                .allMatch(geometry::inRange)
                .doesNotContainAnyElementsOf(safeArea);
    }

    /**
     * Verifies that placement fails when there are insufficient cells.
     *
     * @param columns number of columns
     * @param rows number of rows
     */
    @Property(tries = 100)
    void shouldRejectExcessiveBombCount(
            @ForAll @IntRange(min = 1, max = 10) int columns,
            @ForAll @IntRange(min = 1, max = 10) int rows
    ) {
        BoardGeometry geometry = new BoardGeometry(columns, rows);

        Coord safe = new Coord(0, 0);

        int available = columns * rows
                - geometry.getSafeArea(safe).size();

        RandomBombPlacement placement =
                new RandomBombPlacement(new Random(42));

        assertThatThrownBy(
                () -> placement.place(
                        geometry, available + 1, safe
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }
}

