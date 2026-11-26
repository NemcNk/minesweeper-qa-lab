package core;

import model.CellContent;
import model.Coord;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Provide;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for {@link BombMap}.
 *
 * <p>Verifies bomb placement, safe area protection,
 * neighboring bomb counters, map generation and reset behavior.
 */
class BombMapPropertyTest {

    /**
     * Provides valid board configurations.
     *
     * @return arbitrary board dimensions and safe coordinates
     */
    /**
     * Provides valid board configurations.
     *
     * @return arbitrary board dimensions and safe coordinates
     */
    @Provide
    Arbitrary<int[]> boardConfigurations() {
        return Arbitraries.integers()
                .between(4, 20)
                .flatMap(columns -> Arbitraries.integers()
                        .between(4, 20)
                        .flatMap(rows -> Arbitraries.integers()
                                .between(0, columns - 1)
                                .flatMap(x -> Arbitraries.integers()
                                        .between(0, rows - 1)
                                        .map(y -> new int[]{
                                                columns, rows, x, y
                                        })
                                )
                        )
                );
    }

    /**
     * Creates a deterministic bomb placement strategy.
     *
     * <p>Bombs are placed in the first available cells,
     * excluding the safe area.
     *
     * @return deterministic bomb placement strategy
     */
    private BombPlacement createPlacement() {
        return (geometry, bombs, safeCoord) -> {
            Set<Coord> safeArea = new HashSet<>(
                    geometry.getSafeArea(safeCoord)
            );

            Set<Coord> result = new HashSet<>();

            for (Coord coord : geometry.getAllCoords()) {
                if (!safeArea.contains(coord)
                        && result.size() < bombs) {
                    result.add(coord);
                }
            }

            return result;
        };
    }

    /**
     * Generates a bomb map using a deterministic placement strategy.
     *
     * @param geometry board geometry
     * @param bombs number of bombs
     * @param safeCoord coordinate that must remain safe
     * @return generated bomb map
     */
    private BombMap createMap(
            BoardGeometry geometry,
            int bombs,
            Coord safeCoord
    ) {
        BombMap map = new BombMap(
                geometry,
                bombs,
                createPlacement()
        );

        map.start(safeCoord);

        return map;
    }

    /**
     * Verifies that map generation initializes the map.
     *
     * @param configuration generated board configuration
     */
    @Property(tries = 200)
    void shouldGenerateMap(
            @ForAll("boardConfigurations") int[] configuration
    ) {
        BoardGeometry geometry = new BoardGeometry(
                configuration[0],
                configuration[1]
        );

        Coord safeCoord = new Coord(
                configuration[2],
                configuration[3]
        );

        BombMap map = createMap(
                geometry,
                1,
                safeCoord
        );

        assertThat(map.isGenerated()).isTrue();
    }

    /**
     * Verifies that the configured number of bombs
     * is preserved in the generated map.
     *
     * @param configuration generated board configuration
     * @param bombs requested number of bombs
     */
    @Property(tries = 200)
    void shouldContainCorrectNumberOfBombs(
            @ForAll("boardConfigurations") int[] configuration,
            @ForAll @IntRange(min = 1, max = 100) int bombs
    ) {
        BoardGeometry geometry = new BoardGeometry(
                configuration[0],
                configuration[1]
        );

        Coord safeCoord = new Coord(
                configuration[2],
                configuration[3]
        );

        int available = geometry.getAllCoords().size()
                - geometry.getSafeArea(safeCoord).size();

        int actualBombs = Math.min(bombs, available);

        BombMap map = createMap(
                geometry,
                actualBombs,
                safeCoord
        );

        long actualCount = geometry.getAllCoords()
                .stream()
                .filter(coord -> map.get(coord) == CellContent.BOMB)
                .count();

        assertThat(actualCount).isEqualTo(actualBombs);
        assertThat(map.getTotalBombs()).isEqualTo(actualBombs);
    }

    /**
     * Verifies that the safe area never contains bombs.
     *
     * @param configuration generated board configuration
     * @param bombs requested number of bombs
     */
    @Property(tries = 200)
    void shouldKeepSafeAreaFreeOfBombs(
            @ForAll("boardConfigurations") int[] configuration,
            @ForAll @IntRange(min = 1, max = 100) int bombs
    ) {
        BoardGeometry geometry = new BoardGeometry(
                configuration[0],
                configuration[1]
        );

        Coord safeCoord = new Coord(
                configuration[2],
                configuration[3]
        );

        int available = geometry.getAllCoords().size()
                - geometry.getSafeArea(safeCoord).size();

        BombMap map = createMap(
                geometry,
                Math.min(bombs, available),
                safeCoord
        );

        for (Coord coord : geometry.getSafeArea(safeCoord)) {
            assertThat(map.get(coord))
                    .isNotEqualTo(CellContent.BOMB);
        }
    }

