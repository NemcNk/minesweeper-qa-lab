package ci;

/** Provides a shared color palette for generated quality badges.
 * <p>Colors are inspired by the GitHub dark theme.</p>*/
public final class BadgePalette {

    /** Color used for successful results. */
    public static final String GREEN = "#3FB950";

    /** Color used for intermediate results. */
    public static final String YELLOW = "#D29922";

    /** Color used for low results. */
    public static final String RED = "#F85149";

    /** Color used for unavailable results. */
    public static final String GRAY = "#8B949E";

    /** Prevents instantiation of this utility class.*/
    private BadgePalette() {
        throw new IllegalStateException(
                "Utility class must not be instantiated"
        );
    }

    /** Returns a color according to the measured percentage.
     *
     * @param percentage measured percentage
     * @param greenThreshold minimum percentage for green
     * @param yellowThreshold minimum percentage for yellow
     * @return corresponding hexadecimal color.*/
    public static String colorFor(
            int percentage,
            int greenThreshold,
            int yellowThreshold
    ) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(
                    "Percentage must be between 0 and 100"
            );
        }

        if (yellowThreshold < 0
                || greenThreshold > 100
                || greenThreshold < yellowThreshold) {
            throw new IllegalArgumentException(
                    "Invalid color thresholds"
            );
        }

        if (percentage >= greenThreshold) {
            return GREEN;
        }

        if (percentage >= yellowThreshold) {
            return YELLOW;
        }

        return RED;
    }
}
