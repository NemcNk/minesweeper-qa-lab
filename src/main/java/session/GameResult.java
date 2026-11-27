package session;

import java.util.Objects;

import model.GameState;

/** Outcome of a finished game.
 * @param state          how the game ended: WON, LOST_BOMB or TIMEOUT
 * @param elapsedSeconds game time in seconds, not negative
 */
public record GameResult(GameState state, int elapsedSeconds) {

    /** Validates that the result describes a finished game.
     */
    public GameResult {
        Objects.requireNonNull(state);

        if (state == GameState.PLAYING) {
            throw new IllegalArgumentException(
                    "A running game has no result"
            );
        }

        if (elapsedSeconds < 0) {
            throw new IllegalArgumentException(
                    "Elapsed time must not be negative"
            );
        }
    }

    /** Tells whether the player won.
     * @return true for a win, false for a bomb or a timeout
     */
    public boolean isWon() {
        return state == GameState.WON;
    }
}
