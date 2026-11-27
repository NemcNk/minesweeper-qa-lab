package ui;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/** A semi-transparent overlay displayed over the game board while the game
 * is paused. Captures all mouse events, preventing the cells underneath
 * from responding.*/
final class PauseOverlay extends StackPane {

    /** Creates the pause overlay.*/
    PauseOverlay() {
        getStyleClass().add("pause-overlay");

        Label label = new Label("PAUSED");
        label.getStyleClass().add("pause-label");

        getChildren().add(label);

        setVisible(false);

        addEventFilter(MouseEvent.ANY, MouseEvent::consume);
    }
}

