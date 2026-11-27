package ui;

import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** Displays an animated bomb icon with idle radar effects
 * and an explosion animation.
 * The component supports continuous idle animation, including
 * rotation and radar pulses, as well as a multi-stage explosion effect.
 */
public final class BombIconAnimation extends StackPane {

    /** Defines the colors used for explosion fragments.*/
    private static final Color[] SHARD_COLORS = {
            Color.web("#2980b9"),
            Color.web("#66b8cc"),
            Color.web("#285882"),
            Color.web("#3d5a6e"),
            Color.web("#2c3e50")
    };

    /** Defines the color used for radar rings. */
    private static final Color RING_COLOR = Color.web("#66b8cc");

    /** Defines the duration of one complete radar animation cycle. */
    private static final Duration RADAR_PERIOD = Duration.seconds(3);

    /** Represents the bomb image displayed by this component. */
    private final ImageView bomb;

    /** Contains all visual effects displayed above the bomb icon. */
    private final Pane fx = new Pane();

    /** Provides a glow effect around the bomb icon. */
    private final DropShadow glow = new DropShadow(0, Color.web("#66b8cc"));

    /** Generates random values for explosion effects and shaking. */
    private final Random random = new Random();

    /** Controls the continuous rotation of the bomb icon. */
    private final RotateTransition rotation;

    /** Controls the idle radar and pulse animations. */
    private final Timeline radar;

    /** Indicates whether an explosion animation is currently running. */
    private boolean busy = false;

    /** Creates a bomb icon with idle animation enabled.
     * @param image image displayed as the bomb icon
     */
    public BombIconAnimation(Image image) {
        this(image, true);
    }

