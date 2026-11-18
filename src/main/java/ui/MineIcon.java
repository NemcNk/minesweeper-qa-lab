package ui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * A vector mine icon that scales to fit the cell size.
 */
final class MineIcon extends Group {

    // The original drawing is in square 100x100
    private static final double SCALE_PER_CELL_PIXEL = 0.0095;

    private static final Color DARK_SPIKE = Color.web("#2a5a8a");
    private static final Color LIGHT_SPIKE = Color.web("#6bb9d0");
    private static final Color BODY = Color.web("#2980b9");
    private static final Color DOTS = Color.web("#243246");
    private static final Color BAR = Color.web("#4a5a6a");

    private static final double[][] DOT_POSITIONS = {
            {50, 33}, {50, 67},
            {64.7, 41.5}, {35.3, 41.5},
            {64.7, 58.5}, {35.3, 58.5}
    };

    MineIcon(int cellSize) {

        // dark spikes (diagonal lines at the top and bottom).
        getChildren().addAll(
                spike(65, 24, 70, 15.4, DARK_SPIKE),
                spike(35, 24, 30, 15.4, DARK_SPIKE),
                spike(65, 76, 70, 84.6, DARK_SPIKE),
                spike(35, 76, 30, 84.6, DARK_SPIKE)
        );

        // light spikes
        getChildren().addAll(
                spike(50, 24, 50, 8, LIGHT_SPIKE),
                spike(50, 76, 50, 92, LIGHT_SPIKE),
                spike(72.5, 37, 86.4, 29, LIGHT_SPIKE),
                spike(27.5, 37, 13.6, 29, LIGHT_SPIKE),
                spike(72.5, 63, 86.4, 71, LIGHT_SPIKE),
                spike(27.5, 63, 13.6, 71, LIGHT_SPIKE)
        );

        getChildren().add(new Circle(50, 50, 29, BODY));

        for (double[] p : DOT_POSITIONS) {
            getChildren().add(new Circle(p[0], p[1], 4.5, DOTS));
        }

        Line bar = new Line(10, 50, 90, 50);
        bar.setStroke(BAR);
        bar.setStrokeWidth(2.5);
        bar.setStrokeLineCap(StrokeLineCap.ROUND);
        getChildren().add(bar);

        double scale = cellSize * SCALE_PER_CELL_PIXEL;

        setScaleX(scale);
        setScaleY(scale);
    }

    private static Line spike(
            double x1, double y1,
            double x2, double y2,
            Color color
    ) {

        Line line = new Line(x1, y1, x2, y2);

        line.setStroke(color);
        line.setStrokeWidth(5);
        line.setStrokeLineCap(StrokeLineCap.ROUND);

        return line;
    }
}