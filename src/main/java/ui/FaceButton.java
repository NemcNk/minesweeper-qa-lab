package ui;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

public final class FaceButton extends Canvas {

    public enum Face {
        PLAYING,    // 🙂
        LOST,       // 😵
        WON         // 🤩
    }

    private static final int SIZE = 42;

    private static final Color INK = Color.web("#4A2F12");

    private static final RadialGradient HEAD = new RadialGradient(
            0, 0,
            0.5, 0.32, 0.65,
            true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#FFEE9A")),
            new Stop(0.55, Color.web("#FFCB3D")),
            new Stop(1.0, Color.web("#F2A01E"))
    );

    private Face face = Face.PLAYING;

    public FaceButton(Runnable restartAction) {

        super(SIZE, SIZE);

        setCursor(Cursor.HAND);

        setOnMousePressed(event -> {
            setScaleX(0.92);
            setScaleY(0.92);
        });

        setOnMouseReleased(event -> {
            setScaleX(1);
            setScaleY(1);
        });

        setOnMouseClicked(event -> restartAction.run());

        draw();
    }

    public void setFace(Face newFace) {

        if (newFace == face) {
            return;
        }

        face = newFace;
        draw();

        if (newFace == Face.WON || newFace == Face.LOST) {
            pop();
        }
    }

    private void pop() {

        ScaleTransition t = new ScaleTransition(Duration.millis(140), this);
        t.setFromX(0.8);
        t.setFromY(0.8);
        t.setToX(1);
        t.setToY(1);
        t.setInterpolator(Interpolator.EASE_OUT);
        t.play();
    }

    // drawing

    private void draw() {

        GraphicsContext gc = getGraphicsContext2D();

        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.save();
        gc.scale(getWidth() / 100.0, getHeight() / 100.0);

        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        drawHead(gc);

        switch (face) {
            case PLAYING -> drawPlaying(gc);
            case LOST -> drawLost(gc);
            case WON -> drawWon(gc);
        }

        gc.restore();
    }

    private void drawHead(GraphicsContext gc) {

        gc.setFill(HEAD);
        gc.fillOval(4, 4, 92, 92);

        gc.setStroke(Color.web("#D98A0B"));
        gc.setLineWidth(2);
        gc.strokeOval(5, 5, 90, 90);

        gc.setFill(Color.color(1, 1, 1, 0.28));
        gc.fillOval(22, 8, 56, 26);
    }

    // 🙂
    private void drawPlaying(GraphicsContext gc) {

        eye(gc, 35, 41, 4.5, 6.5);
        eye(gc, 65, 41, 4.5, 6.5);

        gc.setStroke(INK);
        gc.setLineWidth(5);

        gc.beginPath();
        gc.moveTo(32, 63);
        gc.quadraticCurveTo(50, 76, 68, 63);
        gc.stroke();
    }

    // 😵
    private void drawLost(GraphicsContext gc) {

        brows(gc);

        gc.setStroke(INK);
        gc.setLineWidth(4.5);

        cross(gc, 35, 43, 7);
        cross(gc, 65, 43, 7);

        openMouth(gc);
    }

    private static void brows(GraphicsContext gc) {

        gc.setStroke(INK);
        gc.setLineWidth(4);

        gc.beginPath();
        gc.moveTo(25, 28);
        gc.quadraticCurveTo(33, 19, 42, 25);
        gc.stroke();

        gc.beginPath();
        gc.moveTo(58, 25);
        gc.quadraticCurveTo(67, 19, 75, 28);
        gc.stroke();
    }

    private static void openMouth(GraphicsContext gc) {

        gc.setFill(INK);
        gc.fillOval(42, 60, 16, 24);
    }

    // 🤩
    private void drawWon(GraphicsContext gc) {

        star(gc, 34, 40, 14, 6);
        star(gc, 66, 40, 14, 6);

        // mouth
        gc.beginPath();
        gc.moveTo(26, 58);
        gc.lineTo(74, 58);
        gc.quadraticCurveTo(74, 86, 50, 86);
        gc.quadraticCurveTo(26, 86, 26, 58);
        gc.closePath();

        gc.setFill(INK);
        gc.fill();

        gc.save();
        gc.clip();

        gc.setFill(Color.WHITE);
        gc.fillPolygon(
                new double[]{26, 74, 72, 28},
                new double[]{58, 58, 66, 66},
                4
        );

        gc.setFill(Color.web("#FF6B7A"));
        gc.fillOval(37, 76, 26, 16);

        gc.restore();
    }

    // helpers

    private static void eye(GraphicsContext gc,
                            double cx, double cy,
                            double rx, double ry) {

        gc.setFill(INK);
        gc.fillOval(cx - rx, cy - ry, rx * 2, ry * 2);
    }

    private static void cross(GraphicsContext gc,
                              double cx, double cy, double r) {

        gc.beginPath();
        gc.moveTo(cx - r, cy - r);
        gc.lineTo(cx + r, cy + r);
        gc.moveTo(cx + r, cy - r);
        gc.lineTo(cx - r, cy + r);
        gc.stroke();
    }

    private static void star(GraphicsContext gc,
                             double cx, double cy,
                             double outer, double inner) {

        double[] xs = new double[10];
        double[] ys = new double[10];

        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0) ? outer : inner;
            xs[i] = cx + r * Math.cos(angle);
            ys[i] = cy + r * Math.sin(angle);
        }

        gc.setFill(Color.web("#FF8F00"));
        gc.fillPolygon(xs, ys, 10);

        gc.setStroke(Color.web("#B85C00"));
        gc.setLineWidth(2);
        gc.strokePolygon(xs, ys, 10);
    }
}

