package stats;

import model.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Failure paths of {@link JsonStatsRepository}: what is logged and
 * that the repository never crashes the game.
 */
class JsonStatsRepositoryRobustnessTest {

    @TempDir
    Path dir;

    private final RecordingLogger recorder = new RecordingLogger();

    private Path file() {
        return dir.resolve("statistics.json");
    }

    @Test
    void defaultFileIsInsideUserHome() {

        Path path = JsonStatsRepository.defaultFile();

        assertThat(path.getFileName().toString())
                .isEqualTo("statistics.json");
        assertThat(path.getParent().getFileName().toString())
                .isEqualTo(".minesweeper-qa-lab");
        assertThat(path.startsWith(Path.of(System.getProperty("user.home"))))
                .isTrue();;
    }

    @Test
    void pathWithoutParentIsRejected() {

        Path root = dir.toAbsolutePath().getRoot();

        assertThatThrownBy(() -> new JsonStatsRepository(root, recorder))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void damagedFileIsReportedAsWarning() throws IOException {

        Files.writeString(file(), "{not json");

        new JsonStatsRepository(file(), recorder);

        assertThat(recorder.lines).hasSize(1);
        assertThat(recorder.lines.get(0))
                .startsWith("WARNING Cannot read statistics from");
    }

    @Test
    void validFileLogsNothing() {

        new JsonStatsRepository(file(), recorder).recordLoss(Difficulty.EASY);
        new JsonStatsRepository(file(), recorder);

        assertThat(recorder.lines).isEmpty();
    }

    @Test
    void partiallyReadFileDoesNotLeaveHalfLoadedStats() throws IOException {

        // EASY is valid, MEDIUM is damaged: nothing must be kept
        Files.writeString(file(), """
                {"version":1,"stats":{
                "EASY":{"wins":5,"losses":1,"bestTimeSeconds":20},
                "MEDIUM":{"wins":"x","losses":0}}}
                """);

        JsonStatsRepository repository =
                new JsonStatsRepository(file(), recorder);

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
    }

    @Test
    void validEntriesOfAnotherDifficultyAreKept() throws IOException {

        Files.writeString(file(), """
                {"version":1,"stats":{
                "HARD":{"wins":2,"losses":3,"bestTimeSeconds":77}}}
                """);

        DifficultyStats hard = new JsonStatsRepository(file(), recorder)
                .get(Difficulty.HARD);

        assertThat(hard.wins()).isEqualTo(2);
        assertThat(hard.losses()).isEqualTo(3);
        assertThat(hard.bestTimeSeconds()).isEqualTo(OptionalInt.of(77));
    }

    @Test
    void failedSaveIsLoggedAndDoesNotThrow() throws IOException {

        // "blocker" is a regular file, so its "directory" cannot be created
        Path blocker = dir.resolve("blocker");
        Files.writeString(blocker, "x");

        JsonStatsRepository repository = new JsonStatsRepository(
                blocker.resolve("statistics.json"), recorder);

        repository.recordLoss(Difficulty.EASY);

        // the result is still available in memory for this session
        assertThat(repository.get(Difficulty.EASY).losses()).isEqualTo(1);
        assertThat(recorder.lines).hasSize(1);
        assertThat(recorder.lines.get(0))
                .startsWith("ERROR Cannot save statistics to");
    }

    @Test
    void failureToMoveDamagedFileAsideIsLoggedAndDoesNotThrow()
            throws IOException {

        Files.writeString(file(), "{not json");

        // a non-empty directory occupies the backup name
        Path backup = dir.resolve("statistics.json.corrupt");
        Files.createDirectory(backup);
        Files.writeString(backup.resolve("keep.txt"), "x");

        JsonStatsRepository repository =
                new JsonStatsRepository(file(), recorder);

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
        assertThat(recorder.lines).hasSize(2);
        assertThat(recorder.lines.get(0))
                .startsWith("WARNING Cannot read statistics from");
        assertThat(recorder.lines.get(1))
                .startsWith("WARNING Cannot move the damaged file aside");
    }
}
