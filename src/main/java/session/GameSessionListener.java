package session;

/** Receives changes of a {@link GameSession}. The view implements it to
 * redraw itself. All methods are optional and do nothing by default.
 */
public interface GameSessionListener {

    /** A listener that ignores every change. */
    GameSessionListener NONE = new GameSessionListener() { };

    /** The cells of the board changed. */
    default void boardChanged() {
        // no-op by default
    }

    /** The number of bombs without a flag changed.
     * @param remaining bombs minus flags.*/
    default void minesChanged(int remaining) {
        // no-op by default
    }

    /** The displayed time changed.
     * @param remainingSeconds seconds left until the timeout
     */
    default void timeChanged(int remainingSeconds) {
        // no-op by default
    }

    /** The game was paused or resumed.
     * @param paused true if the game is now paused.*/
    default void pauseChanged(boolean paused) {
        // no-op by default
    }

    /** The pause button became available or unavailable.
     * @param available true if pausing is allowed now.*/
    default void pauseAvailabilityChanged(boolean available) {
        // no-op by default
    }

    /** The game ended. Reported once per game, after the result has been
     * stored in the statistics.
     * @param result how the game ended.*/
    default void finished(GameResult result) {
        // no-op by default
    }

    /** The game was restarted and everything is back to the initial state. */
    default void restarted() {
        // no-op by default
    }
}
