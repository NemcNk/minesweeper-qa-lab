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

/** A flat trophy icon that appears with a pop effect and spins like a coin.
 */
final class TrophyIcon extends Group {

    /** Defines the outline color of the trophy. */
    private static final Color OUTLINE = Color.web("#8a5f12");

    /** Contains the graphical elements that form the trophy. */
    private final Group art;

    /** Controls the scale animation used to display the trophy. */
    private final Timeline popAnimation;

    /** Controls the continuous coin-like rotation animation. */
    private final Timeline spinAnimation;

    /** Creates the trophy icon and initializes its graphical elements
     * and animations.*/
    TrophyIcon() {
        // Create the trophy artwork and add it to the scene graph.
        art = createArt();
        getChildren().add(art);

        // Initialize the appearance and rotation animations.
        popAnimation = createPopAnimation();
        spinAnimation = createSpinAnimation();
    }

    /** Starts the trophy appearance and rotation animations.
     */
    void start() {
        // Reset the trophy scale before starting the animations.
        setScaleX(0);
        setScaleY(0);

        // Start both animations from their initial positions.
        popAnimation.playFromStart();
        spinAnimation.playFromStart();
    }

    /** Stops all trophy animations.*/
    void stop() {
        popAnimation.stop();
        spinAnimation.stop();
    }

    /** Creates the graphical representation of the trophy.
     * @return a group containing all trophy elements.*/
    private Group createArt() {

        // Create a gold gradient used to fill the trophy components.
        LinearGradient gold = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#f6d365")),
                new Stop(1, Color.web("#d4a63a"))
        );

        // Draw the handles first so that the bowl overlaps their edges.
        final SVGPath leftHandle = handle("M-29 -30 C-54 -30 -50 0 -22 6");
        final SVGPath rightHandle = handle("M29 -30 C54 -30 50 0 22 6");

        // Create the main bowl of the trophy.
        SVGPath cup = new SVGPath();
        cup.setContent("M-30 -38 H30 V-8 A30 30 0 0 1 -30 -8 Z");
        cup.setFill(gold);
        cup.setStroke(OUTLINE);
        cup.setStrokeWidth(3);
        cup.setStrokeLineJoin(StrokeLineJoin.ROUND);

        // Create the vertical stem connecting the bowl to the base.
        Rectangle stem = new Rectangle(-7, 20, 14, 16);
        stem.setFill(gold);
        stem.setStroke(OUTLINE);
        stem.setStrokeWidth(3);

        // Create the rounded base of the trophy.
        Rectangle base = new Rectangle(-25, 34, 50, 12);
        base.setArcWidth(8);
        base.setArcHeight(8);
        base.setFill(gold);
        base.setStroke(OUTLINE);
        base.setStrokeWidth(3);

        // Add a light reflection to the left side of the trophy bowl.
        SVGPath shine = new SVGPath();
        shine.setContent("M-21 -29 V-14 Q-21 -4 -13 3");
        shine.setFill(Color.TRANSPARENT);
        shine.setStroke(Color.web("#fff3c4"));
        shine.setStrokeWidth(4);
        shine.setStrokeLineCap(StrokeLineCap.ROUND);
        shine.setOpacity(0.8);

        // Generate a decorative five-pointed star.
        Polygon star = new Polygon();

        for (int i = 0; i < 10; i++) {
            // Alternate between outer and inner radii to create star points.
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

        // Combine all graphical elements into a single group.
        Group result = new Group(
                leftHandle, rightHandle, cup, stem, base, shine, star
        );

        // Apply a soft golden glow around the trophy.
        DropShadow glow = new DropShadow(18, Color.web("#d4a63a"));
        glow.setSpread(0.1);
        result.setEffect(glow);

        return result;
    }

    /** Creates one of the trophy handles using an SVG path.
     * @param path SVG path defining the handle shape
     * @return configured SVG path representing the handle.*/
    private static SVGPath handle(String path) {
        SVGPath handle = new SVGPath();
        handle.setContent(path);
        handle.setFill(Color.TRANSPARENT);
        handle.setStroke(Color.web("#b8862b"));
        handle.setStrokeWidth(8);
        handle.setStrokeLineCap(StrokeLineCap.ROUND);

        return handle;
    }

    /** Creates the animation that makes the trophy pop into view.
     * @return configured timeline for the trophy appearance animation
     */
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

    /** Creates a continuous coin-like rotation animation.
     * The horizontal scale follows the cosine of the rotation angle,
     * creating the appearance of a three-dimensional spin.
     * A thin strip remains visible when the trophy is viewed edge-on.
     *
     * @return configured timeline for the continuous rotation animation
     */
    private Timeline createSpinAnimation() {
        // Store the current rotation angle as an observable property.
        SimpleDoubleProperty angle = new SimpleDoubleProperty(0);

        // Update the trophy appearance whenever the angle changes.
        angle.addListener((obs, oldValue, newValue) -> {
            double currentAngle = newValue.doubleValue();
            double c = Math.cos(currentAngle);
            double sign = c < 0 ? -1 : 1;

            // Simulate rotation by compressing the trophy horizontally.
            art.setScaleX(sign * Math.max(Math.abs(c), 0.06));

            // Add a subtle vertical movement during the rotation.
            art.setTranslateY(Math.sin(currentAngle * 2) * 3);
        });

        // Define the rotation from zero to one complete revolution.
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

        // Repeat the rotation indefinitely after the initial delay.
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setDelay(Duration.millis(550));

        return spin;
    }
}


