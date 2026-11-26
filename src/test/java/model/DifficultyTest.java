package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link Difficulty} enum.
 */
public class DifficultyTest {

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
}
