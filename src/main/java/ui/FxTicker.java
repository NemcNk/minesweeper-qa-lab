package ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * {@link Ticker} backed by a JavaFX {@link Timeline}.
 */
final class FxTicker implements Ticker {

    private Timeline timeline;

    @Override
    public void start(Runnable onTick) {

        stop();

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> onTick.run())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @Override
    public void pause() {

        if (timeline != null) {
            timeline.pause();
        }
    }

    @Override
    public void resume() {

        if (timeline != null) {
            timeline.play();
        }
    }

    @Override
    public void stop() {

        if (timeline != null) {
            timeline.stop();
        }
    }
}
