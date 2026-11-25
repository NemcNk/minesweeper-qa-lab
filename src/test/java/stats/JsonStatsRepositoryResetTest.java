package stats;

import model.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import support.RecordingLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link JsonStatsRepository#reset()}: the data disappears from
 * memory and from the file, and the repository stays usable.
 */
class JsonStatsRepositoryResetTest {

    @TempDir
    Path dir;

    private final RecordingLogger logger = new RecordingLogger();

    private Path file() {
        return dir.resolve("statistics.json");
    }

    private JsonStatsRepository open() {
        return new JsonStatsRepository(file(), logger);
    }

    @Test
    void resetClearsEveryDifficulty() {

        JsonStatsRepository repository = open();

        repository.recordWin(Difficulty.EASY, 30);
        repository.recordLoss(Difficulty.EASY);
        repository.recordWin(Difficulty.GOD, 3000);
        repository.recordLoss(Difficulty.HARD);

        repository.reset();

        for (Difficulty difficulty : Difficulty.values()) {
            assertThat(repository.get(difficulty))
                    .isEqualTo(DifficultyStats.EMPTY);
        }
    }

    @Test
    void resetIsStoredInTheFile() {

        JsonStatsRepository repository = open();

        repository.recordWin(Difficulty.EASY, 30);
        repository.recordLoss(Difficulty.MEDIUM);
        repository.reset();

        // A new instance reads the file again, as after an application restart.
        JsonStatsRepository reopened = open();

        for (Difficulty difficulty : Difficulty.values()) {
            assertThat(reopened.get(difficulty))
                    .isEqualTo(DifficultyStats.EMPTY);
        }

        assertThat(logger.lines).isEmpty();
    }

    @Test
    void resultsAfterResetStartFromZero() {

        JsonStatsRepository repository = open();

        repository.recordWin(Difficulty.EASY, 10);
        repository.recordWin(Difficulty.EASY, 20);
        repository.reset();
        repository.recordLoss(Difficulty.EASY);

        DifficultyStats easy = open().get(Difficulty.EASY);

        assertThat(easy.wins()).isEqualTo(0);
        assertThat(easy.losses()).isEqualTo(1);
        assertThat(easy.bestTimeSeconds()).isEqualTo(OptionalInt.empty());
    }

    @Test
    void bestTimeStartsAgainAfterReset() {

        JsonStatsRepository repository = open();

        repository.recordWin(Difficulty.EASY, 10);
        repository.reset();
        // Slower than the deleted record: it must become the new best time.
        repository.recordWin(Difficulty.EASY, 50);

        assertThat(open().get(Difficulty.EASY).bestTimeSeconds())
                .isEqualTo(OptionalInt.of(50));
    }

    @Test
    void resetWithoutAnyDataWritesAValidFile() throws IOException {

        JsonStatsRepository repository = open();

        repository.reset();

        assertThat(Files.exists(file())).isTrue();
        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
        assertThat(logger.lines).isEmpty();
    }

    @Test
    void resetLeavesNoTemporaryFilesBehind() throws IOException {

        JsonStatsRepository repository = open();

        repository.recordLoss(Difficulty.EASY);
        repository.reset();

        try (var files = Files.list(dir)) {
            assertThat(files.map(path -> path.getFileName().toString())
                    .toList())
                    .containsExactly("statistics.json");
        }
    }

    @Test
    void resetAfterDamagedFileKeepsTheBackup() throws IOException {

        Files.writeString(file(), "{ not json");

        JsonStatsRepository repository = open();

        // Opening moved the damaged file aside and logged a warning.
        assertThat(logger.lines).hasSize(1);

        repository.reset();

        assertThat(Files.exists(dir.resolve("statistics.json.corrupt")))
                .isTrue();
        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);

        // Only the warning from the first opening: the new file is valid.
        assertThat(logger.lines).hasSize(1);
    }

    @Test
    void failedSaveDuringResetIsLoggedAndMemoryIsStillCleared()
            throws IOException {

        // The parent "directory" is a regular file, so saving always fails.
        Path blocker = dir.resolve("blocker");
        Files.writeString(blocker, "x");

        JsonStatsRepository repository = new JsonStatsRepository(
                blocker.resolve("statistics.json"), logger
        );

        repository.recordWin(Difficulty.EASY, 5);
        repository.reset();

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
        assertThat(logger.lines).hasSize(2);
        assertThat(logger.lines.get(1)).startsWith("ERROR");
    }
}