package stats;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Test logger: keeps "LEVEL message" lines instead of printing them.
 */
final class RecordingLogger implements System.Logger {

    final List<String> lines = new ArrayList<>();

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
