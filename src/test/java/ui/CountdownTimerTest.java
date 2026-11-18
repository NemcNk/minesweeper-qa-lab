package ui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CountdownTimerTest {

    private final ManualTickerTest ticker = new ManualTickerTest();
    private final List<Integer> ticks = new ArrayList<>();
    private int expiredCount;

    private CountdownTimer timer(int limitSeconds) {
        return new CountdownTimer(
                limitSeconds,
                ticker,
                ticks::add,
                () -> expiredCount++
        );
    }

    @Test
    void limitMustBePositive() {

        assertThatThrownBy(() -> timer(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reportsRemainingTimeAfterEveryTick() {

        CountdownTimer timer = timer(3);

        timer.start();
        ticker.tick(2);

        assertThat(ticks).containsExactly(2, 1);
        assertThat(timer.getRemainingSeconds()).isEqualTo(1);
        assertThat(timer.getElapsedSeconds()).isEqualTo(2);
        assertThat(expiredCount).isZero();
    }

    @Test
    void expiresOnceAtZeroAndStopsTicker() {

        CountdownTimer timer = timer(2);

        timer.start();
        ticker.tick(5);

        assertThat(ticks).containsExactly(1, 0);
        assertThat(expiredCount).isEqualTo(1);
        assertThat(ticker.isRunning()).isFalse();
        assertThat(timer.getRemainingSeconds()).isZero();
    }

    @Test
    void pausedTimerDoesNotCount() {

        CountdownTimer timer = timer(10);

        timer.start();
        timer.pause();
        ticker.tick(3);

        assertThat(ticks).isEmpty();

        timer.resume();
        ticker.tick();

        assertThat(ticks).containsExactly(9);
    }

    @Test
    void resetRestoresInitialTimeAndNotifies() {

        CountdownTimer timer = timer(10);

        timer.start();
        ticker.tick(4);
        timer.reset();

        assertThat(timer.getRemainingSeconds()).isEqualTo(10);
        assertThat(ticks).last().isEqualTo(10);
        assertThat(ticker.isRunning()).isFalse();
    }

    @Test
    void stopPreventsFurtherTicks() {

        CountdownTimer timer = timer(10);

        timer.start();
        timer.stop();
        ticker.tick(3);

        assertThat(ticks).isEmpty();
    }
}
