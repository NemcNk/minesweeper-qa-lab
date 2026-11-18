package core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static core.TestBoards.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatrixTest {

    private final BoardGeometry geometry = new BoardGeometry(4, 2);

    @Test
    void fillsEveryCellWithDefaultValue() {

        Matrix<String> matrix = new Matrix<>(geometry, () -> "x");

        for (var coord : geometry.getAllCoords()) {
            assertThat(matrix.get(coord)).isEqualTo("x");
        }
    }

    @Test
    void asksSupplierOncePerCell() {

        AtomicInteger calls = new AtomicInteger();

        new Matrix<>(geometry, calls::incrementAndGet);

        assertThat(calls.get()).isEqualTo(8);
    }

    @Test
    void setChangesOnlyTheGivenCell() {

        Matrix<Integer> matrix = new Matrix<>(geometry, () -> 0);

        matrix.set(at(2, 1), 7);

        assertThat(matrix.get(at(2, 1))).isEqualTo(7);
        assertThat(matrix.get(at(1, 0))).isZero();
        assertThat(matrix.get(at(2, 0))).isZero();
        assertThat(matrix.get(at(1, 1))).isZero();
    }

    @Test
    void addressesCellsByColumnAndRowOnNonSquareBoard() {

        Matrix<String> matrix = new Matrix<>(geometry, () -> "-");

        matrix.set(at(3, 1), "last");     // last column, last row
        matrix.set(at(0, 0), "first");

        assertThat(matrix.get(at(3, 1))).isEqualTo("last");
        assertThat(matrix.get(at(0, 0))).isEqualTo("first");
    }

    @Test
    void readingOutsideBoardThrows() {

        Matrix<String> matrix = new Matrix<>(geometry, () -> "x");

        assertThatThrownBy(() -> matrix.get(at(4, 0)))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> matrix.get(at(0, 2)))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> matrix.get(at(-1, 0)))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> matrix.get(null))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void writingOutsideBoardThrows() {

        Matrix<String> matrix = new Matrix<>(geometry, () -> "x");

        assertThatThrownBy(() -> matrix.set(at(4, 0), "y"))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void nullValuesAreRejected() {

        Matrix<String> matrix = new Matrix<>(geometry, () -> "x");

        assertThatThrownBy(() -> matrix.set(at(0, 0), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullArgumentsInConstructorAreRejected() {

        assertThatThrownBy(() -> new Matrix<String>(null, () -> "x"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Matrix<String>(geometry, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Matrix<String>(geometry, () -> null))
                .isInstanceOf(NullPointerException.class);
    }
}