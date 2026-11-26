package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link Difficulty} enum.
 */
public class DifficultyTest {

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void everyDifficultyHasTitle(Difficulty difficulty) {
        assertThat(difficulty.getTitle()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void everyDifficultyHasPositiveSize(Difficulty difficulty) {
        assertThat(difficulty.getSize()).isPositive();
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void everyDifficultyHasValidMineCount(Difficulty difficulty) {
        assertThat(difficulty.getMines()).isPositive();
        assertThat(difficulty.getMines())
                .isLessThan(difficulty.getSize() * difficulty.getSize());
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void everyDifficultyCreatesConfig(Difficulty difficulty) {
        GameConfig config = difficulty.createConfig();

        assertThat(config).isNotNull();
        assertThat(config.getColumns()).isEqualTo(difficulty.getSize());
        assertThat(config.getRows()).isEqualTo(difficulty.getSize());
        assertThat(config.getTotalBombs()).isEqualTo(difficulty.getMines());
    }

    /**
     * Tests that each difficulty level returns the correct display title.
     */
    @Test
    void testGetTitle() {
        assertEquals("Easy", Difficulty.EASY.getTitle());
        assertEquals("Medium", Difficulty.MEDIUM.getTitle());
        assertEquals("Hard", Difficulty.HARD.getTitle());
        assertEquals("Expert", Difficulty.EXPERT.getTitle());
        assertEquals("God", Difficulty.GOD.getTitle());
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void everyDifficultyHasPositiveTimeLimit(Difficulty difficulty) {
        assertThat(difficulty.getTimeMinutes()).isPositive();
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void everyDifficultyHasExpectedParameters(Difficulty difficulty) {
        switch (difficulty) {
            case EASY -> {
                assertThat(difficulty.getSize()).isEqualTo(8);
                assertThat(difficulty.getMines()).isEqualTo(10);
                assertThat(difficulty.getTimeMinutes()).isEqualTo(10);
            }
            case MEDIUM -> {
                assertThat(difficulty.getSize()).isEqualTo(12);
                assertThat(difficulty.getMines()).isEqualTo(20);
                assertThat(difficulty.getTimeMinutes()).isEqualTo(20);
            }
            case HARD -> {
                assertThat(difficulty.getSize()).isEqualTo(16);
                assertThat(difficulty.getMines()).isEqualTo(40);
                assertThat(difficulty.getTimeMinutes()).isEqualTo(40);
            }
            case EXPERT -> {
                assertThat(difficulty.getSize()).isEqualTo(20);
                assertThat(difficulty.getMines()).isEqualTo(80);
                assertThat(difficulty.getTimeMinutes()).isEqualTo(80);
            }
            case GOD -> {
                assertThat(difficulty.getSize()).isEqualTo(24);
                assertThat(difficulty.getMines()).isEqualTo(150);
                assertThat(difficulty.getTimeMinutes()).isEqualTo(150);
            }
        }
    }
}
