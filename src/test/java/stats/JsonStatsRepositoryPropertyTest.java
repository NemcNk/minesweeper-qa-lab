package stats;

import model.Difficulty;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link JsonStatsRepository}.
 *
 * <p>Verifies persistence, restoration, reset behavior
 * and statistics isolation between difficulty levels.
 */
class JsonStatsRepositoryPropertyTest {

    /**
     * Creates a temporary file for statistics persistence tests.
     *
     * @param fileName name of the statistics file
     * @return path to the temporary statistics file
     * @throws IOException if the temporary directory cannot be created
     */
    private Path createTemporaryFile(String fileName)
            throws IOException {

        Path directory = Files.createTempDirectory(
                "minesweeper-stats-test-"
        );

        directory.toFile().deleteOnExit();

        return directory.resolve(fileName);
    }

    /**
     * Verifies that recorded wins survive repository reconstruction.
     *
     * @param seconds generated winning time
     * @throws IOException if temporary storage cannot be created
     */
    @Property(tries = 100)
    void shouldRestoreRecordedWins(
            @ForAll @IntRange(min = 1, max = 10000) int seconds
    ) throws IOException {

        Path file = createTemporaryFile("statistics.json");

        JsonStatsRepository first =
                new JsonStatsRepository(file);

        first.recordWin(Difficulty.EASY, seconds);

        JsonStatsRepository restored =
                new JsonStatsRepository(file);

        DifficultyStats result = restored.get(Difficulty.EASY);

        assertThat(result.wins()).isEqualTo(1);
        assertThat(result.losses()).isZero();

        assertThat(result.bestTimeSeconds())
                .isEqualTo(OptionalInt.of(seconds));
    }

    /**
     * Verifies that recorded losses survive repository reconstruction.
     *
     * @param losses generated number of losses
     * @throws IOException if temporary storage cannot be created
     */
    @Property(tries = 100)
    void shouldRestoreRecordedLosses(
            @ForAll @IntRange(min = 1, max = 100) int losses
    ) throws IOException {

        Path file = createTemporaryFile("losses.json");

        JsonStatsRepository repository =
                new JsonStatsRepository(file);

        for (int i = 0; i < losses; i++) {
            repository.recordLoss(Difficulty.MEDIUM);
        }

        JsonStatsRepository restored =
                new JsonStatsRepository(file);

        assertThat(restored.get(Difficulty.MEDIUM).losses())
                .isEqualTo(losses);
    }

    /**
     * Verifies that the best time is always the minimum winning time.
     *
     * @param first first winning time
     * @param second second winning time
     * @throws IOException if temporary storage cannot be created
     */
    @Property(tries = 100)
    void shouldPreserveBestWinningTime(
            @ForAll @IntRange(min = 1, max = 10000) int first,
            @ForAll @IntRange(min = 1, max = 10000) int second
    ) throws IOException {

        Path file = createTemporaryFile("best-time.json");

        JsonStatsRepository repository =
                new JsonStatsRepository(file);

        repository.recordWin(Difficulty.HARD, first);
        repository.recordWin(Difficulty.HARD, second);

        JsonStatsRepository restored =
                new JsonStatsRepository(file);

        assertThat(
                restored.get(Difficulty.HARD).bestTimeSeconds()
        ).isEqualTo(OptionalInt.of(Math.min(first, second)));
    }

    /**
     * Verifies that reset removes all recorded statistics.
     *
     * @param seconds generated winning time
     * @throws IOException if temporary storage cannot be created
     */
    @Property(tries = 100)
    void shouldRestoreEmptyStatisticsAfterReset(
            @ForAll @IntRange(min = 1, max = 1000) int seconds
    ) throws IOException {

        Path file = createTemporaryFile("reset.json");

        JsonStatsRepository repository =
                new JsonStatsRepository(file);

        repository.recordWin(Difficulty.EASY, seconds);
        repository.recordLoss(Difficulty.HARD);

        repository.reset();

        JsonStatsRepository restored =
                new JsonStatsRepository(file);

        assertThat(restored.get(Difficulty.EASY))
                .isEqualTo(DifficultyStats.EMPTY);

        assertThat(restored.get(Difficulty.HARD))
                .isEqualTo(DifficultyStats.EMPTY);
    }

    /**
     * Verifies that statistics for different difficulties are isolated.
     *
     * @param seconds generated winning time
     * @throws IOException if temporary storage cannot be created
     */
    @Property(tries = 100)
    void shouldKeepDifficultyStatisticsIndependent(
            @ForAll @IntRange(min = 1, max = 10000) int seconds
    ) throws IOException {

        Path file = createTemporaryFile("difficulty.json");

        JsonStatsRepository repository =
                new JsonStatsRepository(file);

        repository.recordWin(Difficulty.EASY, seconds);

        JsonStatsRepository restored =
                new JsonStatsRepository(file);

        assertThat(restored.get(Difficulty.EASY).wins())
                .isEqualTo(1);

        assertThat(restored.get(Difficulty.MEDIUM))
                .isEqualTo(DifficultyStats.EMPTY);

        assertThat(restored.get(Difficulty.HARD))
                .isEqualTo(DifficultyStats.EMPTY);
    }
}


