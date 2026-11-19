package ui;

/**
 * Source of one-second ticks. Abstracts the clock so that timer logic
 * can be tested without JavaFX and without real waiting.
 */
interface Ticker {

    /** Starts ticking from scratch; a running ticker is restarted. */
    void start(Runnable onTick);

    /** Suspends ticking; does nothing if not running. */
    void pause();

    /** Continues after {@link #pause()}. */
    void resume();

    /** Stops ticking. */
    void stop();
}

