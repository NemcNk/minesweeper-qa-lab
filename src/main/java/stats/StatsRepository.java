package stats;

import model.Difficulty;

/**
 * Game result repository. UI depends on this interface,
 * not on a specific implementation (memory, file, database).
 */
public interface StatsRepository {
    /**
     * Records a win and updates the best time if this one is faster.
     *
     * @param difficulty difficulty of the finished game, must not be null
     * @param seconds    completion time, must not be negative
     * @throws NullPointerException     if {@code difficulty} is null
     * @throws IllegalArgumentException if {@code seconds} is negative
     */
    void recordWin(Difficulty difficulty, int seconds);

    void recordLoss(Difficulty difficulty);

    DifficultyStats get(Difficulty difficulty);
}

