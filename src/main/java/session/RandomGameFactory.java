package session;

import core.Game;
import logging.GameEventLogger;
import model.Difficulty;

/**
 * Production {@link GameFactory}: random bomb layout, game events are
 * written to the log.
 */
public final class RandomGameFactory implements GameFactory {

    @Override
    public Game create(Difficulty difficulty) {
        return new Game(
                difficulty.createConfig(),
                new GameEventLogger(difficulty)
        );
    }
}
