package stats;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

import model.Difficulty;

/**
 * Statistics stored in memory while the application is running.
 */
public final class InMemoryStatsRepository implements StatsRepository {

    // Stores statistics separately for each difficulty level.
    private final Map<Difficulty, DifficultyStats> stats =
            new EnumMap<>(Difficulty.class);

    @Override
    public void recordWin(Difficulty difficulty, int seconds) {
        // Get the current statistics for the selected difficulty.
        DifficultyStats current = get(difficulty);

        // Keep the fastest completion time.
        int best = current.bestTimeSeconds().isPresent()
                ? Math.min(current.bestTimeSeconds().getAsInt(), seconds)
                : seconds;

        // Update wins and the best completion time.
        stats.put(difficulty, new DifficultyStats(
                current.wins() + 1,
                current.losses(),
                OptionalInt.of(best)
        ));
    }

    @Override
    public void recordLoss(Difficulty difficulty) {
        // Get the current statistics for the selected difficulty.
        DifficultyStats current = get(difficulty);

        // Increment losses without changing wins or the best time.
        stats.put(difficulty, new DifficultyStats(
                current.wins(),
                current.losses() + 1,
                current.bestTimeSeconds()
        ));
    }

    @Override
    public DifficultyStats get(Difficulty difficulty) {
        // Ensure that the difficulty argument is not null.
        Objects.requireNonNull(difficulty);

        // Return empty statistics if no records exist yet.
        return stats.getOrDefault(difficulty, DifficultyStats.EMPTY);
    }
}

