package ci;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JacocoBadgeGenerator {

    private static final Path REPORT =
            Path.of("target/site/jacoco/jacoco.xml");

    private static final Path OUTPUT =
            Path.of("badges");

    private JacocoBadgeGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Document document = loadReport();

        // Core package line coverage.
        Element corePackage = findCorePackage(document);

        int[] lines = getCounter(
                corePackage,
                "LINE"
        );

        generateBadge(
                "core-coverage.svg",
                "core coverage",
                lines[0],
                lines[1]
        );
    }

    private static Document loadReport() throws Exception {
        if (!Files.exists(REPORT)) {
            throw new IllegalStateException(
                    "JaCoCo report not found: " + REPORT
            );
        }

        var factory = DocumentBuilderFactory.newInstance();

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING,
                true
        );

        // JaCoCo XML contains a DOCTYPE declaration.
        // Allow DOCTYPE, but disable external entity processing.

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

    private static Element findCorePackage(
            Document document
    ) {
        NodeList packages = document
                .getElementsByTagName("package");

        for (int i = 0; i < packages.getLength(); i++) {
            Element element = (Element) packages.item(i);

            if ("core".equals(
                    element.getAttribute("name"))) {
                return element;
            }
        }

        throw new IllegalStateException(
                "Package 'core' not found in JaCoCo report"
        );
    }


    private static int[] getCounter(
            Element element,
            String counterType
    ) {
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element counter)) {
                continue;
            }

            if (!"counter".equals(counter.getTagName())) {
                continue;
            }

            if (counterType.equals(
                    counter.getAttribute("type"))) {

                int covered = Integer.parseInt(
                        counter.getAttribute("covered")
                );

                int missed = Integer.parseInt(
                        counter.getAttribute("missed")
                );

                if (covered + missed == 0) {
                    throw new IllegalStateException(
                            "Empty counter: " + counterType
                    );
                }

                return new int[]{covered, missed};
            }
        }

        throw new IllegalStateException(
                "Direct counter not found: " + counterType
        );
    }

    private static void generateBadge(
            String filename,
            String label,
            int covered,
            int missed
    ) throws Exception {

        int total = covered + missed;

        int percentage = (int) Math.round(
                covered * 100.0 / total
        );

        String color = percentage >= 80
                ? "#3FB950"
                : percentage >= 60
                ? "#D29922"
                : "#F85149";

        String value = percentage + "%";

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

        Files.createDirectories(OUTPUT);

        Files.writeString(
                OUTPUT.resolve(filename),
                svg,
                StandardCharsets.UTF_8
        );

        System.out.printf(
                "%s: %d%% (%d/%d)%n",
                label,
                percentage,
                covered,
                total
        );
    }
}

