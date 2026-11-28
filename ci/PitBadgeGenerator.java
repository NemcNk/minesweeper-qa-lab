package ci;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Generates individual PIT mutation coverage badges for project
 * packages and an overall mutation badge.
 *
 * <p>The generator reads one PIT XML report, groups mutations
 * by their mutated class package and creates five package badges
 * in {@code badges/pit} and one overall badge in
 * {@code badges/mutation.svg}.</p>
 *
 * <p>Non-viable mutations are excluded from the calculation.</p>
 */
public final class PitBadgeGenerator {

    /** Path to the PIT XML report. */
    private static final Path REPORT =
            Path.of("target/pit-reports/mutations.xml");

    /** Directory containing package badges. */
    private static final Path OUTPUT_DIRECTORY =
            Path.of("badges/pit");

    /** Path to the overall mutation badge. */
    private static final Path OVERALL_BADGE =
            Path.of("badges/mutation.svg");

    /** Overall mutation score color thresholds. */
    private static final int OVERALL_GREEN_THRESHOLD = 90;
    private static final int OVERALL_YELLOW_THRESHOLD = 80;

    /** Package-specific minimum mutation coverage thresholds. */
    private static final Map<String, Integer> THRESHOLDS =
            createThresholds();

    /** Prevents instantiation of this utility class. */
    private PitBadgeGenerator() {
        throw new IllegalStateException(
                "Utility class must not be instantiated"
        );
    }

    /** Reads the PIT report, generates package and overall badges
     * and verifies individual mutation coverage thresholds.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if the report is invalid or a threshold fails
     */
    public static void main(String[] args) throws Exception {
        Document document = loadReport();

        Map<String, int[]> counters = createCounters();

        collectMutations(document, counters);

        Files.createDirectories(OUTPUT_DIRECTORY);
        Files.createDirectories(OVERALL_BADGE.getParent());

        // Generate the overall mutation badge.
        generateOverallBadge(counters);

        boolean thresholdFailed = false;

        for (Map.Entry<String, Integer> entry
                : THRESHOLDS.entrySet()) {

            String packageName = entry.getKey();
            int threshold = entry.getValue();

            int[] packageCounters = counters.get(packageName);

            int killed = packageCounters[0];
            int total = packageCounters[1];

            if (total == 0) {
                throw new IllegalStateException(
                        "No viable mutations found for package: "
                                + packageName
                );
            }

            int percentage = calculatePercentage(killed, total);

            generateBadge(packageName, percentage, threshold);

            boolean passed = percentage >= threshold;

            System.out.printf(
                    "%s: %d%% (%d/%d), required: %d%% [%s]%n",
                    packageName,
                    percentage,
                    killed,
                    total,
                    threshold,
                    passed ? "PASS" : "FAIL"
            );

            if (!passed) {
                thresholdFailed = true;
            }
        }

        if (thresholdFailed) {
            throw new IllegalStateException(
                    "PIT package mutation threshold not met"
            );
        }
    }

    /** Generates the overall mutation badge using all configured
     * packages.
     *
     * @param counters mutation counters grouped by package
     * @throws Exception if the badge cannot be written.*/
    private static void generateOverallBadge(
            Map<String, int[]> counters
    ) throws Exception {

        int killed = 0;
        int total = 0;

        for (int[] packageCounters : counters.values()) {
            killed += packageCounters[0];
            total += packageCounters[1];
        }

        int percentage = calculatePercentage(killed, total);

        String color = BadgePalette.colorFor(
                percentage,
                OVERALL_GREEN_THRESHOLD,
                OVERALL_YELLOW_THRESHOLD
        );

        writeSvg(
                "mutation",
                percentage + "%",
                color,
                OVERALL_BADGE
        );

        System.out.printf(
                "Overall mutation score: %d%% (%d/%d)%n",
                percentage,
                killed,
                total
        );
    }

    /** Calculates a percentage safely.
     *
     * @param numerator number of successful items
     * @param denominator total number of items
     * @return rounded percentage
     * @throws IllegalStateException if denominator is zero.*/
    private static int calculatePercentage(
            int numerator,
            int denominator
    ) {
        if (denominator <= 0) {
            throw new IllegalStateException(
                    "Mutation counter contains no viable mutations"
            );
        }

        return (int) Math.round(
                numerator * 100.0 / denominator
        );
    }

    /**
     * Creates package-specific mutation thresholds.
     *
     * @return ordered map of package names and thresholds
     */
    private static Map<String, Integer> createThresholds() {
        Map<String, Integer> thresholds = new LinkedHashMap<>();

        thresholds.put("core", 80);
        thresholds.put("logging", 80);
        thresholds.put("session", 80);
        thresholds.put("model", 80);
        thresholds.put("stats", 80);

        return thresholds;
    }

    /** Creates empty mutation counters for all configured packages.
     *
     * <p>Each counter contains killed mutations and total viable
     * mutations, respectively.</p>
     *
     * @return ordered map containing package counters.*/
    private static Map<String, int[]> createCounters() {
        Map<String, int[]> counters = new LinkedHashMap<>();

        for (String packageName : THRESHOLDS.keySet()) {
            counters.put(packageName, new int[2]);
        }

        return counters;
    }

