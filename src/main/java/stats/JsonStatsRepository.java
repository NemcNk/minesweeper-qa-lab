package stats;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Difficulty;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * A statistics repository that stores game results in a JSON file.
 *
 * <p>The file uses the following format (version 1):
 * <pre>
 * {
 *   "version" : 1,
 *   "stats" : {
 *     "EASY" : { "wins" : 3, "losses" : 1, "bestTimeSeconds" : 30 }
 *   }
 * }
 * </pre>
 *
 * <p>The file is loaded once when the repository is created and updated
 * after every recorded result. If the file is corrupted, it is moved to
 * {@code <name>.corrupt}, and the repository starts with empty statistics.
 * Save failures are logged without interrupting the game.
 */
public final class JsonStatsRepository implements StatsRepository {

    /** Name of the logger used for statistics repository operations.*/
    private static final String LOGGER_NAME = "minesweeper.stats";

    /** Current version of the JSON statistics file format.*/
    private static final int FORMAT_VERSION = 1;

    /** Logger used to report loading and saving errors.*/
    private final System.Logger log;

    /** Absolute path to the JSON statistics file.*/
    private final Path file;

    /** Directory containing the JSON statistics file.*/
    private final Path directory;

    /** Jackson object mapper used to read and write JSON data.*/
    private final ObjectMapper mapper = new ObjectMapper();

    /** Statistics indexed by difficulty level.*/
    private final Map<Difficulty, DifficultyStats> stats =
            new EnumMap<>(Difficulty.class);

    /** Creates a repository and loads statistics from the specified file.
     * @param file path to the JSON file, created on the first save
     * @throws IllegalArgumentException if the path has no parent directory
     */
    public JsonStatsRepository(Path file) {
        this(file, System.getLogger(LOGGER_NAME));
    }

    /** Creates a repository using the specified logger.
     * <p>This constructor has package-private visibility to allow tests
     * to provide a recording logger.
     * @param file path to the JSON statistics file
     * @param log logger used to report loading and saving errors
     * @throws NullPointerException if the file or logger is null
     * @throws IllegalArgumentException if the path has no parent directory
     */
    JsonStatsRepository(Path file, System.Logger log) {
        this.log = Objects.requireNonNull(log);

        Path absolute = Objects.requireNonNull(file).toAbsolutePath();
        Path parent = absolute.getParent();

        if (parent == null) {
            throw new IllegalArgumentException(
                    "Not a file path: " + file
            );
        }

        this.file = absolute;
        this.directory = parent;

        load();
    }

    /** Returns the default location of the statistics file.
     * <p>The file is stored at
     * {@code ~/.minesweeper-qa-lab/statistics.json}.
     * @return default statistics file path.*/
    public static Path defaultFile() {
        return Path.of(
                System.getProperty("user.home"),
                ".minesweeper-qa-lab",
                "statistics.json"
        );
    }

    /** Records a win for the specified difficulty level.
     * @param difficulty difficulty level of the completed game
     * @param seconds time taken to complete the game, in seconds
     */
    @Override
    public void recordWin(Difficulty difficulty, int seconds) {
        // Validate the completion time before updating the statistics.
        stats.put(difficulty, get(difficulty).withWin(seconds));
        save();
    }

    /** Records a loss for the specified difficulty level.
     * @param difficulty difficulty level of the lost game.*/
    @Override
    public void recordLoss(Difficulty difficulty) {
        stats.put(difficulty, get(difficulty).withLoss());
        save();
    }

    /** Returns statistics for the specified difficulty level.
     * <p>If no results have been recorded for this difficulty,
     * empty statistics are returned.
     * @param difficulty difficulty level to retrieve statistics for
     * @return statistics associated with the specified difficulty
     * @throws NullPointerException if the difficulty is null
     */
    @Override
    public DifficultyStats get(Difficulty difficulty) {
        Objects.requireNonNull(difficulty);

        return stats.getOrDefault(difficulty, DifficultyStats.EMPTY);
    }

    /** Resets all statistics and saves the empty state to the file.
     * <p>The file is rewritten using the same atomic write mechanism
     * as regular saves, preventing partially written data after a crash.
     * Existing backups of corrupted files are not modified.
     * {@inheritDoc}*/
    @Override
    public void reset() {
        stats.clear();
        save();
    }

