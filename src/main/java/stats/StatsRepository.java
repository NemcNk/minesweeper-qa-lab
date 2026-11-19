package stats;

import model.Difficulty;

/**
 * Game results repository. UI depends on this interface,
 * not on a specific implementation (memory, file, database).
 */
public interface StatsRepository {

    void recordWin(Difficulty difficulty, int seconds);

    void recordLoss(Difficulty difficulty);

    DifficultyStats get(Difficulty difficulty);
}

