package session;

import core.FixedLayoutGameFactory;
import model.Coord;
import model.Difficulty;
import model.GameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import stats.DifficultyStats;
import stats.JsonStatsRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end scenarios without a user interface: a real game with a fixed
 * bomb layout is played through a {@link GameSession}, the result goes to a
 * real {@link JsonStatsRepository} on a temporary file, and a second
 * repository instance opened on the same file (as after an application
 * restart) must show the stored result.
 *
 * <p>The 2x2 board has one bomb at (0,0); the other three cells show 1.
 * A first click on (1,1) opens only that cell, so the game goes on.
 */
class GameSessionStatsIntegrationTest {

    private static final String[] BOARD_2X2 = {"*.", ".."};

    @TempDir
    Path dir;

    private final ManualTicker ticker = new ManualTicker();

    private Path file() {
        return dir.resolve("statistics.json");
    }

    // A new instance reads the file again: the "restart of the application".
    private JsonStatsRepository open() {
        return new JsonStatsRepository(file());
    }

    private GameSession newSession(JsonStatsRepository repository) {
        return newSession(repository, 600);
    }

    private GameSession newSession(
            JsonStatsRepository repository,
            int timeLimitSeconds
    ) {
        return new GameSession(
                Difficulty.EASY,
                new FixedLayoutGameFactory(BOARD_2X2),
                repository,
                ticker,
                timeLimitSeconds
        );
    }

    private static Coord at(int x, int y) {
        return new Coord(x, y);
    }

    private static DifficultyStats stats(
            int wins,
            int losses,
            OptionalInt best
    ) {
        return new DifficultyStats(wins, losses, best);
    }

    private static void loseGame(GameSession session) {
        session.leftClick(at(1, 1));
        session.leftClick(at(0, 0));
    }

    private void winGame(GameSession session, int seconds) {
        session.leftClick(at(1, 1));
        ticker.tick(seconds);
        session.leftClick(at(1, 0));
        session.leftClick(at(0, 1));
    }

    // The main scenario of the project

    @Test
    void lostGameIsStoredAndSurvivesRestart() {

        GameSession session = newSession(open());

        loseGame(session);

        assertThat(session.getState()).isEqualTo(GameState.LOST_BOMB);

        JsonStatsRepository reopened = open();

        assertThat(reopened.get(Difficulty.EASY))
                .isEqualTo(stats(0, 1, OptionalInt.empty()));
    }

    @Test
    void otherDifficultiesStayEmpty() {

        loseGame(newSession(open()));

        JsonStatsRepository reopened = open();

        for (Difficulty difficulty : Difficulty.values()) {
            if (difficulty != Difficulty.EASY) {
                assertThat(reopened.get(difficulty))
                        .isEqualTo(DifficultyStats.EMPTY);
            }
        }
    }

    @Test
    void wonGameIsStoredWithItsTime() {

        GameSession session = newSession(open());

        winGame(session, 42);

        assertThat(session.getState()).isEqualTo(GameState.WON);
        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(stats(1, 0, OptionalInt.of(42)));
    }

    @Test
    void timeoutIsStoredAsLoss() {

        GameSession session = newSession(open(), 3);

        session.leftClick(at(1, 1));
        ticker.tick(3);

        assertThat(session.getState()).isEqualTo(GameState.TIMEOUT);
        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(stats(0, 1, OptionalInt.empty()));
    }

    @Test
    void clicksAfterTheGameEndedDoNotChangeStoredResult() {

        GameSession session = newSession(open());

        loseGame(session);
        session.leftClick(at(1, 0));
        session.rightClick(at(0, 1));
        ticker.tick(10);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(stats(0, 1, OptionalInt.empty()));
    }

    // Several games and several restarts

    @Test
    void resultsAccumulateAcrossRestarts() {

        loseGame(newSession(open()));
        winGame(newSession(open()), 30);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(stats(1, 1, OptionalInt.of(30)));
    }

    @Test
    void bestTimeImprovesOnlyWhenTheNewTimeIsBetter() {

        winGame(newSession(open()), 30);
        winGame(newSession(open()), 50);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(stats(2, 0, OptionalInt.of(30)));

        winGame(newSession(open()), 20);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(stats(3, 0, OptionalInt.of(20)));
    }

    @Test
    void restartInsideOneSessionStoresEveryFinishedGame() {

        GameSession session = newSession(open());

        loseGame(session);
        session.restart();
        winGame(session, 7);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(stats(1, 1, OptionalInt.of(7)));
    }

    // Games that are not finished are not counted

    @Test
    void abandonedGameWritesNothing() {

        GameSession session = newSession(open());

        session.leftClick(at(1, 1));
        ticker.tick(5);
        session.dispose();

        assertThat(Files.exists(file())).isFalse();
        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
    }

    @Test
    void restartOfAnUnfinishedGameWritesNothing() {

        GameSession session = newSession(open());

        session.leftClick(at(1, 1));
        session.restart();

        assertThat(Files.exists(file())).isFalse();
    }

    // Reset

    @Test
    void resetRemovesStoredResultsFromTheFile() {

        JsonStatsRepository repository = open();

        loseGame(newSession(repository));
        winGame(newSession(repository), 12);
        repository.reset();

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
    }

    // Production factory: random layout

    @Test
    void randomGameIsStoredExactlyOnce() {

        JsonStatsRepository repository = open();

        GameSession session = new GameSession(
                Difficulty.EASY,
                new RandomGameFactory(),
                repository,
                ticker
        );

        // Open every cell until the game ends: either a bomb or the last
        // safe cell stops it.
        int size = Difficulty.EASY.getSize();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                session.leftClick(at(x, y));
            }
        }

        GameState result = session.getState();
        DifficultyStats stored = open().get(Difficulty.EASY);

        assertThat(result == GameState.WON || result == GameState.LOST_BOMB)
                .isTrue();
        assertThat(stored.wins() + stored.losses()).isEqualTo(1);
        assertThat(stored.wins()).isEqualTo(result == GameState.WON ? 1 : 0);
    }
}
