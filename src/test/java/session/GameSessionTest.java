package session;

import core.FixedLayoutGameFactory;
import model.CellContent;
import model.CellState;
import model.Coord;
import model.Difficulty;
import model.GameState;
import org.junit.jupiter.api.Test;
import stats.DifficultyStats;
import stats.StatsRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the session rules without JavaFX: time comes from a
 * {@link ManualTicker}, the bomb layout is fixed and the statistics
 * repository is a recording stub.
 *
 * <p>The 2x2 board has one bomb at (0,0); the other three cells show 1.
 * The 3x3 board has one bomb at (0,0); a click on (2,2) opens everything.
 */
class GameSessionTest {

    private static final int LIMIT = 100;

    private final ManualTicker ticker = new ManualTicker();
    private final RecordingStats stats = new RecordingStats();
    private final RecordingListener listener = new RecordingListener();

    private static Coord at(int x, int y) {
        return new Coord(x, y);
    }

    private GameSession session2x2() {
        return session(LIMIT, "*.", "..");
    }

    private GameSession session3x3() {
        return session(LIMIT, "*..", "...", "...");
    }

    private GameSession session(int limitSeconds, String... rows) {
        GameSession session = new GameSession(
                Difficulty.EASY,
                new FixedLayoutGameFactory(rows),
                stats,
                ticker,
                limitSeconds
        );

        session.setListener(listener);

        return session;
    }

    private static CellState stateOf(GameSession session, int x, int y) {
        return session.getBoard().getCell(at(x, y)).getState();
    }

    // construction

    @Test
    void usesTimeLimitOfTheDifficultyByDefault() {

        GameSession session = new GameSession(
                Difficulty.EASY,
                new FixedLayoutGameFactory("*.", ".."),
                stats,
                ticker
        );

        assertThat(session.getRemainingSeconds())
                .isEqualTo(Difficulty.EASY.getTimeMinutes() * 60);
    }

    @Test
    void rejectsNonPositiveTimeLimit() {

        assertThatThrownBy(() -> session(0, "*.", ".."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsFactoryThatReturnsNoGame() {

        assertThatThrownBy(() -> new GameSession(
                Difficulty.EASY, difficulty -> null, stats, ticker
        )).isInstanceOf(NullPointerException.class);
    }

    // timer

    @Test
    void timerDoesNotRunBeforeFirstLeftClick() {

        GameSession session = session2x2();

        session.rightClick(at(1, 0));
        session.togglePause();
        ticker.tick(5);

        assertThat(ticker.isRunning()).isFalse();
        assertThat(session.isPaused()).isFalse();
        assertThat(session.isPauseAvailable()).isFalse();
        assertThat(session.getRemainingSeconds()).isEqualTo(LIMIT);
    }

    @Test
    void firstLeftClickStartsTimerAndEnablesPause() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));

        assertThat(ticker.isRunning()).isTrue();
        assertThat(session.isPauseAvailable()).isTrue();
        assertThat(listener.events).contains("pauseAvailable:true");
    }

    @Test
    void timerCountsDownAndReportsEverySecond() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        ticker.tick(5);

