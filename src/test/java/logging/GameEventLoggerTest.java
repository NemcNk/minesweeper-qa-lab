package logging;

import model.Coord;
import model.Difficulty;
import model.GameState;
import org.junit.jupiter.api.Test;
import support.RecordingLogger;

import static org.assertj.core.api.Assertions.assertThat;

class GameEventLoggerTest {

    private final RecordingLogger recorder = new RecordingLogger();
    private final GameEventLogger logger =
            new GameEventLogger(recorder, Difficulty.EASY);

    @Test
    void startIsLoggedAtInfo() {

        logger.gameStarted(new Coord(3, 4));

        assertThat(recorder.lines).containsExactly(
                "INFO Game started: difficulty=EASY, firstClick=(3, 4)"
        );
    }

    @Test
    void flagsAreLoggedAtDebug() {

        logger.flagToggled(new Coord(0, 1), true);
        logger.flagToggled(new Coord(0, 1), false);

        assertThat(recorder.lines).containsExactly(
                "DEBUG Flag placed: difficulty=EASY, cell=(0, 1)",
                "DEBUG Flag removed: difficulty=EASY, cell=(0, 1)"
        );
    }

    @Test
    void finishIsLoggedAtInfo() {

        logger.gameFinished(GameState.LOST_BOMB);

        assertThat(recorder.lines).containsExactly(
                "INFO Game finished: difficulty=EASY, result=LOST_BOMB"
        );
    }
}
