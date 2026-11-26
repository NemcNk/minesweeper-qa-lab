package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameConfigTest {

    @Test
    void keepsValues() {

        GameConfig config = new GameConfig(8, 6, 10);

        assertThat(config.getColumns()).isEqualTo(8);
        assertThat(config.getRows()).isEqualTo(6);
        assertThat(config.getTotalBombs()).isEqualTo(10);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 5",
            "5, 0",
            "-1, 5",
            "5, -1"
    })
    void rejectsInvalidDimensions(int columns, int rows) {
        assertThatThrownBy(() ->
                new GameConfig(columns, rows, 0)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 3, 9",
            "2, 2, 4",
            "1, 1, 1"
    })
    void rejectsBombCountEqualToCellCount(
            int columns,
            int rows,
            int bombs
    ) {
        assertThatThrownBy(() ->
                new GameConfig(columns, rows, bombs)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeBombCount() {
        assertThatThrownBy(() ->
                new GameConfig(3, 3, -1)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsZeroBombs() {
        assertThatCode(() ->
                new GameConfig(3, 3, 0)
        ).doesNotThrowAnyException();
    }

    @Test
    void acceptsMaximumBombCountAllowedByConfig() {
        assertThatCode(() ->
                new GameConfig(3, 3, 8)
        ).doesNotThrowAnyException();
    }

    @Test
    void acceptsOneLessBombThanCellCount() {
        assertThatCode(() ->
                new GameConfig(3, 3, 8)
        ).doesNotThrowAnyException();
    }

    @Test
    void handlesLargeCellCountWithoutIntegerOverflow() {
        assertThatCode(() ->
                new GameConfig(50_000, 50_000, Integer.MAX_VALUE)
        ).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void createsValidConfigForEveryDifficulty(Difficulty difficulty) {
        GameConfig config = difficulty.createConfig();

        assertThat(config.getColumns()).isEqualTo(difficulty.getSize());
        assertThat(config.getRows()).isEqualTo(difficulty.getSize());
        assertThat(config.getTotalBombs()).isEqualTo(difficulty.getMines());
    }
}