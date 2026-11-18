package stats;

import java.util.OptionalInt;

/**
 * An unvarying set of statistical information of a certain complexity
 */
public record DifficultyStats(
        int wins,
        int losses,
        OptionalInt bestTimeSeconds
) {

    public static final DifficultyStats EMPTY =
            new DifficultyStats(0, 0, OptionalInt.empty());
}