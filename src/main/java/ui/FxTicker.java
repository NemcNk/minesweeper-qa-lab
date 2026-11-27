package ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import session.Ticker;

/** An implementation of {@link Ticker} that uses a JavaFX {@link Timeline}
 * to execute periodic tick actions.
 */
public final class FxTicker implements Ticker {

    /** The JavaFX timeline responsible for generating periodic ticks. */
    private Timeline timeline;

    /** Starts the timer and executes the specified action every second.
     * Stops any previously running timer before creating a new one.
     *
     * @param onTick the action to execute on each timer tick
     */
    @Override
    public void start(Runnable onTick) {

        stop();

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> onTick.run())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /** Pauses the timer if it has been initialized.*/
    @Override
    public void pause() {

        if (timeline != null) {
            timeline.pause();
        }
    }

    /** Resumes the timer if it has been initialized.*/
    @Override
    public void resume() {

        if (timeline != null) {
            timeline.play();
        }
    }

    /** Stops the timer if it has been initialized.*/
    @Override
    public void stop() {

        if (timeline != null) {
            timeline.stop();
        }
    }
}

