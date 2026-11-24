package core;

import model.Difficulty;
import session.GameFactory;

/**
 * Test {@link GameFactory} that builds every game from the same ASCII map
 * ({@code '*'} is a bomb). It lives in package {@code core} because the
 * constructor that accepts a bomb layout is package-private, so the public
 * API of {@code core} does not have to grow for the tests.
 */
public final class FixedLayoutGameFactory implements GameFactory {

    private final String[] rows;

    /**
     * Creates a factory for the given layout.
     *
     * @param rows board rows, see {@code TestBoards.game}
     */
    public FixedLayoutGameFactory(String... rows) {
        this.rows = rows.clone();
    }

    @Override
    public Game create(Difficulty difficulty) {
        return TestBoards.game(rows);
    }
}
