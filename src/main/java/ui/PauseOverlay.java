package ui;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * A semi-transparent overlay displayed over the game board while the game is paused.
 * Captures all mouse events, preventing the cells underneath from responding.
 */
final class PauseOverlay extends StackPane {

    PauseOverlay() {

        setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        Label label = new Label("PAUSED");
        label.setStyle(
                "-fx-text-fill: white;"
                        + "-fx-font-size: 32px;"
                        + "-fx-font-weight: bold;"
        );

        getChildren().add(label);

        setVisible(false);

        addEventFilter(MouseEvent.ANY, MouseEvent::consume);
    }
}
