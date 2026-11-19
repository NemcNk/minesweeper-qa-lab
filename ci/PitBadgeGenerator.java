package ci;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PitBadgeGenerator {

    private static final Path REPORT =
            Path.of("target/pit-reports/mutations.xml");

    private static final Path OUTPUT =
            Path.of("badges/mutation.svg");

    private PitBadgeGenerator() {
    }

    public static void main(String[] args) throws Exception {
        if (!Files.exists(REPORT)) {
            writeUnavailableBadge();
            System.out.println("PIT report not found");
            return;
        }

        Document document = loadReport();

        NodeList mutations = document
                .getElementsByTagName("mutation");

        int killed = 0;
        int total = 0;

        for (int i = 0; i < mutations.getLength(); i++) {
            Element mutation = (Element) mutations.item(i);

            String status = mutation.getAttribute("status");

            if ("NON_VIABLE".equals(status)) {
                continue;
            }

            total++;

            if ("KILLED".equals(status)) {
                killed++;
            }
        }

        if (total == 0) {
            writeUnavailableBadge();
            System.out.println("No viable mutations found");
            return;
        }

        int percentage = (int) Math.round(
                killed * 100.0 / total
        );

        generateBadge(percentage);

        System.out.printf(
                "Mutation coverage: %d%% (%d/%d)%n",
                percentage,
                killed,
                total
        );
    }

    private static Document loadReport() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();

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

    private static void generateBadge(int percentage)
            throws Exception {

        String color = percentage >= 90
                ? "#4c1"
                : percentage >= 70
                ? "#dfb317"
                : "#e05d44";

        String value = percentage + "%";

        writeSvg(value, color);
    }

    private static void writeUnavailableBadge()
            throws Exception {

        writeSvg("N/A", "#9f9f9f");
    }

    private static void writeSvg(
            String value,
            String color
    ) throws Exception {

        String label = "mutation";
        int labelWidth = label.length() * 7 + 10;
        int valueWidth = 40;
        int width = labelWidth + valueWidth;

        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg"
                     width="%d" height="20"
                     role="img"
                     aria-label="mutation: %s">
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

        Files.createDirectories(OUTPUT.getParent());

        Files.writeString(
                OUTPUT,
                svg,
                StandardCharsets.UTF_8
        );
    }
}
