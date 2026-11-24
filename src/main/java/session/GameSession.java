package session;

import java.util.Objects;

import core.BoardReader;
import core.Game;
import model.Coord;
import model.Difficulty;
import model.GameState;
import stats.StatsRepository;

/**
 * Rules of one play session that do not depend on JavaFX: the countdown,
 * pause and resume, restart, and recording the result exactly once.
 *
 * <p>The view forwards user input to the session and redraws itself from
 * the {@link GameSessionListener} callbacks. Time comes from a
 * {@link Ticker}, the game from a {@link GameFactory} and the statistics
 * go to a {@link StatsRepository}; all three are supplied by the caller,
 * so the whole session can be tested with a manual ticker.
 *
 * <p>Use from a single thread (the JavaFX application thread).
 */
public final class GameSession {

    private final Difficulty difficulty;
    private final StatsRepository stats;
    private final Game game;
    private final CountdownTimer timer;

    private GameSessionListener listener = GameSessionListener.NONE;

    private boolean paused;
    private boolean finished;
    private boolean disposed;

    /**
     * Creates a session with the time limit of the difficulty.
     *
     * @param difficulty selected difficulty
     * @param factory    creates the game
     * @param stats      receives the result of the game
     * @param ticker     provides one-second ticks
     */
    public GameSession(
            Difficulty difficulty,
            GameFactory factory,
            StatsRepository stats,
            Ticker ticker
    ) {
        this(
                difficulty,
                factory,
                stats,
                ticker,
                Objects.requireNonNull(difficulty).getTimeMinutes() * 60
        );
    }

    /**
     * Creates a session with an explicit time limit. A short limit makes
     * the timeout easy to check.
     *
     * @param difficulty       selected difficulty
     * @param factory          creates the game
     * @param stats            receives the result of the game
     * @param ticker           provides one-second ticks
     * @param timeLimitSeconds time limit, must be positive
     * @throws IllegalArgumentException if the limit is not positive
     */
    public GameSession(
            Difficulty difficulty,
            GameFactory factory,
            StatsRepository stats,
            Ticker ticker,
            int timeLimitSeconds
    ) {
        this.difficulty = Objects.requireNonNull(difficulty);
        this.stats = Objects.requireNonNull(stats);
        this.game = Objects.requireNonNull(
                Objects.requireNonNull(factory).create(difficulty),
                "The factory returned no game"
        );

        this.timer = new CountdownTimer(
                timeLimitSeconds,
                Objects.requireNonNull(ticker),
                this::onTick,
                this::onTimeExpired
        );
    }

    /**
     * Sets the receiver of changes. Only one listener is supported.
     *
     * @param newListener the view
     */
    public void setListener(GameSessionListener newListener) {
        listener = Objects.requireNonNull(newListener);
    }

    // State

    /**
     * Returns the board for rendering.
     *
     * @return read-only view of the board
     */
    public BoardReader getBoard() {
        return game;
    }

    /**
     * Returns the state of the game.
     *
     * @return current game state
     */
    public GameState getState() {
        return game.getState();
    }

    /**
     * Returns the number of bombs without a flag.
     *
     * @return remaining bomb count
     */
    public int getRemainingBombs() {
        return game.getRemainingBombs();
    }

    /**
     * Returns the seconds left until the timeout.
     *
     * @return remaining seconds
     */
    public int getRemainingSeconds() {
        return timer.getRemainingSeconds();
    }

    /**
     * Returns the seconds played so far.
     *
     * @return elapsed seconds
     */
    public int getElapsedSeconds() {
        return timer.getElapsedSeconds();
    }

    /**
     * Tells whether the game is paused.
     *
     * @return true while paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Tells whether pausing is allowed: the first move has been made and
     * the game is still running.
     *
     * @return true if {@link #togglePause()} has an effect
     */
    public boolean isPauseAvailable() {
        return !disposed
                && !finished
                && game.getState() == GameState.PLAYING
                && game.isStarted();
    }

    // Input

    /**
     * Handles a left click. The first accepted click starts the timer.
     * Ignored while paused, after the game has ended and after
     * {@link #dispose()}.
     *
     * @param coord clicked cell
     */
    public void leftClick(Coord coord) {
        if (!acceptsInput()) {
            return;
        }

        boolean wasStarted = game.isStarted();

        game.pressLeftButton(coord);

        if (!wasStarted && game.isStarted()) {
            timer.start();
            listener.pauseAvailabilityChanged(true);
        }

        afterMove();
    }

    /**
     * Handles a right click (flag). Does not start the timer. Ignored in
     * the same situations as {@link #leftClick(Coord)}.
     *
     * @param coord clicked cell
     */
    public void rightClick(Coord coord) {
        if (!acceptsInput()) {
            return;
        }

        game.pressRightButton(coord);

        afterMove();
    }

    /**
     * Pauses a running game or resumes a paused one. Does nothing before
     * the first move and after the game has ended.
     */
    public void togglePause() {
        if (!isPauseAvailable()) {
            return;
        }

        paused = !paused;

        if (paused) {
            timer.pause();
        } else {
            timer.resume();
        }

        listener.pauseChanged(paused);
    }

    /**
     * Starts a new game on the same board: the timer, the pause and the
     * recorded-result flag are reset. A game abandoned before it ended is
     * not counted in the statistics.
     */
    public void restart() {
        if (disposed) {
            return;
        }

        // Reports the full time through onTick.
        timer.reset();

        paused = false;
        finished = false;

        game.start();

        listener.pauseChanged(false);
        listener.pauseAvailabilityChanged(false);
        listener.boardChanged();
        listener.minesChanged(game.getRemainingBombs());
        listener.restarted();
    }

    /**
     * Stops the timer and ignores all further input. Called when the
     * player leaves the game screen. A game left unfinished is not
     * counted in the statistics.
     */
    public void dispose() {
        disposed = true;

        timer.stop();
    }

    // Internals

    private boolean acceptsInput() {
        return !disposed
                && !paused
                && !finished
                && game.getState() == GameState.PLAYING;
    }

    private void afterMove() {
        listener.boardChanged();
        listener.minesChanged(game.getRemainingBombs());

        checkFinished();
    }

    private void onTick(int remainingSeconds) {
        listener.timeChanged(remainingSeconds);
    }

    private void onTimeExpired() {
        game.timeout();

        listener.boardChanged();

        checkFinished();
    }

    private void checkFinished() {
        GameState state = game.getState();

        if (finished || state == GameState.PLAYING) {
            return;
        }

        finish(state);
    }

    // The result is stored once: the "finished" flag blocks a second call
    // until the next restart.
    private void finish(GameState state) {
        finished = true;

        timer.stop();

        GameResult result = new GameResult(
                state,
                timer.getElapsedSeconds()
        );

        if (result.isWon()) {
            stats.recordWin(difficulty, result.elapsedSeconds());
        } else {
            stats.recordLoss(difficulty);
        }

        listener.pauseAvailabilityChanged(false);
        listener.finished(result);
    }
}
