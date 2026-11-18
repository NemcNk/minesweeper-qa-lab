package stats;

import model.Difficulty;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Statistics in memory: lives while the app is running.
 */
public final class InMemoryStatsRepository implements StatsRepository {

    private final Map<Difficulty, DifficultyStats> stats =
            new EnumMap<>(Difficulty.class);

    @Override
    public void recordWin(Difficulty difficulty, int seconds) {

        DifficultyStats current = get(difficulty);

        int best = current.bestTimeSeconds().isPresent()
                ? Math.min(current.bestTimeSeconds().getAsInt(), seconds)
                : seconds;

        stats.put(difficulty, new DifficultyStats(
                current.wins() + 1,
                current.losses(),
                OptionalInt.of(best)
        ));
    }

    @Override
    public void recordLoss(Difficulty difficulty) {

        DifficultyStats current = get(difficulty);

        stats.put(difficulty, new DifficultyStats(
                current.wins(),
                current.losses() + 1,
                current.bestTimeSeconds()
        ));
    }

    @Override
    public DifficultyStats get(Difficulty difficulty) {

        Objects.requireNonNull(difficulty);

        return stats.getOrDefault(difficulty, DifficultyStats.EMPTY);
    }
}
