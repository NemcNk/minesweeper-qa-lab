package stats;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * Immutable statistics snapshot for one difficulty.
 *
 * @param wins            number of wins, not negative
 * @param losses          number of losses, not negative
 * @param bestTimeSeconds fastest win in seconds, empty if there was no win
 */
public record DifficultyStats(
        int wins,
        int losses,
        OptionalInt bestTimeSeconds
) {

    public static final DifficultyStats EMPTY =
            new DifficultyStats(0, 0, OptionalInt.empty());

    /**
     * Validates the values, so a damaged data file cannot create
     * impossible statistics.
     */
    public DifficultyStats {
        Objects.requireNonNull(bestTimeSeconds);

        if (wins < 0 || losses < 0) {
            throw new IllegalArgumentException(
                    "Wins and losses must not be negative"
            );
        }

        if (bestTimeSeconds.isPresent() && bestTimeSeconds.getAsInt() < 0) {
            throw new IllegalArgumentException(
                    "Best time must not be negative"
            );
        }
    }

    /**
     * Returns statistics after one more win. The best time changes only
     * if this win is faster.
     *
     * @param seconds completion time, must not be negative
     * @throws IllegalArgumentException if {@code seconds} is negative
     */
    public DifficultyStats withWin(int seconds) {

        if (seconds < 0) {
            throw new IllegalArgumentException(
                    "Seconds must not be negative: " + seconds
            );
        }

        int best = bestTimeSeconds.isPresent()
                ? Math.min(bestTimeSeconds.getAsInt(), seconds)
                : seconds;

        return new DifficultyStats(wins + 1, losses, OptionalInt.of(best));
    }

    /**
     * Returns statistics after one more loss. Wins and the best time
     * do not change.
     */
    public DifficultyStats withLoss() {
        return new DifficultyStats(wins, losses + 1, bestTimeSeconds);
    }
}
