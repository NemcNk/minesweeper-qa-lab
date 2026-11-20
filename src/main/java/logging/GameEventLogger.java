package logging;

import core.GameEventListener;
import model.Coord;
import model.Difficulty;
import model.GameState;

import java.util.Objects;

/**
 * Writes game events to a {@link System.Logger}. Starts and finishes are
 * logged at INFO, flags at DEBUG (hidden by the default configuration).
 */
public final class GameEventLogger implements GameEventListener {

    /** Name of the logger, usable in logging configuration. */
    public static final String LOGGER_NAME = "minesweeper.game";

    private final System.Logger logger;
    private final Difficulty difficulty;

    /**
     * Creates a listener that logs to the default game logger.
     *
     * @param difficulty difficulty of the game, added to every message
     */
    public GameEventLogger(Difficulty difficulty) {
        this(System.getLogger(LOGGER_NAME), difficulty);
    }

    // Package-private for tests: the caller supplies a recording logger
    GameEventLogger(System.Logger logger, Difficulty difficulty) {
        this.logger = Objects.requireNonNull(logger);
        this.difficulty = Objects.requireNonNull(difficulty);
    }

    @Override
    public void gameStarted(Coord firstClick) {
        logger.log(System.Logger.Level.INFO, () ->
                "Game started: difficulty=" + difficulty
                        + ", firstClick=" + format(firstClick));
    }

    @Override
    public void flagToggled(Coord coord, boolean flagged) {
        logger.log(System.Logger.Level.DEBUG, () ->
                (flagged ? "Flag placed" : "Flag removed")
                        + ": difficulty=" + difficulty
                        + ", cell=" + format(coord));
    }

    @Override
    public void gameFinished(GameState result) {
        logger.log(System.Logger.Level.INFO, () ->
                "Game finished: difficulty=" + difficulty
                        + ", result=" + result);
    }

    private static String format(Coord coord) {
        return "(" + coord.getX() + ", " + coord.getY() + ")";
    }
}
