package session;

import core.Game;
import model.Difficulty;

/** Creates the {@link Game} for a session.
 * <p>The session does not call {@code new Game(...)} itself, so the source
 * of the bomb layout is decided by whoever builds the session: the
 * application uses {@link RandomGameFactory}, tests supply a fixed layout.
 */
@FunctionalInterface
public interface GameFactory {

    /** Creates a new game that has not been started yet.
     * @param difficulty selected difficulty
     * @return a new game in the PLAYING state with no bombs placed
     */
    Game create(Difficulty difficulty);
}
