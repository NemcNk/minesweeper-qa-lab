package stats;

import model.Difficulty;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryStatsRepositoryTest {

    private final InMemoryStatsRepository repository =
            new InMemoryStatsRepository();

    @Test
    void emptyRepositoryReturnsEmptyStats() {

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
    }

    @Test
    void firstWinSetsBestTime() {

        repository.recordWin(Difficulty.EASY, 50);

        DifficultyStats stats = repository.get(Difficulty.EASY);

        assertThat(stats.wins()).isEqualTo(1);
        assertThat(stats.losses()).isZero();
        assertThat(stats.bestTimeSeconds()).isEqualTo(OptionalInt.of(50));
    }

    @Test
    void bestTimeUpdatesOnlyWhenImproved() {

        repository.recordWin(Difficulty.EASY, 50);
        repository.recordWin(Difficulty.EASY, 70);   // slower: ignored

        assertThat(repository.get(Difficulty.EASY).bestTimeSeconds())
                .isEqualTo(OptionalInt.of(50));

        repository.recordWin(Difficulty.EASY, 30);   // faster: replaces

        assertThat(repository.get(Difficulty.EASY).bestTimeSeconds())
                .isEqualTo(OptionalInt.of(30));
        assertThat(repository.get(Difficulty.EASY).wins()).isEqualTo(3);
    }

    @Test
    void equalTimeKeepsBestTime() {

        repository.recordWin(Difficulty.EASY, 40);
        repository.recordWin(Difficulty.EASY, 40);

        assertThat(repository.get(Difficulty.EASY).bestTimeSeconds())
                .isEqualTo(OptionalInt.of(40));
    }

    @Test
    void lossIncrementsLossesOnly() {

        repository.recordWin(Difficulty.EASY, 40);
        repository.recordLoss(Difficulty.EASY);
        repository.recordLoss(Difficulty.EASY);

        DifficultyStats stats = repository.get(Difficulty.EASY);

        assertThat(stats.wins()).isEqualTo(1);
        assertThat(stats.losses()).isEqualTo(2);
        assertThat(stats.bestTimeSeconds()).isEqualTo(OptionalInt.of(40));
    }

    @Test
    void lossWithoutWinsLeavesBestTimeEmpty() {

        repository.recordLoss(Difficulty.MEDIUM);

        assertThat(repository.get(Difficulty.MEDIUM).bestTimeSeconds())
                .isEmpty();
    }

    @Test
    void difficultiesAreIndependent() {

        repository.recordWin(Difficulty.EASY, 20);
        repository.recordLoss(Difficulty.HARD);

        assertThat(repository.get(Difficulty.EASY).losses()).isZero();
        assertThat(repository.get(Difficulty.HARD).wins()).isZero();
        assertThat(repository.get(Difficulty.HARD).bestTimeSeconds())
                .isEmpty();
        assertThat(repository.get(Difficulty.MEDIUM))
                .isEqualTo(DifficultyStats.EMPTY);
    }

    @Test
    void nullDifficultyIsRejected() {

        assertThatThrownBy(() -> repository.get(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void negativeSecondsAreRejected() {

        assertThatThrownBy(() -> repository.recordWin(Difficulty.EASY, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectedWinDoesNotChangeStats() {

        repository.recordWin(Difficulty.EASY, 40);

        assertThatThrownBy(() -> repository.recordWin(Difficulty.EASY, -5))
                .isInstanceOf(IllegalArgumentException.class);

        DifficultyStats stats = repository.get(Difficulty.EASY);

        assertThat(stats.wins()).isEqualTo(1);
        assertThat(stats.bestTimeSeconds()).isEqualTo(OptionalInt.of(40));
    }

    @Test
    void zeroSecondsIsAccepted() {

        repository.recordWin(Difficulty.EASY, 0);

        assertThat(repository.get(Difficulty.EASY).bestTimeSeconds())
                .isEqualTo(OptionalInt.of(0));
    }
}
