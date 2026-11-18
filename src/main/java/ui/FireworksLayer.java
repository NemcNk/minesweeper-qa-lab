package ui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

/**
 * A layer of fireworks.
 * It does not catch a mouse and does not go beyond its limits.
 */
final class FireworksLayer extends Pane {

    private static final Color[] COLORS = {
            Color.web("#ffd166"), Color.web("#ef476f"), Color.web("#06d6a0"),
            Color.web("#4cc9f0"), Color.web("#f72585"), Color.web("#ffffff")
    };

    private final Random random = new Random();

    private Timeline launcher;

    FireworksLayer() {

        setMouseTransparent(true);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }

    void start() {

        stop();

        launcher = new Timeline(
                new KeyFrame(Duration.millis(550), e -> launch())
        );
        launcher.setDelay(Duration.millis(300));
        launcher.setCycleCount(Animation.INDEFINITE);
        launcher.play();
    }

    void stop() {

        if (launcher != null) {
            launcher.stop();
            launcher = null;
        }

        getChildren().clear();
    }

    private void launch() {

        double w = getWidth();
        double h = getHeight();

        if (w <= 0 || h <= 0) {
            return;
        }

        double x = w * (0.1 + 0.8 * random.nextDouble());
        double y = h * (0.1 + 0.4 * random.nextDouble());
        Color color = COLORS[random.nextInt(COLORS.length)];

        // Rocket takes off from below
        Circle rocket = new Circle(x, h, 2.5, Color.WHITE);
        getChildren().add(rocket);

        TranslateTransition up = new TranslateTransition(
                Duration.millis(450),
                rocket
        );
        up.setToY(y - h);
        up.setInterpolator(Interpolator.EASE_OUT);
        up.setOnFinished(e -> {
            getChildren().remove(rocket);
            burst(x, y, color);
        });
        up.play();
    }

    private void burst(double x, double y, Color color) {

        int count = 30;

        for (int i = 0; i < count; i++) {

            Circle spark = new Circle(
                    x, y, 2 + random.nextDouble() * 2, color
            );
            getChildren().add(spark);

            double angle = (2 * Math.PI / count) * i
                    + (random.nextDouble() - 0.5) * 0.3;
            double speed = 50 + random.nextDouble() * 80;
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed + 45;   // +45: «gravitation»

            Timeline t = new Timeline(
                    new KeyFrame(Duration.millis(1200),
                            new KeyValue(spark.translateXProperty(), dx, Interpolator.EASE_OUT),
                            new KeyValue(spark.translateYProperty(), dy, Interpolator.EASE_OUT),
                            new KeyValue(spark.opacityProperty(), 0, Interpolator.EASE_IN),
                            new KeyValue(spark.radiusProperty(), 0.5, Interpolator.LINEAR))
            );
            t.setOnFinished(e -> getChildren().remove(spark));
            t.play();
        }
    }
}
