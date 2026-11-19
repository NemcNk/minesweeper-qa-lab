package ui;

import java.util.function.IntConsumer;

/**
 * Countdown timer with pause and resume support.
 * Time is provided by a Ticker, and results are delivered through callbacks.
 */
final class CountdownTimer {

    private final int limitSeconds;
    private final Ticker ticker;
    private final IntConsumer onTick;
    private final Runnable onExpired;

    private int remainingSeconds;

    /**
     * Creates a countdown timer with the specified time limit.
     *
     * @param limitSeconds start value, must be positive
     * @param ticker       provides timer ticks
     * @param onTick       receives the remaining seconds after every tick
     *                     and after {@link #reset()}
     * @param onExpired    called once, after {@code onTick(0)}
     * @throws IllegalArgumentException if {@code limitSeconds} is not positive
     */
    CountdownTimer(
            int limitSeconds,
            Ticker ticker,
            IntConsumer onTick,
            Runnable onExpired
    ) {
        if (limitSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Time limit must be positive"
            );
        }

        this.limitSeconds = limitSeconds;
        this.ticker = ticker;
        this.onTick = onTick;
        this.onExpired = onExpired;
        this.remainingSeconds = limitSeconds;
    }

    void start() {
        ticker.start(this::tick);
    }

    void pause() {
        ticker.pause();
    }

    void resume() {
        ticker.resume();
    }

    void stop() {
        ticker.stop();
    }

    /** Stops the timer and restores the initial time. */
    void reset() {
        ticker.stop();
        remainingSeconds = limitSeconds;
        onTick.accept(remainingSeconds);
    }

    int getRemainingSeconds() {
        return remainingSeconds;
    }

    int getElapsedSeconds() {
        return limitSeconds - remainingSeconds;
    }

    private void tick() {
        remainingSeconds--;

        if (remainingSeconds <= 0) {
            remainingSeconds = 0;
            ticker.stop();

            onTick.accept(remainingSeconds);
            onExpired.run();

            return;
        }

        onTick.accept(remainingSeconds);
    }
}
