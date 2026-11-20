package core;

import model.Coord;
import model.GameState;

/**
 * Receives key events of a {@link Game}. All methods are optional: the
 * default implementations do nothing, so a listener overrides only what it
 * needs. The core stays free of logging and UI code; the application (or a
 * test) plugs a listener in.
 */
public interface GameEventListener {

    /** A listener that ignores every event. */
    GameEventListener NONE = new GameEventListener() { };

    /**
     * The first accepted left click: the bombs are placed and the game
     * has started. Reported once per game.
     *
     * @param firstClick the clicked cell
     */
    default void gameStarted(Coord firstClick) {
        // no-op by default
    }

    /**
     * A flag was placed or removed. Clicks that change nothing (an opened
     * cell, the flag limit reached) are not reported.
     *
     * @param coord   the cell
     * @param flagged {@code true} if the flag was placed,
     *                {@code false} if it was removed
     */
    default void flagToggled(Coord coord, boolean flagged) {
        // no-op by default
    }

    /**
     * The game ended. Reported once per game.
     *
     * @param result {@link GameState#WON}, {@link GameState#LOST_BOMB}
     *               or {@link GameState#TIMEOUT}
     */
    default void gameFinished(GameState result) {
        // no-op by default
    }
}
