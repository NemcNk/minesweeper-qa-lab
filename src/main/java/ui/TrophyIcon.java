package ui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

/**
 * A flat trophy that pops up and spins like a coin.
 */
final class TrophyIcon extends Group {

    private static final Color OUTLINE = Color.web("#8a5f12");

    private final Group art;
    private final Timeline popAnimation;
    private final Timeline spinAnimation;

    TrophyIcon() {
        art = createArt();
        getChildren().add(art);

        popAnimation = createPopAnimation();
        spinAnimation = createSpinAnimation();
    }

    void start() {
        setScaleX(0);
        setScaleY(0);

        popAnimation.playFromStart();
        spinAnimation.playFromStart();
    }

    void stop() {
        popAnimation.stop();
        spinAnimation.stop();
    }

    // Figure

    private Group createArt() {
        LinearGradient gold = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#f6d365")),
                new Stop(1, Color.web("#d4a63a"))
        );

        // The handles are drawn first so that the bowl overlaps their edges.
        final SVGPath leftHandle = handle("M-29 -30 C-54 -30 -50 0 -22 6");
        final SVGPath rightHandle = handle("M29 -30 C54 -30 50 0 22 6");

        SVGPath cup = new SVGPath();
        cup.setContent("M-30 -38 H30 V-8 A30 30 0 0 1 -30 -8 Z");
        cup.setFill(gold);
        cup.setStroke(OUTLINE);
        cup.setStrokeWidth(3);
        cup.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Rectangle stem = new Rectangle(-7, 20, 14, 16);
        stem.setFill(gold);
        stem.setStroke(OUTLINE);
        stem.setStrokeWidth(3);

        Rectangle base = new Rectangle(-25, 34, 50, 12);
        base.setArcWidth(8);
        base.setArcHeight(8);
        base.setFill(gold);
        base.setStroke(OUTLINE);
        base.setStrokeWidth(3);

        SVGPath shine = new SVGPath();
        shine.setContent("M-21 -29 V-14 Q-21 -4 -13 3");
        shine.setFill(Color.TRANSPARENT);
        shine.setStroke(Color.web("#fff3c4"));
        shine.setStrokeWidth(4);
        shine.setStrokeLineCap(StrokeLineCap.ROUND);
        shine.setOpacity(0.8);

        Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? 10 : 4.5;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            star.getPoints().addAll(
                    r * Math.cos(a),
                    -14 + r * Math.sin(a)
            );
        }

        star.setFill(Color.web("#fff3c4"));
        star.setStroke(OUTLINE);
        star.setStrokeWidth(1.5);
        star.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Group result = new Group(
                leftHandle, rightHandle, cup, stem, base, shine, star
        );

        DropShadow glow = new DropShadow(18, Color.web("#d4a63a"));
        glow.setSpread(0.1);
        result.setEffect(glow);

        return result;
    }

    private static SVGPath handle(String path) {
        SVGPath handle = new SVGPath();
        handle.setContent(path);
        handle.setFill(Color.TRANSPARENT);
        handle.setStroke(Color.web("#b8862b"));
        handle.setStrokeWidth(8);
        handle.setStrokeLineCap(StrokeLineCap.ROUND);

        return handle;
    }

    // Animations

    private Timeline createPopAnimation() {
        return new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scaleXProperty(), 0),
                        new KeyValue(scaleYProperty(), 0)
                ),
                new KeyFrame(Duration.millis(350),
                        new KeyValue(
                                scaleXProperty(), 1.15, Interpolator.EASE_OUT
                        ),
                        new KeyValue(
                                scaleYProperty(), 1.15, Interpolator.EASE_OUT
                        )
                ),
                new KeyFrame(Duration.millis(550),
                        new KeyValue(
                                scaleXProperty(), 1, Interpolator.EASE_BOTH
                        ),
                        new KeyValue(
                                scaleYProperty(), 1, Interpolator.EASE_BOTH
                        )
                )
        );
    }

    /**
     * Coin-like rotation: scaleX follows cosine of the angle.
     * A thin strip remains visible when viewed edge-on.
     */
    private Timeline createSpinAnimation() {
        SimpleDoubleProperty angle = new SimpleDoubleProperty(0);

        angle.addListener((obs, oldValue, newValue) -> {
            double currentAngle = newValue.doubleValue();
            double c = Math.cos(currentAngle);
            double sign = c < 0 ? -1 : 1;

            art.setScaleX(sign * Math.max(Math.abs(c), 0.06));
            art.setTranslateY(Math.sin(currentAngle * 2) * 3);
        });

        Timeline spin = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(angle, 0)
                ),
                new KeyFrame(Duration.seconds(2.4),
                        new KeyValue(
                                angle, 2 * Math.PI, Interpolator.LINEAR
                        )
                )
        );

        spin.setCycleCount(Animation.INDEFINITE);
        spin.setDelay(Duration.millis(550));

        return spin;
    }
}

