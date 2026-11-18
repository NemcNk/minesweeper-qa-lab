package core;

import model.Coord;
import model.GameConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Test helpers: build a game from an ASCII map.
 */
final class TestBoards {

    private TestBoards() {
    }

    /**
     * Builds a game with a fixed bomb layout. {@code '*'} is a bomb, any
     * other character is an empty cell. The layout ignores the safe area,
     * so the test chooses a sensible first click itself.
     */
    static Game game(String... rows) {

        int height = rows.length;
        int width = rows[0].length();

        Set<Coord> bombs = new HashSet<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (rows[y].charAt(x) == '*') {
                    bombs.add(new Coord(x, y));
                }
            }
        }

        GameConfig config = new GameConfig(width, height, bombs.size());

        return new Game(config, (geometry, count, safe) -> bombs);
    }

    static Coord at(int x, int y) {
        return new Coord(x, y);
    }
}
