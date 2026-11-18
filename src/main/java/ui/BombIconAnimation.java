package ui;

import javafx.animation.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

public final class BombIconAnimation extends StackPane {

    // Blue hues from the main icon
    private static final Color[] SHARD_COLORS = {
            Color.web("#2980b9"),
            Color.web("#66b8cc"),
            Color.web("#285882"),
            Color.web("#3d5a6e"),
            Color.web("#2c3e50")
    };

    private static final Color RING_COLOR = Color.web("#66b8cc");
    private static final Duration RADAR_PERIOD = Duration.seconds(3);

    private final ImageView bomb;
    private final Pane fx = new Pane();
    private final DropShadow glow = new DropShadow(0, Color.web("#66b8cc"));
    private final Random random = new Random();

    private final RotateTransition rotation;
    private final Timeline radar;

    private boolean busy = false;

    public BombIconAnimation(Image image) {
        this(image, true);
    }

    public BombIconAnimation(Image image, boolean idleAnimation) {

        bomb = new ImageView(image);
        bomb.setFitWidth(85);
        bomb.setFitHeight(85);
        bomb.setPreserveRatio(true);
        bomb.setEffect(glow);

        // Zero-size effect layer: (0,0) = icon center
        fx.setPrefSize(0, 0);
        fx.setMinSize(0, 0);
        fx.setMaxSize(0, 0);
        fx.setMouseTransparent(true);

        getChildren().addAll(bomb, fx);
        setViewOrder(-1);

        // Rotation: one turn per period
        rotation = new RotateTransition(RADAR_PERIOD, bomb);
        rotation.setByAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.setCycleCount(Animation.INDEFINITE);

        // Pulse + "radar" waves at the beginning of each round
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
    // API

    public void start() {
        stop();
        rotation.play();
        radar.play();
    }

    public void stop() {
        rotation.stop();
        radar.stop();
        resetBomb();
        fx.getChildren().clear();
        busy = false;
    }

    /** Click the Play icon to start playing;
     * action is triggered after animation. */
    public void explodeThen(Runnable after) {
        if (busy) {
            return;
        }
        busy = true;

        radar.stop(); // pulse and new waves stop, rotation still going on

        PauseTransition boom = new PauseTransition(Duration.millis(1));
        boom.setOnFinished(e -> {
            rotation.stop();
            explode();
        });

        PauseTransition wait = new PauseTransition(Duration.millis(700));

        SequentialTransition seq = new SequentialTransition(
                new ParallelTransition(chargeTimeline(350), shakeTimeline(350)),
                boom,
                wait
        );

        seq.setOnFinished(e -> {
            busy = false;
            after.run();
        });

        seq.play();
    }

    // radar (idle)

    private void spawnRing(double delayMs) {
        Circle ring = new Circle(0, 0, 44);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(RING_COLOR);
        ring.setStrokeWidth(4);
        ring.setOpacity(0);
        fx.getChildren().add(ring);

        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(ring.opacityProperty(), 0.8)),
                new KeyFrame(Duration.millis(1400),
                        new KeyValue(ring.radiusProperty(), 105, Interpolator.EASE_OUT),
                        new KeyValue(ring.strokeWidthProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(ring.opacityProperty(), 0, Interpolator.EASE_IN))
        );
        t.setDelay(Duration.millis(delayMs));
        t.setOnFinished(e -> fx.getChildren().remove(ring));
        t.play();
    }

    // preparing for the explosion

    private Timeline chargeTimeline(double millis) {
        return new Timeline(
                new KeyFrame(Duration.millis(millis),
                        new KeyValue(bomb.scaleXProperty(), 1.2, Interpolator.EASE_IN),
                        new KeyValue(bomb.scaleYProperty(), 1.2, Interpolator.EASE_IN),
                        new KeyValue(glow.radiusProperty(), 30, Interpolator.EASE_IN)
                )
        );
    }

    private Timeline shakeTimeline(double millis) {
        int ticks = (int) (millis / 40);
        Timeline shake = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            double amp = 1 + (bomb.getScaleX() - 1) * 30;
            bomb.setTranslateX((random.nextDouble() - 0.5) * 2 * amp);
            bomb.setTranslateY((random.nextDouble() - 0.5) * 2 * amp);
        }));
        shake.setCycleCount(ticks);
        shake.setOnFinished(e -> {
            bomb.setTranslateX(0);
            bomb.setTranslateY(0);
        });
        return shake;
    }

    private void resetBomb() {
        bomb.setScaleX(1);
        bomb.setScaleY(1);
        bomb.setRotate(0);
        bomb.setTranslateX(0);
        bomb.setTranslateY(0);
        bomb.setOpacity(1);
        glow.setRadius(0);
    }

    // explosion

    private void explode() {
        bomb.setOpacity(0);
        glow.setRadius(0);
        bomb.setTranslateX(0);
        bomb.setTranslateY(0);

        spawnFlash();
        spawnShockwave();
        spawnShards();
    }

    private void spawnFlash() {
        Circle flash = new Circle(0, 0, 45, Color.web("#66b8cc"));
        flash.setOpacity(0.85);
        fx.getChildren().add(flash);

        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(flash.scaleXProperty(), 0.3),
                        new KeyValue(flash.scaleYProperty(), 0.3)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(flash.scaleXProperty(), 1.8, Interpolator.EASE_OUT),
                        new KeyValue(flash.scaleYProperty(), 1.8, Interpolator.EASE_OUT),
                        new KeyValue(flash.opacityProperty(), 0, Interpolator.EASE_IN))
        );
        t.setOnFinished(e -> fx.getChildren().remove(flash));
        t.play();
    }

    private void spawnShockwave() {
        Circle ring = new Circle(0, 0, 20);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(Color.web("#2980b9"));
        ring.setStrokeWidth(8);
        fx.getChildren().add(ring);

        Timeline t = new Timeline(
                new KeyFrame(Duration.millis(550),
                        new KeyValue(ring.radiusProperty(), 110, Interpolator.EASE_OUT),
                        new KeyValue(ring.strokeWidthProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(ring.opacityProperty(), 0, Interpolator.EASE_IN))
        );
        t.setOnFinished(e -> fx.getChildren().remove(ring));
        t.play();
    }

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

            double angle = (2 * Math.PI / count) * i + (random.nextDouble() - 0.5) * 0.5;
            double dist = 70 + random.nextDouble() * 70;

            TranslateTransition move = new TranslateTransition(
                    Duration.millis(650 + random.nextDouble() * 250), shard);
            move.setToX(Math.cos(angle) * dist);
            move.setToY(Math.sin(angle) * dist);
            move.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition spin = new RotateTransition(Duration.millis(900), shard);
            spin.setByAngle((random.nextDouble() - 0.5) * 720);

            FadeTransition fade = new FadeTransition(Duration.millis(600), shard);
            fade.setDelay(Duration.millis(300));
            fade.setToValue(0);

            ParallelTransition p = new ParallelTransition(move, spin, fade);
            p.setOnFinished(e -> fx.getChildren().remove(shard));
            p.play();
        }
    }
}