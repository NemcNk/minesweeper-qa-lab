package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/** A seven-segment LED display for showing numbers and time.
 */
public final class SevenSegmentDisplay extends Canvas {

    /** Defines the width of a single digit in the original coordinate system. */
    private static final int DIGIT_WIDTH = 30;

    /** Defines the default display width for a three-digit counter. */
    private static final double MINE_WIDTH = 60;

    /** Defines the height of the display on the screen. */
    private static final double DISPLAY_HEIGHT = 27;

    /** Defines the original height used for drawing the segments. */
    private static final double ORIGINAL_HEIGHT = 42;

    /** Defines the horizontal scaling factor for the display. */
    private static final double SCALE_X = 2.0 / 3.0;

    /** Stores the number of digits displayed by the counter. */
    private final int digits;

    /** Indicates whether a colon separator should be displayed. */
    private final boolean showColon;

    /** Stores the vertical scaling factor used for drawing. */
    private final double scaleY;

    /** Defines the color of illuminated segments. */
    private static final Color ON = Color.rgb(255, 0, 0);

    /** Defines the color of inactive segments. */
    private static final Color OFF = Color.rgb(65, 0, 0);

    /** Defines which segments must be illuminated for each decimal digit.
     * Each row represents a digit from zero to nine.
     */
    private static final boolean[][] SEGMENTS = {
            {true, true, true, true, true, true, false},
            {false, true, true, false, false, false, false},
            {true, true, false, true, true, false, true},
            {true, true, true, true, false, false, true},
            {false, true, true, false, false, true, true},
            {true, false, true, true, false, true, true},
            {true, false, true, true, true, true, true},
            {true, true, true, false, false, false, false},
            {true, true, true, true, true, true, true},
            {true, true, true, true, false, true, true}
    };

    /** Stores the position and dimensions of each segment.
     * Each row contains the horizontal position, vertical position,
     * width, and height of the corresponding segment.
     */
    private static final double[][] POSITIONS = {
            {5, 2, 18, 4},
            {23, 5, 4, 14},
            {23, 22, 4, 14},
            {5, 37, 18, 4},
            {1, 22, 4, 14},
            {1, 5, 4, 14},
            {5, 19, 18, 4}
    };

    /** Creates a display with three digits and no colon separator.
     */
    public SevenSegmentDisplay() {
        this(3, false);
    }

    /** Creates a seven-segment display with the specified configuration.
     * @param digits number of digits
     * @param showColon whether to display a colon.*/
    public SevenSegmentDisplay(int digits, boolean showColon) {
        super(
                showColon
                        ? (digits * DIGIT_WIDTH + 8) * SCALE_X
                        : MINE_WIDTH,
                DISPLAY_HEIGHT
        );

        // Ensure that the display contains at least one digit.
        if (digits <= 0) {
            throw new IllegalArgumentException(
                    "Digits must be positive"
            );
        }

        this.digits = digits;
        this.showColon = showColon;

        // Calculate the vertical scaling factor from the display height.
        this.scaleY = getHeight() / ORIGINAL_HEIGHT;

        // Initialize the display with a zero value.
        setValue(0);
    }

    /** Sets the numeric value displayed, limiting it to the supported range.
     * @param value value to display.*/
    public void setValue(int value) {

        // Calculate the maximum value supported by the configured digit count.
        int max = (int) Math.pow(10, digits) - 1;

        // Keep the value within the valid display range.
        value = Math.max(0, Math.min(value, max));

        // Format the number with leading zeros when necessary.
        String text = String.format(
                "%0" + digits + "d",
                value
        );

        drawDisplay(text);
    }

    /** Sets the time displayed in minutes and seconds.
     * @param totalSeconds total time in seconds.*/
    public void setTime(int totalSeconds) {

        // Prevent negative time values.
        totalSeconds = Math.max(0, totalSeconds);

        // Convert the total time into minutes and remaining seconds.
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        // Format the time with two digits for the seconds component.
        String timeText = String.format(
                "%d%02d",
                minutes,
                seconds
        );

        // Adjust the display width to fit the formatted time.
        setWidth(
                (timeText.length() * DIGIT_WIDTH + 8) * SCALE_X
        );

        drawDisplay(timeText);
    }

    /** Draws a single digit using seven individual segments.
     * @param gc graphics context
     * @param digit digit to draw
     * @param x horizontal position.*/
    private void drawDigit(
            GraphicsContext gc,
            int digit,
            double x
    ) {
        // Retrieve the activation state of each segment for the given digit.
        boolean[] active = SEGMENTS[digit];

        // Draw all seven segments using their predefined positions.
        for (int i = 0; i < 7; i++) {
            double[] p = POSITIONS[i];

            if (active[i]) {
                // Apply the illuminated color and glow effect.
                gc.setFill(ON);

                gc.setEffect(
                        new DropShadow(
                                4,
                                Color.rgb(255, 0, 0, 0.65)
                        )
                );
            } else {
                // Use the inactive color without a glow effect.
                gc.setEffect(null);
                gc.setFill(OFF);
            }

            // Draw the current segment with rounded corners.
            gc.fillRoundRect(
                    x + p[0],
                    p[1],
                    p[2],
                    p[3],
                    3,
                    3
            );
        }

        // Reset the drawing effect after rendering the digit.
        gc.setEffect(null);
    }

    /** Draws the complete display using the specified text.
     * @param text text to display.*/
    private void drawDisplay(String text) {
        GraphicsContext gc = getGraphicsContext2D();

        // Save the current graphics state before applying transformations.
        gc.save();

        // Clear the canvas by filling it with a black background.
        gc.setEffect(null);
        gc.setFill(Color.BLACK);

        gc.fillRect(
                0,
                0,
                getWidth(),
                getHeight()
        );

        // Apply horizontal and vertical scaling to the drawing.
        double scaleX = SCALE_X;

        gc.scale(scaleX, scaleY);

        // Determine the position of the colon separator when enabled.
        int colonPosition = showColon
                ? text.length() - 2
                : -1;

        // Draw each character at its corresponding horizontal position.
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            // Skip empty positions without drawing a digit.
            if (character == ' ') {
                continue;
            }

            int digit = character - '0';

            double x = i * DIGIT_WIDTH;

            // Shift the digits after the colon to make room for the separator.
            if (showColon && i >= colonPosition) {
                x += 8;
            }

            drawDigit(gc, digit, x);
        }

        // Draw the two illuminated dots of the colon separator.
        if (showColon) {
            double x = colonPosition * DIGIT_WIDTH + 1;

            gc.setEffect(null);
            gc.setFill(ON);

            gc.fillOval(x, 12, 4, 4);
            gc.fillOval(x, 27, 4, 4);
        }

        // Restore the original graphics state.
        gc.restore();
    }
}







