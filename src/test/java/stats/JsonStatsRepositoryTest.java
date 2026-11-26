package stats;

import support.RecordingLogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonStatsRepositoryTest {

    @TempDir
    Path dir;

    private Path file() {
        return dir.resolve("statistics.json");
    }

    private Path backup() {
        return dir.resolve("statistics.json.corrupt");
    }

    private final RecordingLogger logger = new RecordingLogger();
    private JsonStatsRepository open() {
        return new JsonStatsRepository(file(), logger);
    }

    @Test
    void missingFileGivesEmptyStatsAndCreatesNothing() {

        JsonStatsRepository repository = open();

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
        assertThat(file()).doesNotExist();
    }

    @Test
    void resultsSurviveRestart() {

        JsonStatsRepository first = open();
        first.recordWin(Difficulty.EASY, 50);
        first.recordLoss(Difficulty.EASY);
        first.recordWin(Difficulty.HARD, 99);

        JsonStatsRepository second = open();

        assertThat(second.get(Difficulty.EASY))
                .isEqualTo(first.get(Difficulty.EASY));
        assertThat(second.get(Difficulty.HARD).bestTimeSeconds())
                .isEqualTo(OptionalInt.of(99));
        assertThat(second.get(Difficulty.MEDIUM))
                .isEqualTo(DifficultyStats.EMPTY);
    }

    @Test
    void bestTimeStillImprovesOnlyAfterRestart() {

        open().recordWin(Difficulty.EASY, 50);

        // a slower win after a restart must not replace the stored best time
        open().recordWin(Difficulty.EASY, 70);

        DifficultyStats stats =
                open().get(Difficulty.EASY);

        assertThat(stats.wins()).isEqualTo(2);
        assertThat(stats.bestTimeSeconds()).isEqualTo(OptionalInt.of(50));
    }

    @Test
    void fileFormatIsStable() throws IOException {

        JsonStatsRepository repository = open();
        repository.recordWin(Difficulty.EASY, 30);
        repository.recordLoss(Difficulty.EASY);

        JsonNode root = new ObjectMapper().readTree(file().toFile());

        assertThat(root.path("version").asInt()).isEqualTo(1);
        assertThat(root.at("/stats/EASY/wins").asInt()).isEqualTo(1);
        assertThat(root.at("/stats/EASY/losses").asInt()).isEqualTo(1);
        assertThat(root.at("/stats/EASY/bestTimeSeconds").asInt())
                .isEqualTo(30);
        assertThat(root.at("/stats/HARD/bestTimeSeconds").isNull()).isTrue();
    }

    @Test
    void createsMissingParentDirectories() {

        Path nested = dir.resolve("a/b/statistics.json");

        new JsonStatsRepository(nested, logger).recordLoss(Difficulty.GOD);

        assertThat(nested).exists();
    }

    @Test
    void noTemporaryFilesAreLeftBehind() throws IOException {

        JsonStatsRepository repository = open();
        repository.recordLoss(Difficulty.EASY);
        repository.recordLoss(Difficulty.EASY);

        try (var files = Files.list(dir)) {
            assertThat(files).containsExactly(file());
        }
    }

    @Test
    void emptyFileMeansNoDataAndIsLeftInPlace() throws IOException {

        Files.writeString(file(), "");

        JsonStatsRepository repository = open();

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
        assertThat(file()).exists();
        assertThat(backup()).doesNotExist();
    }

    @Test
    void brokenJsonStartsEmptyAndKeepsBackup() throws IOException {

        Files.writeString(file(), "{not json");

        JsonStatsRepository repository = open();

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
        assertThat(backup()).hasContent("{not json");
        assertThat(file()).doesNotExist();

        // the repository keeps working and writes a fresh valid file
        repository.recordLoss(Difficulty.EASY);

        assertThat(open()
                .get(Difficulty.EASY).losses()).isEqualTo(1);
    }

    @Test
    void negativeValuesAreTreatedAsDamagedFile() throws IOException {

        Files.writeString(file(), """
                {"version":1,"stats":{"EASY":
                {"wins":-1,"losses":0,"bestTimeSeconds":null}}}
                """);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
        assertThat(backup()).exists();
    }

    @Test
    void wrongFieldTypeIsTreatedAsDamagedFile() throws IOException {

        Files.writeString(file(), """
                {"version":1,"stats":{"EASY":{"wins":"many","losses":0}}}
                """);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
        assertThat(backup()).exists();
    }

    @Test
    void unknownFormatVersionIsTreatedAsDamagedFile() throws IOException {

        Files.writeString(file(), "{\"version\":99,\"stats\":{}}");

        open();

        assertThat(backup()).exists();
    }

    @Test
    void rootThatIsNotAnObjectIsTreatedAsDamagedFile() throws IOException {

        Files.writeString(file(), "[1, 2, 3]");

        open();

        assertThat(backup()).exists();
    }

    @Test
    void rejectedWinWritesNothing() {

        JsonStatsRepository repository = open();

        assertThatThrownBy(() -> repository.recordWin(Difficulty.EASY, -1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(file()).doesNotExist();
        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);
    }

    @Test
    void publicConstructorWorksWithDefaultLogger() {

        new JsonStatsRepository(file()).recordLoss(Difficulty.EASY);

        assertThat(new JsonStatsRepository(file())
                .get(Difficulty.EASY).losses()).isEqualTo(1);
    }

    @Test
    void missingBestTimeMeansNoBestTime() throws IOException {

        Files.writeString(file(), """
            {
              "version": 1,
              "stats": {
                "EASY": {
                  "wins": 3,
                  "losses": 1
                }
              }
            }
            """);

        JsonStatsRepository repository = open();

        assertThat(repository.get(Difficulty.EASY))
                .isEqualTo(new DifficultyStats(
                        3, 1, OptionalInt.empty()
                ));

        assertThat(backup()).doesNotExist();
    }

    @Test
    void missingWinsIsTreatedAsDamagedFile() throws IOException {

        Files.writeString(file(), """
            {
              "version": 1,
              "stats": {
                "EASY": {
                  "losses": 1,
                  "bestTimeSeconds": null
                }
              }
            }
            """);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);

        assertThat(backup()).exists();
    }

    @Test
    void missingLossesIsTreatedAsDamagedFile() throws IOException {

        Files.writeString(file(), """
            {
              "version": 1,
              "stats": {
                "EASY": {
                  "wins": 3,
                  "bestTimeSeconds": 30
                }
              }
            }
            """);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);

        assertThat(backup()).exists();
    }

    @Test
    void wrongBestTimeTypeIsTreatedAsDamagedFile() throws IOException {

        Files.writeString(file(), """
            {
              "version": 1,
              "stats": {
                "EASY": {
                  "wins": 3,
                  "losses": 1,
                  "bestTimeSeconds": "fast"
                }
              }
            }
            """);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);

        assertThat(backup()).exists();
    }

    @Test
    void negativeBestTimeIsTreatedAsDamagedFile() throws IOException {

        Files.writeString(file(), """
            {
              "version": 1,
              "stats": {
                "EASY": {
                  "wins": 3,
                  "losses": 1,
                  "bestTimeSeconds": -10
                }
              }
            }
            """);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);

        assertThat(backup()).exists();
    }

    @Test
    void nullBestTimeMeansNoBestTime() throws IOException {

        Files.writeString(file(), """
            {
              "version": 1,
              "stats": {
                "EASY": {
                  "wins": 2,
                  "losses": 4,
                  "bestTimeSeconds": null
                }
              }
            }
            """);

        assertThat(open().get(Difficulty.EASY))
                .isEqualTo(new DifficultyStats(
                        2, 4, OptionalInt.empty()
                ));

        assertThat(backup()).doesNotExist();
    }
}
