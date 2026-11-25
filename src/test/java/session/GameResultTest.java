package session;

import model.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameResultTest {

    @Test
    void shouldThrowExceptionWhenStateIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new GameResult(null, 10)
        );
    }

    @Test
    void shouldThrowExceptionWhenGameIsPlaying() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameResult(GameState.PLAYING, 10)
        );
    }

    @Test
    void shouldThrowExceptionWhenElapsedSecondsIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameResult(GameState.WON, -1)
        );
    }

    @Test
    void shouldAllowZeroElapsedSeconds() {
        GameResult result = new GameResult(GameState.WON, 0);

        assertEquals(GameState.WON, result.state());
        assertEquals(0, result.elapsedSeconds());
    }

    @Test
    void shouldAllowAllFinishedGameStates() {
        for (GameState state : GameState.values()) {
            if (state != GameState.PLAYING) {
                assertDoesNotThrow(
                        () -> new GameResult(state, 10)
                );
            }
        }
    }
}