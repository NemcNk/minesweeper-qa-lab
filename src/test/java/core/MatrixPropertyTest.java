package core;

import model.Coord;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Provide;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for {@link Matrix}.
 *
 * <p>Verifies matrix initialization, coordinate access,
 * value isolation and boundary validation.
 */
class MatrixPropertyTest {

    /**
     * Provides positive matrix dimensions.
     *
     * @return arbitrary pairs of matrix dimensions
     */
    @Provide
    Arbitrary<int[]> dimensions() {
        return Arbitraries.integers()
                .between(1, 30)
                .flatMap(columns ->
                        Arbitraries.integers()
                                .between(1, 30)
                                .map(rows ->
                                        new int[]{columns, rows}
                                )
                );
    }

    /**
     * Verifies that every matrix cell is initialized.
     *
     * @param dimensions generated matrix dimensions
     */
    @Property(tries = 100)
    void shouldInitializeEveryCell(
            @ForAll("dimensions") int[] dimensions
    ) {
        BoardGeometry geometry = new BoardGeometry(
                dimensions[0], dimensions[1]
        );

        Matrix<Integer> matrix = new Matrix<>(
                geometry, () -> 0
        );

        for (Coord coord : geometry.getAllCoords()) {
            assertThat(matrix.get(coord)).isZero();
        }
    }

    /**
     * Verifies that matrix values can be written and retrieved.
     *
     * @param x generated column
     * @param y generated row
     * @param value generated value
     */
    @Property(tries = 200)
    void shouldPreserveWrittenValues(
            @ForAll @IntRange(min = 0, max = 19) int x,
            @ForAll @IntRange(min = 0, max = 19) int y,
            @ForAll int value
    ) {
        BoardGeometry geometry = new BoardGeometry(20, 20);
        Matrix<Integer> matrix = new Matrix<>(
                geometry, () -> 0
        );

        Coord coord = new Coord(x, y);

        matrix.set(coord, value);

        assertThat(matrix.get(coord)).isEqualTo(value);
    }

    /**
     * Verifies that modifying one cell does not modify another.
     *
     * @param x first column
     * @param y first row
     * @param value value to store
     */
    @Property(tries = 100)
    void shouldKeepOtherCellsUnchanged(
            @ForAll @IntRange(min = 0, max = 19) int x,
            @ForAll @IntRange(min = 0, max = 19) int y,
            @ForAll int value
    ) {
        BoardGeometry geometry = new BoardGeometry(20, 20);
        Matrix<Integer> matrix = new Matrix<>(
                geometry, () -> 0
        );

        Coord first = new Coord(x, y);
        Coord second = new Coord((x + 1) % 20, y);

        matrix.set(first, value);

        assertThat(matrix.get(second)).isZero();
    }

    /**
     * Verifies that coordinates outside the matrix are rejected.
     *
     * @param x invalid column
     */
    @Property
    void shouldRejectInvalidCoordinates(
            @ForAll @IntRange(min = 20, max = 100) int x
    ) {
        Matrix<Integer> matrix = new Matrix<>(
                new BoardGeometry(20, 20), () -> 0
        );

        assertThatThrownBy(
                () -> matrix.get(new Coord(x, 0))
        ).isInstanceOf(IndexOutOfBoundsException.class);
    }
}