    /** Creates a bomb icon with optional idle animation.
     * @param image image displayed as the bomb icon
     * @param idleAnimation whether idle animation should start automatically
     */
    public BombIconAnimation(Image image, boolean idleAnimation) {
        bomb = new ImageView(image);
        bomb.setFitWidth(85);
        bomb.setFitHeight(85);
        bomb.setPreserveRatio(true);
        bomb.setEffect(glow);

        // Keep the effects layer centered on the icon.
        fx.setPrefSize(0, 0);
        fx.setMinSize(0, 0);
        fx.setMaxSize(0, 0);
        fx.setMouseTransparent(true);

        getChildren().addAll(bomb, fx);
        setViewOrder(-1);

        // Configure continuous rotation with a linear interpolation.
        rotation = new RotateTransition(RADAR_PERIOD, bomb);
        rotation.setByAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.setCycleCount(Animation.INDEFINITE);

        // Create the idle pulse and radar wave animation.
        radar = new Timeline(
                new KeyFrame(Duration.millis(1), e -> {
                    spawnRing(0);
                    spawnRing(250);
                }),
                new KeyFrame(Duration.ZERO,
                        new KeyValue(bomb.scaleXProperty(), 1),
                        new KeyValue(bomb.scaleYProperty(), 1)),
                new KeyFrame(Duration.millis(180),
                        new KeyValue(bomb.scaleXProperty(), 1.12, Interpolator.EASE_OUT),
                        new KeyValue(bomb.scaleYProperty(), 1.12, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(650),
                        new KeyValue(bomb.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(bomb.scaleYProperty(), 1, Interpolator.EASE_BOTH)),
                new KeyFrame(RADAR_PERIOD,
                        new KeyValue(bomb.scaleXProperty(), 1),
                        new KeyValue(bomb.scaleYProperty(), 1))
        );

        radar.setCycleCount(Animation.INDEFINITE);

        // Automatically manage animation when the component enters or leaves a scene.
        if (idleAnimation) {
            sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) {
                    stop();
                } else {
                    start();
                }
            });
        }
    }

    /** Starts the idle rotation and radar animations.
     * Any previously running idle animations are stopped before restarting.
     */
    public void start() {
        stop();
        rotation.play();
        radar.play();
    }

    /** Stops all idle animations and restores the original icon state.
     * Also removes any remaining visual effects from the effects layer.
     */
    public void stop() {
        rotation.stop();
        radar.stop();
        resetBomb();
        fx.getChildren().clear();
        busy = false;
    }

    /** Plays the explosion animation and executes the callback afterward.
     * The explosion is ignored if another explosion is already running.
     * @param after action executed after the animation finishes;
     *              may be null if no callback is required
     */
    public void explodeThen(Runnable after) {
        if (busy) {
            return;
        }

        busy = true;

        // Stop radar waves while keeping the icon rotation active.
        radar.stop();

        PauseTransition boom = new PauseTransition(Duration.millis(1));
        boom.setOnFinished(e -> {
            rotation.stop();
            explode();
        });

        PauseTransition wait = new PauseTransition(Duration.millis(700));

        SequentialTransition seq = new SequentialTransition(
                new ParallelTransition(
                        chargeTimeline(350),
                        shakeTimeline(350)
                ),
                boom,
                wait
        );

        seq.setOnFinished(e -> {
            busy = false;

            if (after != null) {
                after.run();
            }
        });

        seq.play();
    }

    // -------------------- Idle radar effects --------------------

    /** Creates an expanding radar ring.
     * @param delayMs delay before the ring animation starts, in milliseconds
     */
    private void spawnRing(double delayMs) {
        Circle ring = new Circle(0, 0, 44);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(RING_COLOR);
        ring.setStrokeWidth(4);
        ring.setOpacity(0);

        fx.getChildren().add(ring);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(ring.opacityProperty(), 0.8)),
                new KeyFrame(Duration.millis(1400),
                        new KeyValue(ring.radiusProperty(), 105, Interpolator.EASE_OUT),
                        new KeyValue(ring.strokeWidthProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(ring.opacityProperty(), 0, Interpolator.EASE_IN))
        );

        timeline.setDelay(Duration.millis(delayMs));
        timeline.setOnFinished(e -> fx.getChildren().remove(ring));
        timeline.play();
    }

    // -------------------- Explosion preparation --------------------

    /** Creates the icon charging animation.
     * @param millis animation duration in milliseconds
     * @return timeline responsible for scaling the icon and increasing its glow
     */
    private Timeline chargeTimeline(double millis) {
        return new Timeline(
                new KeyFrame(Duration.millis(millis),
                        new KeyValue(bomb.scaleXProperty(), 1.2, Interpolator.EASE_IN),
                        new KeyValue(bomb.scaleYProperty(), 1.2, Interpolator.EASE_IN),
                        new KeyValue(glow.radiusProperty(), 30, Interpolator.EASE_IN))
        );
    }

    /** Creates a random shaking animation.
     * @param millis animation duration in milliseconds
     * @return timeline responsible for randomly moving the icon
     */
    private Timeline shakeTimeline(double millis) {
        int ticks = (int) (millis / 40);

        Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(40), e -> {
                    double amp = 1 + (bomb.getScaleX() - 1) * 30;

                    bomb.setTranslateX((random.nextDouble() - 0.5) * 2 * amp);
                    bomb.setTranslateY((random.nextDouble() - 0.5) * 2 * amp);
                })
        );

        shake.setCycleCount(ticks);

        shake.setOnFinished(e -> {
            bomb.setTranslateX(0);
            bomb.setTranslateY(0);
        });

        return shake;
    }

    /** Restores the bomb icon to its original visual state.
     * Resets its scale, rotation, position, opacity, and glow intensity.
     */
    private void resetBomb() {
        bomb.setScaleX(1);
        bomb.setScaleY(1);
        bomb.setRotate(0);
        bomb.setTranslateX(0);
        bomb.setTranslateY(0);
        bomb.setOpacity(1);
        glow.setRadius(0);
    }

    // -------------------- Explosion effects --------------------

    /** Hides the bomb icon and launches all explosion effects.
     * Starts the flash, shockwave, and fragment animations simultaneously.
     */
    private void explode() {
        bomb.setOpacity(0);
        glow.setRadius(0);
        bomb.setTranslateX(0);
        bomb.setTranslateY(0);

        spawnFlash();
        spawnShockwave();
        spawnShards();
    }

    /** Creates a short expanding flash at the explosion center.
     * The flash gradually increases in size and fades out.
     */
    private void spawnFlash() {
        Circle flash = new Circle(0, 0, 45, Color.web("#66b8cc"));
        flash.setOpacity(0.85);

        fx.getChildren().add(flash);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(flash.scaleXProperty(), 0.3),
                        new KeyValue(flash.scaleYProperty(), 0.3)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(flash.scaleXProperty(), 1.8, Interpolator.EASE_OUT),
                        new KeyValue(flash.scaleYProperty(), 1.8, Interpolator.EASE_OUT),
                        new KeyValue(flash.opacityProperty(), 0, Interpolator.EASE_IN))
        );

        timeline.setOnFinished(e -> fx.getChildren().remove(flash));
        timeline.play();
    }

    /** Creates an expanding shockwave ring.
     * The ring expands outward while its stroke gradually becomes thinner
     * and its opacity decreases.
     */
    private void spawnShockwave() {
        Circle ring = new Circle(0, 0, 20);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(Color.web("#2980b9"));
        ring.setStrokeWidth(8);

        fx.getChildren().add(ring);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(550),
                        new KeyValue(ring.radiusProperty(), 110, Interpolator.EASE_OUT),
                        new KeyValue(ring.strokeWidthProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(ring.opacityProperty(), 0, Interpolator.EASE_IN))
        );

        timeline.setOnFinished(e -> fx.getChildren().remove(ring));
        timeline.play();
    }

    /** Creates and animates randomly distributed explosion fragments.
     * Each fragment moves away from the explosion center, rotates,
     * and gradually fades out.
     */
    private void spawnShards() {
        int count = 22;

        for (int i = 0; i < count; i++) {
            double w = 5 + random.nextDouble() * 8;
            double h = 5 + random.nextDouble() * 8;

            Rectangle shard = new Rectangle(-w / 2, -h / 2, w, h);
            shard.setArcWidth(3);
            shard.setArcHeight(3);
            shard.setFill(SHARD_COLORS[random.nextInt(SHARD_COLORS.length)]);

            fx.getChildren().add(shard);

            double angle = 2 * Math.PI / count * i
                    + (random.nextDouble() - 0.5) * 0.5;
            double dist = 70 + random.nextDouble() * 70;

            // Move each fragment away from the explosion center.
            TranslateTransition move = new TranslateTransition(
                    Duration.millis(650 + random.nextDouble() * 250), shard
            );

            move.setToX(Math.cos(angle) * dist);
            move.setToY(Math.sin(angle) * dist);
            move.setInterpolator(Interpolator.EASE_OUT);

            // Rotate each fragment while it moves.
            RotateTransition spin = new RotateTransition(
                    Duration.millis(900), shard
            );

            spin.setByAngle((random.nextDouble() - 0.5) * 720);

            // Gradually fade out the fragment.
            FadeTransition fade = new FadeTransition(
                    Duration.millis(600), shard
            );

            fade.setDelay(Duration.millis(300));
            fade.setToValue(0);

            ParallelTransition animation = new ParallelTransition(
                    move, spin, fade
            );

            animation.setOnFinished(e -> fx.getChildren().remove(shard));
            animation.play();
        }
    }
}