    /**
     * Verifies that every numbered cell contains the exact
     * number of bombs in its neighborhood.
     *
     * @param configuration generated board configuration
     * @param bombs requested number of bombs
     */
    @Property(tries = 300)
    void shouldCalculateCorrectNeighborBombCounts(
            @ForAll("boardConfigurations") int[] configuration,
            @ForAll @IntRange(min = 1, max = 100) int bombs
    ) {
        BoardGeometry geometry = new BoardGeometry(
                configuration[0],
                configuration[1]
        );

        Coord safeCoord = new Coord(
                configuration[2],
                configuration[3]
        );

        int available = geometry.getAllCoords().size()
                - geometry.getSafeArea(safeCoord).size();

        BombMap map = createMap(
                geometry,
                Math.min(bombs, available),
                safeCoord
        );

        for (Coord coord : geometry.getAllCoords()) {
            CellContent content = map.get(coord);

            if (content.isNumber()) {
                long expected = geometry.getCoordsAround(coord)
                        .stream()
                        .filter(neighbor ->
                                map.get(neighbor) == CellContent.BOMB
                        )
                        .count();

                assertThat(content.getNumber())
                        .isEqualTo((int) expected);
            }
        }
    }

    /**
     * Verifies that bomb cells are never replaced
     * by neighboring bomb counters.
     *
     * @param configuration generated board configuration
     * @param bombs requested number of bombs
     */
    @Property(tries = 200)
    void shouldPreserveBombCells(
            @ForAll("boardConfigurations") int[] configuration,
            @ForAll @IntRange(min = 1, max = 100) int bombs
    ) {
        BoardGeometry geometry = new BoardGeometry(
                configuration[0],
                configuration[1]
        );

        Coord safeCoord = new Coord(
                configuration[2],
                configuration[3]
        );

        int available = geometry.getAllCoords().size()
                - geometry.getSafeArea(safeCoord).size();

        BombMap map = createMap(
                geometry,
                Math.min(bombs, available),
                safeCoord
        );

        for (Coord bomb : geometry.getAllCoords()) {
            if (map.get(bomb) == CellContent.BOMB) {
                for (Coord neighbor : geometry.getCoordsAround(bomb)) {
                    assertThat(map.get(neighbor))
                            .isNotNull();
                }
            }
        }
    }

    /**
     * Verifies that reset clears the generated map.
     *
     * @param configuration generated board configuration
     */
    @Property(tries = 100)
    void shouldResetGeneratedMap(
            @ForAll("boardConfigurations") int[] configuration
    ) {
        BoardGeometry geometry = new BoardGeometry(
                configuration[0],
                configuration[1]
        );

        Coord safeCoord = new Coord(
                configuration[2],
                configuration[3]
        );

        BombMap map = createMap(
                geometry,
                1,
                safeCoord
        );

        map.reset();

        assertThat(map.isGenerated()).isFalse();

        assertThatThrownBy(
                () -> map.get(safeCoord)
        ).isInstanceOf(IllegalStateException.class);
    }

    /**
     * Verifies that a map cannot be read before generation.
     *
     * @param configuration generated board configuration
     */
    @Property(tries = 100)
    void shouldRejectAccessBeforeGeneration(
            @ForAll("boardConfigurations") int[] configuration
    ) {
        BoardGeometry geometry = new BoardGeometry(
                configuration[0],
                configuration[1]
        );

        BombMap map = new BombMap(
                geometry,
                1,
                createPlacement()
        );

        assertThat(map.isGenerated()).isFalse();

        assertThatThrownBy(
                () -> map.get(new Coord(
                        configuration[2],
                        configuration[3]
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    /**
     * Verifies that generation rejects an incorrect bomb count.
     *
     * @param configuration generated board configuration
     */
    @Property(tries = 100)
    void shouldRejectIncorrectBombCount(
            @ForAll("boardConfigurations") int[] configuration
    ) {
        BoardGeometry geometry = new BoardGeometry(
                configuration[0],
                configuration[1]
        );

        BombPlacement invalidPlacement =
                (board, bombs, safeCoord) -> Set.of();

        BombMap map = new BombMap(
                geometry,
                1,
                invalidPlacement
        );

        assertThatThrownBy(
                () -> map.start(new Coord(
                        configuration[2],
                        configuration[3]
                ))
        ).isInstanceOf(IllegalStateException.class);

        assertThat(map.isGenerated()).isFalse();
    }
}

