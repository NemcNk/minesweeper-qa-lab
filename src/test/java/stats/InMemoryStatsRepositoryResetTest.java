package stats;

import model.Difficulty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link InMemoryStatsRepository#reset()}.
 */
class InMemoryStatsRepositoryResetTest {

    private final InMemoryStatsRepository repository =
            new InMemoryStatsRepository();

    @Test
    void resetClearsEveryDifficulty() {

        repository.recordWin(Difficulty.EASY, 30);
        repository.recordLoss(Difficulty.EASY);
        repository.recordLoss(Difficulty.GOD);

        repository.reset();

        for (Difficulty difficulty : Difficulty.values()) {
            assertThat(repository.get(difficulty))
                    .isEqualTo(DifficultyStats.EMPTY);
        }
    }

    @Test
    void repositoryWorksAfterReset() {

        repository.recordWin(Difficulty.EASY, 10);
        repository.reset();
        repository.recordWin(Difficulty.EASY, 50);

        DifficultyStats easy = repository.get(Difficulty.EASY);

        assertThat(easy.wins()).isEqualTo(1);
        assertThat(easy.bestTimeSeconds().getAsInt()).isEqualTo(50);
    }

    @Test
    void resetOfAnEmptyRepositoryChangesNothing() {

        repository.reset();

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
    }
}