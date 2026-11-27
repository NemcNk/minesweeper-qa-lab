package logging;

import core.GameEventListener;
import model.Coord;
import model.Difficulty;
import model.GameState;

import java.util.Objects;

/** Logs game events using a {@link System.Logger}.
 *
 * <p>Game start and finish events are logged at INFO level,
 * while flag changes are logged at DEBUG level.</p>
 */
public final class GameEventLogger implements GameEventListener {

    /** Defines the name of the logger used for game event logging.
     */
    public static final String LOGGER_NAME = "minesweeper.game";

    /** Logger instance used to record game events. */
    private final System.Logger logger;

    /** Difficulty level associated with the current game. */
    private final Difficulty difficulty;

    /** Creates a game event logger using the default game logger.
     *
     * @param difficulty the difficulty level of the game.*/
    public GameEventLogger(Difficulty difficulty) {
        this(System.getLogger(LOGGER_NAME), difficulty);
    }

    /** Creates a game event logger with the specified logger.
     *
     * <p>This constructor has package-private visibility
     * to allow tests to provide a custom recording logger.</p>
     *
     * @param logger the logger used to record game events
     * @param difficulty the difficulty level of the game.*/
    GameEventLogger(System.Logger logger, Difficulty difficulty) {
        this.logger = Objects.requireNonNull(logger);
        this.difficulty = Objects.requireNonNull(difficulty);
    }

    /** Logs the start of a game.
     *
     * @param firstClick the coordinates of the first clicked cell
     */
    @Override
    public void gameStarted(Coord firstClick) {
        logger.log(System.Logger.Level.INFO, () ->
                "Game started: difficulty=" + difficulty
                        + ", firstClick=" + format(firstClick));
    }

    /** Logs a flag placement or removal.
     *
     * @param coord the coordinates of the affected cell
     * @param flagged {@code true} if a flag was placed,
     *                {@code false} if a flag was removed
     */
    @Override
    public void flagToggled(Coord coord, boolean flagged) {
        logger.log(System.Logger.Level.DEBUG, () ->
                (flagged ? "Flag placed" : "Flag removed")
                        + ": difficulty=" + difficulty
                        + ", cell=" + format(coord));
    }

    /** Logs the completion of a game.
     * @param result the final state of the game.*/
    @Override
    public void gameFinished(GameState result) {
        logger.log(System.Logger.Level.INFO, () ->
                "Game finished: difficulty=" + difficulty
                        + ", result=" + result);
    }

    /** Converts cell coordinates into a human-readable string.
     *
     * @param coord the coordinates to format
     * @return the formatted coordinates in {@code (x, y)} notation
     */
    private static String format(Coord coord) {
        return "(" + coord.getX() + ", " + coord.getY() + ")";
    }
}

