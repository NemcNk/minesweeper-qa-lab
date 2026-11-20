package stats;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DifficultyStatsTest {

    @Test
    void emptyHasNoResults() {

        assertThat(DifficultyStats.EMPTY.wins()).isZero();
        assertThat(DifficultyStats.EMPTY.losses()).isZero();
        assertThat(DifficultyStats.EMPTY.bestTimeSeconds()).isEmpty();
    }

    @Test
    void withWinKeepsFasterTime() {

        DifficultyStats stats = DifficultyStats.EMPTY
                .withWin(50)
                .withWin(70)
                .withWin(30);

        assertThat(stats.wins()).isEqualTo(3);
        assertThat(stats.bestTimeSeconds()).isEqualTo(OptionalInt.of(30));
    }

    @Test
    void withLossChangesOnlyLosses() {

        DifficultyStats stats = DifficultyStats.EMPTY.withWin(40).withLoss();

        assertThat(stats.wins()).isEqualTo(1);
        assertThat(stats.losses()).isEqualTo(1);
        assertThat(stats.bestTimeSeconds()).isEqualTo(OptionalInt.of(40));
    }

    @Test
    void withWinDoesNotModifyOriginal() {

        DifficultyStats original = DifficultyStats.EMPTY;

        original.withWin(10);

        assertThat(original).isEqualTo(DifficultyStats.EMPTY);
    }

    @Test
    void negativeSecondsAreRejected() {

        assertThatThrownBy(() -> DifficultyStats.EMPTY.withWin(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void zeroSecondsIsAccepted() {

        assertThat(DifficultyStats.EMPTY.withWin(0).bestTimeSeconds())
                .isEqualTo(OptionalInt.of(0));
    }

    @Test
    void constructorRejectsInvalidValues() {

        assertThatThrownBy(() ->
                new DifficultyStats(-1, 0, OptionalInt.empty()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                new DifficultyStats(0, -1, OptionalInt.empty()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                new DifficultyStats(0, 0, OptionalInt.of(-5)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
