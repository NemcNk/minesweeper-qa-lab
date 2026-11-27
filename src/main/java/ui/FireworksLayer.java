package ui;

import java.util.Random;

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

/**
 * A transparent layer that displays animated fireworks.
 * The layer ignores mouse events and clips all effects
 * to its own boundaries.
 */
final class FireworksLayer extends Pane {

    /** Defines the colors used for the fireworks particles.
     */
    private static final Color[] COLORS = {
            Color.web("#ffd166"), Color.web("#ef476f"), Color.web("#06d6a0"),
            Color.web("#4cc9f0"), Color.web("#f72585"), Color.web("#ffffff")
    };

    /** Generates random positions, colors, and particle movement values. */
    private final Random random = new Random();

    /** Controls the repeated launching of fireworks. */
    private Timeline launcher;

    /** Creates a fireworks layer and configures its mouse transparency
     * and clipping boundaries.*/
    FireworksLayer() {
        setMouseTransparent(true);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }

    /** Starts the fireworks animation by periodically launching rockets.
     * Stops any previously running animation before starting a new one.
     */
    void start() {
        stop();

        launcher = new Timeline(
                new KeyFrame(Duration.millis(550), e -> launch())
        );
        launcher.setDelay(Duration.millis(300));
        launcher.setCycleCount(Animation.INDEFINITE);
        launcher.play();
    }

    /** Stops the fireworks animation and removes all active visual effects.
     */
    void stop() {
        if (launcher != null) {
            launcher.stop();
            launcher = null;
        }

        getChildren().clear();
    }

    /** Launches a rocket from the bottom of the layer to a random position.
     * When the rocket reaches its destination, it creates an explosion.
     */
    private void launch() {
        double w = getWidth();
        double h = getHeight();

        if (w <= 0 || h <= 0) {
            return;
        }

        double x = w * (0.1 + 0.8 * random.nextDouble());
        double y = h * (0.1 + 0.4 * random.nextDouble());

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
            burst(x, y, COLORS[random.nextInt(COLORS.length)]);
        });
        up.play();
    }

    /** Creates an explosion by generating multiple colored particles
     * that move outward, gradually fade, and shrink.
     *
     * @param x the horizontal coordinate of the explosion center
     * @param y the vertical coordinate of the explosion center
     * @param color the color applied to all particles in the explosion
     */
    private void burst(double x, double y, Color color) {
        int count = 30;

        for (int i = 0; i < count; i++) {
            Circle spark = new Circle(
                    x, y, 2 + random.nextDouble() * 2, color
            );
            getChildren().add(spark);

            double angle = 2 * Math.PI / count * i
                    + (random.nextDouble() - 0.5) * 0.3;
            double speed = 50 + random.nextDouble() * 80;
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed + 45;

            Timeline t = new Timeline(
                    new KeyFrame(Duration.millis(1200),
                            new KeyValue(
                                    spark.translateXProperty(), dx,
                                    Interpolator.EASE_OUT
                            ),
                            new KeyValue(
                                    spark.translateYProperty(), dy,
                                    Interpolator.EASE_OUT
                            ),
                            new KeyValue(
                                    spark.opacityProperty(), 0,
                                    Interpolator.EASE_IN
                            ),
                            new KeyValue(
                                    spark.radiusProperty(), 0.5,
                                    Interpolator.LINEAR
                            )
                    )
            );
            t.setOnFinished(e -> getChildren().remove(spark));
            t.play();
        }
    }
}

