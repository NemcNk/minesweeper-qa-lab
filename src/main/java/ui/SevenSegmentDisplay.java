package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

public final class SevenSegmentDisplay extends Canvas {

    private static final int DIGIT_WIDTH = 30;

    private static final double MINE_WIDTH = 60;
    private static final double DISPLAY_HEIGHT = 27;
    private static final double ORIGINAL_HEIGHT = 42;
    private static final double SCALE_X = 2.0 / 3.0;

    private final int digits;
    private final boolean showColon;

    private final double scaleY;

    private static final Color ON = Color.rgb(255, 0, 0);
    private static final Color OFF = Color.rgb(65, 0, 0);

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

    private static final double[][] POSITIONS = {

            {5, 2, 18, 4},
            {23, 5, 4, 14},
            {23, 22, 4, 14},
            {5, 37, 18, 4},
            {1, 22, 4, 14},
            {1, 5, 4, 14},
            {5, 19, 18, 4}
    };

    public SevenSegmentDisplay() {
        this(3, false);
    }

    public SevenSegmentDisplay(
            int digits,
            boolean showColon
    ) {

        super(
                showColon
                        ? (digits * DIGIT_WIDTH + 8) * SCALE_X
                        : MINE_WIDTH,
                DISPLAY_HEIGHT
        );

        if (digits <= 0) {
            throw new IllegalArgumentException(
                    "Digits must be positive"
            );
        }

        this.digits = digits;
        this.showColon = showColon;
        this.scaleY = getHeight() / ORIGINAL_HEIGHT;

        setValue(0);
    }

    public void setValue(int value) {

        int max = (int) Math.pow(10, digits) - 1;

        value = Math.max(0, Math.min(value, max));

        String text = String.format(
                "%0" + digits + "d",
                value
        );

        drawDisplay(text);
    }

    public void setTime(int totalSeconds) {

        totalSeconds = Math.max(0, totalSeconds);

        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        String timeText = String.format(
                "%d%02d",
                minutes,
                seconds
        );

        setWidth(
                (timeText.length() * DIGIT_WIDTH + 8) * SCALE_X
        );

        drawDisplay(timeText);
    }


    private void drawDigit(
            GraphicsContext gc,
            int digit,
            double x
    ) {

        boolean[] active = SEGMENTS[digit];

        for (int i = 0; i < 7; i++) {

            double[] p = POSITIONS[i];

            if (active[i]) {

                gc.setFill(ON);

                gc.setEffect(
                        new DropShadow(
                                4,
                                Color.rgb(255, 0, 0, 0.65)
                        )
                );

            } else {

                gc.setEffect(null);
                gc.setFill(OFF);
            }

            gc.fillRoundRect(
                    x + p[0],
                    p[1],
                    p[2],
                    p[3],
                    3,
                    3
            );
        }

        gc.setEffect(null);
    }

    private void drawDisplay(String text) {

        GraphicsContext gc = getGraphicsContext2D();

        gc.save();

        gc.setEffect(null);
        gc.setFill(Color.BLACK);

        gc.fillRect(
                0,
                0,
                getWidth(),
                getHeight()
        );

        double scaleX = SCALE_X;

        gc.scale(scaleX, scaleY);

        int colonPosition = showColon
                ? text.length() - 2
                : -1;

        for (int i = 0; i < text.length(); i++) {

            char character = text.charAt(i);

            if (character == ' ') {
                continue;
            }

            int digit = character - '0';

            double x = i * DIGIT_WIDTH;

            if (showColon && i >= colonPosition) {
                x += 8;
            }

            drawDigit(gc, digit, x);
        }

        if (showColon) {

            double x = colonPosition * DIGIT_WIDTH + 1;

            gc.setEffect(null);
            gc.setFill(ON);

            gc.fillOval(x, 12, 4, 4);
            gc.fillOval(x, 27, 4, 4);
        }

        gc.restore();
    }
}






