package core;

import model.Coord;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link BoardGeometry}.
 *
 * <p>Verifies coordinate generation, neighbor relationships
 * and safe area construction.
 */
class BoardGeometryPropertyTest {

    /**
     * Verifies that the number of generated coordinates
     * matches the board dimensions.
     *
     * @param columns number of columns
     * @param rows number of rows
     */
    @Property(tries = 200)
    void shouldGenerateCorrectNumberOfCoordinates(
            @ForAll @IntRange(min = 1, max = 50) int columns,
            @ForAll @IntRange(min = 1, max = 50) int rows
    ) {
        BoardGeometry geometry = new BoardGeometry(columns, rows);

        assertThat(geometry.getAllCoords())
                .hasSize(columns * rows);

        assertThat(new HashSet<>(geometry.getAllCoords()))
                .hasSize(columns * rows);
    }

    /**
     * Verifies that every generated coordinate is inside the board.
     *
     * @param columns number of columns
     * @param rows number of rows
     */
    @Property(tries = 200)
    void shouldGenerateOnlyValidCoordinates(
            @ForAll @IntRange(min = 1, max = 50) int columns,
            @ForAll @IntRange(min = 1, max = 50) int rows
    ) {
        BoardGeometry geometry = new BoardGeometry(columns, rows);

        assertThat(geometry.getAllCoords())
                .allMatch(geometry::inRange);
    }

    /**
     * Verifies the number and validity of neighboring cells.
     *
     * @param columns number of columns
     * @param rows number of rows
     * @param x column of the center coordinate
     * @param y row of the center coordinate
     */
    @Property(tries = 300)
    void shouldReturnOnlyValidNeighbors(
            @ForAll @IntRange(min = 1, max = 20) int columns,
            @ForAll @IntRange(min = 1, max = 20) int rows,
            @ForAll @IntRange(min = 0, max = 19) int x,
            @ForAll @IntRange(min = 0, max = 19) int y
    ) {
        int validX = x % columns;
        int validY = y % rows;

        BoardGeometry geometry = new BoardGeometry(columns, rows);
        Coord center = new Coord(validX, validY);

        List<Coord> neighbors = geometry.getCoordsAround(center);

        assertThat(neighbors)
                .hasSizeBetween(0, 8)
                .doesNotContain(center)
                .allMatch(geometry::inRange);

        assertThat(new HashSet<>(neighbors))
                .hasSameSizeAs(neighbors);

        for (Coord neighbor : neighbors) {
            assertThat(Math.abs(neighbor.getX() - validX))
                    .isLessThanOrEqualTo(1);

            assertThat(Math.abs(neighbor.getY() - validY))
                    .isLessThanOrEqualTo(1);
        }
    }

    /**
     * Verifies that neighboring relationships are symmetric.
     *
     * @param columns number of columns
     * @param rows number of rows
     * @param x first coordinate column
     * @param y first coordinate row
     */
    @Property(tries = 200)
    void shouldHaveSymmetricNeighbors(
            @ForAll @IntRange(min = 1, max = 20) int columns,
            @ForAll @IntRange(min = 1, max = 20) int rows,
            @ForAll @IntRange(min = 0, max = 19) int x,
            @ForAll @IntRange(min = 0, max = 19) int y
    ) {
        BoardGeometry geometry = new BoardGeometry(columns, rows);

        Coord first = new Coord(x % columns, y % rows);

        for (Coord second : geometry.getCoordsAround(first)) {
            assertThat(geometry.getCoordsAround(second))
                    .contains(first);
        }
    }

    /**
     * Verifies that a safe area contains its center coordinate.
     *
     * @param columns number of columns
     * @param rows number of rows
     * @param x center column
     * @param y center row
     */
    @Property(tries = 200)
    void shouldIncludeCenterInSafeArea(
            @ForAll @IntRange(min = 1, max = 20) int columns,
            @ForAll @IntRange(min = 1, max = 20) int rows,
            @ForAll @IntRange(min = 0, max = 19) int x,
            @ForAll @IntRange(min = 0, max = 19) int y
    ) {
        BoardGeometry geometry = new BoardGeometry(columns, rows);

        Coord center = new Coord(x % columns, y % rows);

        assertThat(geometry.getSafeArea(center))
                .contains(center)
                .hasSizeBetween(1, 9);
    }
}
