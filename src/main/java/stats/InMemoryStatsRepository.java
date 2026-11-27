package stats;

import model.Difficulty;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Statistics stored in memory while the application is running.
 */
public final class InMemoryStatsRepository implements StatsRepository {

    /** Stores statistics separately for each difficulty level. */
    private final Map<Difficulty, DifficultyStats> stats =
            new EnumMap<>(Difficulty.class);

    @Override
    public void recordWin(Difficulty difficulty, int seconds) {
        // withWin validates the time before anything is stored.
        stats.put(difficulty, get(difficulty).withWin(seconds));
    }

    @Override
    public void recordLoss(Difficulty difficulty) {
        stats.put(difficulty, get(difficulty).withLoss());
    }

    @Override
    public DifficultyStats get(Difficulty difficulty) {
        Objects.requireNonNull(difficulty);

        return stats.getOrDefault(difficulty, DifficultyStats.EMPTY);
    }

    @Override
    public void reset() {
        stats.clear();
    }
}
