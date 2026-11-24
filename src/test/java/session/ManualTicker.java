package session;

/**
 * Test double: time moves only when the test calls {@link #tick()}.
 */
final class ManualTicker implements Ticker {

    private Runnable onTick;
    private boolean running;

    @Override
    public void start(Runnable onTick) {
        this.onTick = onTick;
        this.running = true;
    }

    @Override
    public void pause() {
        running = false;
    }

    @Override
    public void resume() {
        running = onTick != null;
    }

    @Override
    public void stop() {
        running = false;
    }

    boolean isRunning() {
        return running;
    }

    /** Simulates one second; does nothing while not running. */
    void tick() {

        if (running) {
            onTick.run();
        }
    }

    void tick(int seconds) {

        for (int i = 0; i < seconds; i++) {
            tick();
        }
    }
}
