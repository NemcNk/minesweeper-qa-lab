package core;

import model.Coord;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static core.TestBoards.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RandomBombPlacementTest {

    private final BoardGeometry geometry = new BoardGeometry(8, 8);

    @Test
    void acceptsExactlyAsManyBombsAsFitOutsideSafeArea() {
        // 64 - 9 = 55: the boundary value must be accepted
        Set<Coord> bombs = new RandomBombPlacement(new Random(3))
                .place(geometry, 55, at(4, 4));

        assertThat(bombs).hasSize(55);
    }

    @Test
    void layoutIsShuffledNotJustFirstFreeCells() {
        Coord click = at(4, 4);

        List<Coord> ordered = new ArrayList<>(geometry.getAllCoords());
        ordered.removeAll(geometry.getSafeArea(click));
        Set<Coord> firstTen = new HashSet<>(ordered.subList(0, 10));

        Set<Coord> bombs = new RandomBombPlacement(new Random(1))
                .place(geometry, 10, click);

        assertThat(bombs).isNotEqualTo(firstTen);
    }

    @Test
    void placesExactlyRequestedNumberOfBombs() {

        Set<Coord> bombs = new RandomBombPlacement(new Random(1))
                .place(geometry, 10, at(3, 3));

        assertThat(bombs).hasSize(10);
        assertThat(geometry.getAllCoords()).containsAll(bombs);
    }

    @Test
    void keepsSafeAreaFree() {

        Coord click = at(0, 0);

        Set<Coord> bombs = new RandomBombPlacement(new Random(2))
                .place(geometry, 40, click);

        assertThat(bombs).doesNotContainAnyElementsOf(
                geometry.getSafeArea(click)
        );
    }

    @Test
    void sameSeedGivesSameLayout() {

        Set<Coord> first = new RandomBombPlacement(new Random(7))
                .place(geometry, 10, at(4, 4));

        Set<Coord> second = new RandomBombPlacement(new Random(7))
                .place(geometry, 10, at(4, 4));

        assertThat(first).isEqualTo(second);
    }

    @Test
    void failsWhenBombsDoNotFitOutsideSafeArea() {

        // 64 cells minus 9 in the safe zone = 55
        RandomBombPlacement placement =
                new RandomBombPlacement(new Random(3));

        assertThatThrownBy(() -> placement.place(geometry, 56, at(4, 4)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
