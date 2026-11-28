package ci;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Generates JaCoCo line coverage badges for project packages
 * and an overall core coverage badge.
 *
 * <p>The generator reads one JaCoCo XML report and creates
 * five package badges in {@code badges/jacoco} and one
 * overall badge in {@code badges/core-coverage.svg}.</p>*/
public final class JacocoBadgeGenerator {

    /** Path to the JaCoCo XML report. */
    private static final Path REPORT =
            Path.of("target/site/jacoco/jacoco.xml");

    /** Directory containing package badges. */
    private static final Path OUTPUT_DIRECTORY =
            Path.of("badges/jacoco");

    /** Path to the overall core coverage badge. */
    private static final Path CORE_BADGE =
            Path.of("badges/core-coverage.svg");

    /** Package names included in coverage analysis. */
    private static final String[] PACKAGES = {
            "core",
            "logging",
            "session",
            "model",
            "stats"
    };

    /** Minimum line coverage required for each package. */
    private static final int[] THRESHOLDS = {
            90,
            80,
            90,
            90,
            85
    };

    /** Prevents instantiation of this utility class. */
    private JacocoBadgeGenerator() {
        throw new IllegalStateException(
                "Utility class must not be instantiated"
        );
    }

    /** Generates package and overall coverage badges.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if the report is invalid or a threshold fails
     */
    public static void main(String[] args) throws Exception {
        Document document = loadReport();

        Files.createDirectories(OUTPUT_DIRECTORY);
        Files.createDirectories(CORE_BADGE.getParent());

        // Generate the overall core badge.
        generateCoreBadge(document);

        boolean thresholdFailed = false;

        for (int i = 0; i < PACKAGES.length; i++) {
            String packageName = PACKAGES[i];
            int threshold = THRESHOLDS[i];

            Element packageElement =
                    findPackage(document, packageName);

            int[] counters = getLineCounter(packageElement);

            int covered = counters[0];
            int missed = counters[1];
            int total = covered + missed;

            int percentage = calculatePercentage(
                    covered,
                    total
            );

            generateBadge(packageName, percentage, threshold);

            boolean passed = percentage >= threshold;

            System.out.printf(
                    "%s: %d%% (%d/%d), required: %d%% [%s]%n",
                    packageName,
                    percentage,
                    covered,
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
                    "JaCoCo package coverage threshold not met"
            );
        }
    }

    /** Generates the overall core coverage badge.
     *
     * @param document parsed JaCoCo XML document
     * @throws Exception if the badge cannot be written.*/
    private static void generateCoreBadge(
            Document document
    ) throws Exception {

        Element corePackage = findPackage(document, "core");

        int[] counters = getLineCounter(corePackage);

        int covered = counters[0];
        int missed = counters[1];
        int total = covered + missed;

        int percentage = calculatePercentage(covered, total);

        String color = BadgePalette.colorFor(
                percentage,
                90,
                70
        );

        writeSvg(
                "core coverage",
                percentage + "%",
                color,
                CORE_BADGE
        );

        System.out.printf(
                "Overall core coverage: %d%% (%d/%d)%n",
                percentage,
                covered,
                total
        );
    }

    /** Calculates a percentage safely.
     *
     * @param covered number of covered items
     * @param total total number of items
     * @return rounded percentage
     * @throws IllegalStateException if total is zero.*/
    private static int calculatePercentage(
            int covered,
            int total
    ) {
        if (total <= 0) {
            throw new IllegalStateException(
                    "Coverage counter contains no lines"
            );
        }

        return (int) Math.round(
                covered * 100.0 / total
        );
    }

    /**
     * Loads the JaCoCo XML report with external entity processing
     * disabled.
     *
     * @return parsed XML document
     * @throws Exception if the report is missing or invalid
     */
    private static Document loadReport() throws Exception {
        if (!Files.isRegularFile(REPORT)) {
            throw new IllegalStateException(
                    "JaCoCo report not found: " + REPORT
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

    /** Finds a package by its exact name in the JaCoCo report.
     *
     * @param document parsed JaCoCo XML document
     * @param packageName package name to find
     * @return matching package element
     * @throws IllegalStateException if the package is missing.*/
    private static Element findPackage(
            Document document,
            String packageName
    ) {
        NodeList packages =
                document.getElementsByTagName("package");

        for (int i = 0; i < packages.getLength(); i++) {
            Element element = (Element) packages.item(i);

            if (packageName.equals(
                    element.getAttribute("name"))) {
                return element;
            }
        }

        throw new IllegalStateException(
                "Package not found in JaCoCo report: "
                        + packageName
        );
    }

    /** Reads the direct LINE coverage counter of a package.
     *
     * @param packageElement package XML element
     * @return array containing covered and missed lines
     * @throws IllegalStateException if the counter is missing
     *                                or contains invalid values
     */
    private static int[] getLineCounter(Element packageElement) {
        NodeList children = packageElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element counter)) {
                continue;
            }

            if (!"counter".equals(counter.getTagName())
                    || !"LINE".equals(
                    counter.getAttribute("type"))) {
                continue;
            }

            int covered = Integer.parseInt(
                    counter.getAttribute("covered")
            );

            int missed = Integer.parseInt(
                    counter.getAttribute("missed")
            );

            if (covered < 0 || missed < 0
                    || covered + missed == 0) {
                throw new IllegalStateException(
                        "Invalid LINE counter"
                );
            }

            return new int[]{covered, missed};
        }

        throw new IllegalStateException(
                "LINE counter not found"
        );
    }

    /** Generates a coverage badge for a package.
     *
     * @param packageName package name
     * @param percentage measured coverage percentage
     * @param threshold minimum required coverage
     * @throws Exception if the badge cannot be written.*/
    private static void generateBadge(
            String packageName,
            int percentage,
            int threshold
    ) throws Exception {

        String color = BadgePalette.colorFor(
                percentage,
                threshold,
                Math.max(0, threshold - 20)
        );

        String label = "jacoco " + packageName;

        writeSvg(
                label,
                percentage + "%",
                color,
                OUTPUT_DIRECTORY.resolve(packageName + ".svg")
        );
    }

    /** Writes an SVG badge to the specified destination.
     *
     * @param label badge label
     * @param value displayed percentage
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



