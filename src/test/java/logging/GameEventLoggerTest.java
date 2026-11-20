package logging;

import model.Coord;
import model.Difficulty;
import model.GameState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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

    // Records "LEVEL message" lines instead of printing them.
    private static final class RecordingLogger implements System.Logger {

        private final List<String> lines = new ArrayList<>();

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public boolean isLoggable(Level level) {
            return true;
        }

        @Override
        public void log(Level level, ResourceBundle bundle,
                        String msg, Throwable thrown) {
            lines.add(level + " " + msg);
        }

        @Override
        public void log(Level level, ResourceBundle bundle,
                        String format, Object... params) {
            lines.add(level + " " + format);
        }
    }
}
