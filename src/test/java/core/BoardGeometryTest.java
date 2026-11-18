package core;

import model.Coord;

import org.junit.jupiter.api.Test;

import static core.TestBoards.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardGeometryTest {

    private final BoardGeometry geometry = new BoardGeometry(5, 4);

    @Test
    void rejectsNonPositiveDimensions() {

        assertThatThrownBy(() -> new BoardGeometry(0, 3))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new BoardGeometry(3, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void containsEveryCellExactlyOnce() {

        assertThat(geometry.getAllCoords())
                .hasSize(20)
                .doesNotHaveDuplicates();
    }

    @Test
    void allCoordsListIsUnmodifiable() {

        assertThatThrownBy(() -> geometry.getAllCoords().add(at(0, 0)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void inRangeAcceptsBoardCellsOnly() {

        assertThat(geometry.inRange(at(0, 0))).isTrue();
        assertThat(geometry.inRange(at(4, 3))).isTrue();

        assertThat(geometry.inRange(at(-1, 0))).isFalse();
        assertThat(geometry.inRange(at(0, -1))).isFalse();
        assertThat(geometry.inRange(at(5, 0))).isFalse();
        assertThat(geometry.inRange(at(0, 4))).isFalse();
        assertThat(geometry.inRange(null)).isFalse();
    }

    @Test
    void cornerHasThreeNeighbours() {

        assertThat(geometry.getCoordsAround(at(0, 0)))
                .containsExactlyInAnyOrder(at(1, 0), at(0, 1), at(1, 1));
    }

    @Test
    void edgeHasFiveNeighbours() {

        assertThat(geometry.getCoordsAround(at(2, 0))).hasSize(5);
    }

    @Test
    void centreHasEightNeighboursWithoutItself() {

        Coord centre = at(2, 2);

        assertThat(geometry.getCoordsAround(centre))
                .hasSize(8)
                .doesNotContain(centre);
    }

    @Test
    void neighboursOfCellOutsideBoardThrow() {

        assertThatThrownBy(() -> geometry.getCoordsAround(at(9, 9)))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> geometry.getCoordsAround(null))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void safeAreaIsCellPlusNeighbours() {

        assertThat(geometry.getSafeArea(at(2, 2))).hasSize(9);
        assertThat(geometry.getSafeArea(at(0, 0))).hasSize(4);
        assertThat(geometry.getSafeArea(at(0, 0))).contains(at(0, 0));
    }
}