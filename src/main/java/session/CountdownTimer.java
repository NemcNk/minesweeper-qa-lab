package session;

import java.util.function.IntConsumer;

/** Countdown timer with pause and resume support.
 * Time is provided by a Ticker, and results are delivered through callbacks.
 */
final class CountdownTimer {

    /** Initial time limit in seconds. */
    private final int limitSeconds;

    /** Provides timer ticks and controls the timer lifecycle. */
    private final Ticker ticker;

    /** Callback invoked when the remaining time changes. */
    private final IntConsumer onTick;

    /** Callback invoked when the countdown reaches zero. */
    private final Runnable onExpired;

    /** Current remaining time in seconds. */
    private int remainingSeconds;

    /** Creates a countdown timer with the specified time limit.
     *
     * @param limitSeconds start value, must be positive
     * @param ticker provides timer ticks
     * @param onTick receives the remaining seconds after every tick
     *               and after {@link #reset()}
     * @param onExpired called once, after {@code onTick(0)}
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

    /** Starts the countdown timer. */
    void start() {
        ticker.start(this::tick);
    }

    /** Pauses the countdown timer. */
    void pause() {
        ticker.pause();
    }

    /** Resumes the paused countdown timer. */
    void resume() {
        ticker.resume();
    }

    /** Stops the countdown timer. */
    void stop() {
        ticker.stop();
    }

    /** Stops the timer and restores the initial time. */
    void reset() {
        ticker.stop();
        remainingSeconds = limitSeconds;
        onTick.accept(remainingSeconds);
    }

    /** Returns the remaining time.
     * @return remaining time in seconds.*/
    int getRemainingSeconds() {
        return remainingSeconds;
    }

    /** Returns the elapsed time.
     * @return elapsed time in seconds.*/
    int getElapsedSeconds() {
        return limitSeconds - remainingSeconds;
    }

    /** Updates the countdown after a timer tick. */
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