    /**
     * Loads the PIT XML report with external entity processing
     * disabled.
     *
     * @return parsed XML document
     * @throws Exception if the report is missing or invalid
     */
    private static Document loadReport() throws Exception {
        if (!Files.isRegularFile(REPORT)) {
            throw new IllegalStateException(
                    "PIT report not found: " + REPORT
            );
        }

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING,
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                ""
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory.newDocumentBuilder()
                .parse(REPORT.toFile());
    }

    /** Collects viable mutations and groups them by package.
     *
     * @param document parsed PIT XML document
     * @param counters package mutation counters.*/
    private static void collectMutations(
            Document document,
            Map<String, int[]> counters
    ) {
        NodeList mutations =
                document.getElementsByTagName("mutation");

        for (int i = 0; i < mutations.getLength(); i++) {
            Element mutation = (Element) mutations.item(i);

            String status = mutation.getAttribute("status");

            if ("NON_VIABLE".equals(status)) {
                continue;
            }

            Element mutatedClass = getMutatedClass(mutation);

            String className =
                    mutatedClass.getTextContent().trim();

            String packageName = getPackageName(className);

            int[] packageCounters = counters.get(packageName);

            if (packageCounters == null) {
                continue;
            }

            if ("KILLED".equals(status)) {
                packageCounters[0]++;
                packageCounters[1]++;
            } else if (isViableUnkilledStatus(status)) {
                packageCounters[1]++;
            } else {
                throw new IllegalStateException(
                        "Unknown PIT mutation status: " + status
                );
            }
        }
    }

    /** Finds the mutated class element inside a PIT mutation.
     *
     * @param mutation mutation XML element
     * @return mutated class element
     * @throws IllegalStateException if the class element is missing
     */
    private static Element getMutatedClass(Element mutation) {
        NodeList classes =
                mutation.getElementsByTagName("mutatedClass");

        if (classes.getLength() == 0) {
            throw new IllegalStateException(
                    "mutatedClass element not found"
            );
        }

        return (Element) classes.item(0);
    }

    /**
     * Extracts a package name from a fully qualified class name.
     *
     * @param className fully qualified class name
     * @return package name
     * @throws IllegalArgumentException if the class name is invalid
     */
    private static String getPackageName(String className) {
        int separator = className.lastIndexOf('.');

        if (separator <= 0 || separator == className.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid mutated class name: " + className
            );
        }

        return className.substring(0, separator);
    }

    /** Determines whether a PIT mutation status represents
     * a viable mutation that was not killed.
     *
     * @param status PIT mutation status
     * @return true if the mutation contributes to the denominator
     */
    private static boolean isViableUnkilledStatus(String status) {
        return switch (status) {
            case "SURVIVED",
                 "NO_COVERAGE",
                 "TIMED_OUT",
                 "MEMORY_ERROR",
                 "RUN_ERROR",
                 "STARTED",
                 "NOT_STARTED" -> true;
            default -> false;
        };
    }

    /** Generates a mutation badge for a package.
     *
     * @param packageName package name
     * @param percentage measured mutation coverage
     * @param threshold minimum required mutation coverage
     * @throws Exception if the badge cannot be written.*/
    private static void generateBadge(
            String packageName,
            int percentage,
            int threshold
    ) throws Exception {

        String color = BadgePalette.colorFor(
                percentage,
                threshold,
                Math.max(0, threshold - 10)
        );

        writeSvg(
                "mutation " + packageName,
                percentage + "%",
                color,
                OUTPUT_DIRECTORY.resolve(packageName + ".svg")
        );
    }

    /** Writes an SVG mutation badge to the specified destination.
     *
     * @param label badge label
     * @param value displayed mutation percentage
     * @param color badge value background color
     * @param output destination path
     * @throws Exception if the SVG cannot be written.*/
    private static void writeSvg(
            String label,
            String value,
            String color,
            Path output
    ) throws Exception {

        int labelWidth = label.length() * 7 + 10;
        int valueWidth = 40;
        int width = labelWidth + valueWidth;

        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg"
                     width="%d" height="20"
                     role="img"
                     aria-label="%s: %s">
                  <rect width="%d" height="20"
                        rx="3" fill="#555"/>
                  <rect x="%d" width="%d"
                        height="20" fill="%s"/>
                  <g fill="#fff"
                     text-anchor="middle"
                     font-family="Verdana,sans-serif"
                     font-size="11">
                    <text x="%d" y="14">%s</text>
                    <text x="%d" y="14">%s</text>
                  </g>
                </svg>
                """.formatted(
                width,
                label,
                value,
                width,
                labelWidth,
                valueWidth,
                color,
                labelWidth / 2,
                label,
                labelWidth + valueWidth / 2,
                value
        );

        Files.writeString(
                output,
                svg,
                StandardCharsets.UTF_8
        );
    }
}