        assertThat(session.getRemainingSeconds()).isEqualTo(LIMIT - 5);
        assertThat(session.getElapsedSeconds()).isEqualTo(5);
        assertThat(listener.events).contains("time:99", "time:95");
    }

    // pause

    @Test
    void pauseStopsTimerAndBlocksClicksUntilResume() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        ticker.tick(3);

        session.togglePause();
        ticker.tick(10);
        session.leftClick(at(1, 0));
        session.rightClick(at(0, 1));

        assertThat(session.isPaused()).isTrue();
        assertThat(session.getRemainingSeconds()).isEqualTo(LIMIT - 3);
        assertThat(stateOf(session, 1, 0)).isEqualTo(CellState.CLOSED);
        assertThat(stateOf(session, 0, 1)).isEqualTo(CellState.CLOSED);
        assertThat(listener.events).contains("pause:true");

        session.togglePause();
        ticker.tick(2);

        assertThat(session.isPaused()).isFalse();
        assertThat(session.getRemainingSeconds()).isEqualTo(LIMIT - 5);
        assertThat(listener.events).contains("pause:false");

        session.leftClick(at(1, 0));

        assertThat(stateOf(session, 1, 0)).isEqualTo(CellState.OPENED);
    }

    @Test
    void pauseIsUnavailableAfterTheGameEnds() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        session.leftClick(at(0, 0));
        session.togglePause();

        assertThat(session.getState()).isEqualTo(GameState.LOST_BOMB);
        assertThat(session.isPauseAvailable()).isFalse();
        assertThat(session.isPaused()).isFalse();
    }

    // flags

    @Test
    void rightClickPlacesFlagWithoutStartingTimer() {

        GameSession session = session2x2();

        session.rightClick(at(1, 0));

        assertThat(stateOf(session, 1, 0)).isEqualTo(CellState.FLAGGED);
        assertThat(session.getRemainingBombs()).isEqualTo(0);
        assertThat(listener.events).contains("mines:0");
        assertThat(ticker.isRunning()).isFalse();
    }

    // result

    @Test
    void lossIsRecordedOnceAndStopsTimer() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        ticker.tick(4);
        session.leftClick(at(0, 0));

        // Everything after the game has ended is ignored.
        session.leftClick(at(1, 0));
        session.rightClick(at(0, 1));
        ticker.tick(5);

        assertThat(session.getState()).isEqualTo(GameState.LOST_BOMB);
        assertThat(stats.losses).isEqualTo(1);
        assertThat(stats.wins).isEmpty();
        assertThat(ticker.isRunning()).isFalse();
        assertThat(stateOf(session, 1, 0)).isEqualTo(CellState.CLOSED);
        assertThat(listener.results)
                .containsExactly(new GameResult(GameState.LOST_BOMB, 4));
    }

    @Test
    void winIsRecordedWithElapsedTime() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        ticker.tick(7);
        session.leftClick(at(1, 0));
        session.leftClick(at(0, 1));

        assertThat(session.getState()).isEqualTo(GameState.WON);
        assertThat(stats.wins).containsExactly("EASY:7");
        assertThat(stats.losses).isEqualTo(0);
        assertThat(ticker.isRunning()).isFalse();
        assertThat(session.isPauseAvailable()).isFalse();
        assertThat(listener.results)
                .containsExactly(new GameResult(GameState.WON, 7));
    }

    @Test
    void winOnTheFirstClickIsRecordedWithZeroSeconds() {

        GameSession session = session3x3();

        session.leftClick(at(2, 2));

        assertThat(session.getState()).isEqualTo(GameState.WON);
        assertThat(stats.wins).containsExactly("EASY:0");
        assertThat(ticker.isRunning()).isFalse();
    }

    @Test
    void timeoutIsRecordedAsLoss() {

        GameSession session = session(3, "*.", "..");

        session.leftClick(at(1, 1));
        ticker.tick(3);
        ticker.tick(3);

        assertThat(session.getState()).isEqualTo(GameState.TIMEOUT);
        assertThat(session.getRemainingSeconds()).isEqualTo(0);
        assertThat(stats.losses).isEqualTo(1);
        assertThat(stats.wins).isEmpty();
        assertThat(ticker.isRunning()).isFalse();
        assertThat(stateOf(session, 0, 0)).isEqualTo(CellState.OPENED);
        assertThat(listener.results)
                .containsExactly(new GameResult(GameState.TIMEOUT, 3));
    }

    @Test
    void resultIsRecordedOnceEvenIfTickerKeepsTicking() {

        // A tick that was already queued can arrive after stop().
        LeakyTicker leaky = new LeakyTicker();

        GameSession session = new GameSession(
                Difficulty.EASY,
                new FixedLayoutGameFactory("*.", ".."),
                stats,
                leaky,
                2
        );

        session.setListener(listener);

        session.leftClick(at(1, 1));
        session.leftClick(at(0, 0));
        leaky.tick();
        leaky.tick();

        assertThat(session.getState()).isEqualTo(GameState.LOST_BOMB);
        assertThat(stats.losses).isEqualTo(1);
        assertThat(listener.results).hasSize(1);
    }

    // restart

    @Test
    void restartResetsTimerBoardAndPause() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        ticker.tick(4);
        session.leftClick(at(0, 0));
        session.restart();

        assertThat(session.getState()).isEqualTo(GameState.PLAYING);
        assertThat(session.getRemainingSeconds()).isEqualTo(LIMIT);
        assertThat(session.getRemainingBombs()).isEqualTo(1);
        assertThat(session.isPauseAvailable()).isFalse();
        assertThat(ticker.isRunning()).isFalse();
        assertThat(stateOf(session, 0, 0)).isEqualTo(CellState.CLOSED);
        assertThat(session.getBoard().getCell(at(0, 0)).getContent())
                .isEqualTo(CellContent.ZERO);
        assertThat(listener.events).contains("restarted", "time:100");
    }

    @Test
    void restartAllowsRecordingTheNextResult() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        session.leftClick(at(0, 0));
        session.restart();
        session.leftClick(at(1, 1));
        session.leftClick(at(0, 0));

        assertThat(stats.losses).isEqualTo(2);
    }

    @Test
    void restartDuringPauseClearsPause() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        session.togglePause();
        session.restart();
        ticker.tick(5);

        assertThat(session.isPaused()).isFalse();
        assertThat(session.getRemainingSeconds()).isEqualTo(LIMIT);
        assertThat(listener.events).contains("pause:false");
    }

    @Test
    void restartingAnUnfinishedGameRecordsNothing() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        session.restart();

        assertThat(stats.losses).isEqualTo(0);
        assertThat(stats.wins).isEmpty();
    }

    // dispose

    @Test
    void disposeStopsTimerAndIgnoresInput() {

        GameSession session = session2x2();

        session.leftClick(at(1, 1));
        session.dispose();

        session.leftClick(at(1, 0));
        session.rightClick(at(0, 1));
        session.togglePause();
        session.restart();
        ticker.tick(5);

        assertThat(ticker.isRunning()).isFalse();
        assertThat(session.isPauseAvailable()).isFalse();
        assertThat(stateOf(session, 1, 0)).isEqualTo(CellState.CLOSED);
        assertThat(stats.losses).isEqualTo(0);
        assertThat(stats.wins).isEmpty();
    }

    // test doubles

    /** A ticker that ignores stop(), like a tick already in the queue. */
    private static final class LeakyTicker implements Ticker {

        private Runnable onTick;

        @Override
        public void start(Runnable onTick) {
            this.onTick = onTick;
        }

        @Override
        public void pause() {
            // ignored on purpose
        }

        @Override
        public void resume() {
            // ignored on purpose
        }

        @Override
        public void stop() {
            // ignored on purpose
        }

        void tick() {
            onTick.run();
        }
    }

    private static final class RecordingStats implements StatsRepository {

        private final List<String> wins = new ArrayList<>();
        private int losses;

        @Override
        public void recordWin(Difficulty difficulty, int seconds) {
            wins.add(difficulty + ":" + seconds);
        }

        @Override
        public void recordLoss(Difficulty difficulty) {
            losses++;
        }

        @Override
        public DifficultyStats get(Difficulty difficulty) {
            return DifficultyStats.EMPTY;
        }

        @Override
        public void reset() {
            wins.clear();
            losses = 0;
        }
    }

    private static final class RecordingListener
            implements GameSessionListener {

        private final List<String> events = new ArrayList<>();
        private final List<GameResult> results = new ArrayList<>();

        @Override
        public void boardChanged() {
            events.add("board");
        }

        @Override
        public void minesChanged(int remaining) {
            events.add("mines:" + remaining);
        }

        @Override
        public void timeChanged(int remainingSeconds) {
            events.add("time:" + remainingSeconds);
        }

        @Override
        public void pauseChanged(boolean paused) {
            events.add("pause:" + paused);
        }

        @Override
        public void pauseAvailabilityChanged(boolean available) {
            events.add("pauseAvailable:" + available);
        }

        @Override
        public void finished(GameResult result) {
            results.add(result);
        }

        @Override
        public void restarted() {
            events.add("restarted");
        }
    }
}