    /** Loads statistics from the JSON file.
     * <p>If the file does not exist, the repository remains empty.
     * If reading or parsing fails, the corrupted file is moved aside.
     */
    private void load() {

        if (!Files.exists(file)) {
            return;
        }

        try {
            JsonNode root = mapper.readTree(file.toFile());

            // An empty file means that no statistics have been saved yet.
            if (root.isMissingNode()) {
                return;
            }

            readRoot(root);

        } catch (IOException | IllegalArgumentException e) {
            log.log(System.Logger.Level.WARNING,
                    "Cannot read statistics from " + file
                            + ", starting with empty statistics", e);

            stats.clear();
            moveCorruptFileAside();
        }
    }

    /** Validates the root JSON object and reads statistics for each difficulty.
     * @param root root JSON node containing the repository data
     * @throws IllegalArgumentException if the root structure or format
     *                                  version is invalid
     */
    private void readRoot(JsonNode root) {

        if (!root.isObject()) {
            throw new IllegalArgumentException("Root must be an object");
        }

        JsonNode version = root.path("version");

        if (version.isInt() && version.asInt() != FORMAT_VERSION) {
            throw new IllegalArgumentException(
                    "Unsupported format version: " + version.asInt()
            );
        }

        JsonNode entries = root.path("stats");

        // Read only entries corresponding to known difficulty levels.
        for (Difficulty difficulty : Difficulty.values()) {
            JsonNode node = entries.path(difficulty.name());

            if (node.isObject()) {
                stats.put(difficulty, readEntry(node));
            }
        }
    }

    /** Reads and validates statistics for a single difficulty level.
     * @param node JSON object containing wins, losses, and the best time
     * @return validated statistics entry
     * @throws IllegalArgumentException if any numeric field is invalid
     */
    private static DifficultyStats readEntry(JsonNode node) {

        int wins = readInt(node, "wins");
        int losses = readInt(node, "losses");

        JsonNode best = node.path("bestTimeSeconds");

        OptionalInt bestTime = best.isMissingNode() || best.isNull()
                ? OptionalInt.empty()
                : OptionalInt.of(readInt(node, "bestTimeSeconds"));

        // DifficultyStats performs additional validation of the values.
        return new DifficultyStats(wins, losses, bestTime);
    }

    /** Reads an integer field from a JSON object.
     * @param node JSON object containing the requested field
     * @param field name of the field to read
     * @return integer value of the field
     * @throws IllegalArgumentException if the field is missing or is not
     *                                  an integer
     */
    private static int readInt(JsonNode node, String field) {

        JsonNode value = node.path(field);

        if (!value.isInt()) {
            throw new IllegalArgumentException(
                    "Field '" + field + "' must be an integer"
            );
        }

        return value.asInt();
    }

    /** Moves the corrupted statistics file to a backup location.
     * <p>The backup file uses the {@code .corrupt} suffix.
     * If the move fails, the error is logged.*/
    private void moveCorruptFileAside() {

        Path backup = file.resolveSibling(file.getFileName() + ".corrupt");

        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.log(System.Logger.Level.WARNING,
                    "Cannot move the damaged file aside: " + file, e);
        }
    }

    /** Serializes the current statistics and saves them to the JSON file.
     * <p>Save failures are logged and do not propagate to the caller.
     */
    private void save() {

        ObjectNode root = mapper.createObjectNode();
        root.put("version", FORMAT_VERSION);

        ObjectNode entries = root.putObject("stats");

        // Write an entry for every difficulty, including empty statistics.
        for (Difficulty difficulty : Difficulty.values()) {
            DifficultyStats entry = get(difficulty);

            ObjectNode node = entries.putObject(difficulty.name());
            node.put("wins", entry.wins());
            node.put("losses", entry.losses());

            if (entry.bestTimeSeconds().isPresent()) {
                node.put("bestTimeSeconds", entry.bestTimeSeconds().getAsInt());
            } else {
                node.putNull("bestTimeSeconds");
            }
        }

        try {
            write(root);
        } catch (IOException e) {
            log.log(System.Logger.Level.ERROR,
                    "Cannot save statistics to " + file, e);
        }
    }

    /** Writes JSON data to a temporary file and replaces the target file.
     * <p>An atomic move is attempted first. If the file system does not
     * support atomic moves, a regular replacement is performed instead.
     * The temporary file is deleted regardless of the operation result.
     * @param root JSON object to write
     * @throws IOException if directory creation, writing, moving,
     *                     or temporary file deletion fails
     */
    private void write(ObjectNode root) throws IOException {

        Files.createDirectories(directory);

        Path temp = Files.createTempFile(directory, "statistics", ".tmp");

        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temp.toFile(), root);

            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                // Fall back to a regular replacement when atomic moves
                // are not supported by the file system.
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }

        } finally {
            Files.deleteIfExists(temp);
        }
    }
}

