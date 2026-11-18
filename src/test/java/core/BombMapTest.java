package core;

import model.CellContent;
import model.Coord;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static core.TestBoards.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BombMapTest {

    private final BoardGeometry geometry = new BoardGeometry(3, 3);

    private BombMap map(int bombs, Set<Coord> layout) {
        return new BombMap(geometry, bombs, (g, count, safe) -> layout);
    }

    @Test
    void readingBeforeGenerationThrows() {

        BombMap map = map(1, Set.of(at(1, 1)));

        assertThat(map.isGenerated()).isFalse();

        assertThatThrownBy(() -> map.get(at(0, 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void centralBombMakesEveryNeighbourOne() {

        BombMap map = map(1, Set.of(at(1, 1)));

        map.start(at(0, 0));

        assertThat(map.get(at(1, 1))).isEqualTo(CellContent.BOMB);

        for (Coord around : geometry.getCoordsAround(at(1, 1))) {
            assertThat(map.get(around)).isEqualTo(CellContent.NUM1);
        }
    }

    @Test
    void numberCountsAllAdjacentBombs() {

        BombMap map = map(2, Set.of(at(0, 0), at(2, 0)));

        map.start(at(1, 2));

        assertThat(map.get(at(1, 0))).isEqualTo(CellContent.NUM2);
        assertThat(map.get(at(1, 1))).isEqualTo(CellContent.NUM2);
        assertThat(map.get(at(0, 1))).isEqualTo(CellContent.NUM1);
        assertThat(map.get(at(1, 2))).isEqualTo(CellContent.ZERO);
    }

    @Test
    void wrongNumberOfBombsFromPlacementIsRejected() {

        BombMap map = map(2, Set.of(at(1, 1)));

        assertThatThrownBy(() -> map.start(at(0, 0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(map.isGenerated()).isFalse();
    }

    @Test
    void bombOutsideBoardIsRejected() {

        BombMap map = map(1, Set.of(at(5, 5)));

        assertThatThrownBy(() -> map.start(at(0, 0)))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void resetForgetsLayout() {

        BombMap map = map(1, Set.of(at(1, 1)));

        map.start(at(0, 0));
        map.reset();

        assertThat(map.isGenerated()).isFalse();

        assertThatThrownBy(() -> map.get(at(1, 1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
