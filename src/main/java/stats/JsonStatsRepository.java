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
 * Statistics stored in a JSON file.
 *
 * <p>File format (version 1):
 * <pre>
 * {
 *   "version" : 1,
 *   "stats" : {
 *     "EASY" : { "wins" : 3, "losses" : 1, "bestTimeSeconds" : 30 }
 *   }
 * }
 * </pre>
 *
 * <p>The file is read once in the constructor and rewritten after every
 * recorded result. A damaged file is moved aside to
 * {@code <name>.corrupt} and the repository starts empty. A failed save is
 * logged and never interrupts the game.
 */
public final class JsonStatsRepository implements StatsRepository {

    private static final String LOGGER_NAME = "minesweeper.stats";

    private static final int FORMAT_VERSION = 1;

    private final System.Logger log;
    private final Path file;
    private final Path directory;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Difficulty, DifficultyStats> stats =
            new EnumMap<>(Difficulty.class);

    /**
     * Opens the repository and loads the file if it exists.
     *
     * @param file location of the JSON file, created on the first save
     * @throws IllegalArgumentException if the path has no parent directory
     */
    public JsonStatsRepository(Path file) {
        this(file, System.getLogger(LOGGER_NAME));
    }

    // Package-private for tests: the caller supplies a recording logger
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

    /**
     * Returns the default location of the statistics file:
     * {@code ~/.minesweeper-qa-lab/statistics.json}.
     */
    public static Path defaultFile() {
        return Path.of(
                System.getProperty("user.home"),
                ".minesweeper-qa-lab",
                "statistics.json"
        );
    }

    @Override
    public void recordWin(Difficulty difficulty, int seconds) {
        // withWin validates the time before anything is stored.
        stats.put(difficulty, get(difficulty).withWin(seconds));
        save();
    }

    @Override
    public void recordLoss(Difficulty difficulty) {
        stats.put(difficulty, get(difficulty).withLoss());
        save();
    }

    @Override
    public DifficultyStats get(Difficulty difficulty) {
        Objects.requireNonNull(difficulty);

        return stats.getOrDefault(difficulty, DifficultyStats.EMPTY);
    }

    // Loading

    private void load() {

        if (!Files.exists(file)) {
            return;
        }

        try {
            JsonNode root = mapper.readTree(file.toFile());

            // An empty file simply means "no data yet".
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

        for (Difficulty difficulty : Difficulty.values()) {
            JsonNode node = entries.path(difficulty.name());

            if (node.isObject()) {
                stats.put(difficulty, readEntry(node));
            }
        }
    }

    private static DifficultyStats readEntry(JsonNode node) {

        int wins = readInt(node, "wins");
        int losses = readInt(node, "losses");

        JsonNode best = node.path("bestTimeSeconds");

        OptionalInt bestTime = best.isMissingNode() || best.isNull()
                ? OptionalInt.empty()
                : OptionalInt.of(readInt(node, "bestTimeSeconds"));

        // The record rejects negative values.
        return new DifficultyStats(wins, losses, bestTime);
    }

    private static int readInt(JsonNode node, String field) {

        JsonNode value = node.path(field);

        if (!value.isInt()) {
            throw new IllegalArgumentException(
                    "Field '" + field + "' must be an integer"
            );
        }

        return value.asInt();
    }

    private void moveCorruptFileAside() {

        Path backup = file.resolveSibling(file.getFileName() + ".corrupt");

        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.log(System.Logger.Level.WARNING,
                    "Cannot move the damaged file aside: " + file, e);
        }
    }

    // Saving

    private void save() {

        ObjectNode root = mapper.createObjectNode();
        root.put("version", FORMAT_VERSION);

        ObjectNode entries = root.putObject("stats");

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

    // Writes to a temporary file first, so a crash cannot leave
    // a half-written statistics file.
    private void write(ObjectNode root) throws IOException {

        Files.createDirectories(directory);

        Path temp = Files.createTempFile(directory, "statistics", ".tmp");

        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temp.toFile(), root);

            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }

        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
