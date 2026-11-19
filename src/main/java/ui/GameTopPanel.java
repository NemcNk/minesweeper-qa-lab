package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Represents the top panel of the Minesweeper game.
 * Displays the mine counter, timer and restart button.
 */
public final class GameTopPanel extends StackPane {

    private final SevenSegmentDisplay mineDisplay =
            new SevenSegmentDisplay();

    private final SevenSegmentDisplay timeDisplay =
            new SevenSegmentDisplay(5, true);

    private final FaceButton faceButton;

    /**
     * Creates the game top panel.
     *
     * @param restartAction action performed when the face button is clicked
     */
    public GameTopPanel(final Runnable restartAction) {
        faceButton = new FaceButton(restartAction);

        getStyleClass().add("game-top-panel");

        // Mine counter
        final Label mineLabel = new Label("MINES");

        final VBox mineBox = new VBox(
                4,
                mineLabel,
                mineDisplay
        );

        mineBox.setAlignment(Pos.CENTER_LEFT);

        // Timer
        final Label timeLabel = new Label("TIME");

        final VBox timeBox = new VBox(
                4,
                timeLabel,
                timeDisplay
        );

        timeBox.setAlignment(Pos.CENTER_RIGHT);

        // Two equally wide halves of the panel
        final GridPane grid = new GridPane();

        final ColumnConstraints leftColumn =
                new ColumnConstraints();

        leftColumn.setPercentWidth(50);
        leftColumn.setHgrow(Priority.ALWAYS);

        final ColumnConstraints rightColumn =
                new ColumnConstraints();

        rightColumn.setPercentWidth(50);
        rightColumn.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(
                leftColumn,
                rightColumn
        );

        grid.setMaxSize(
                Double.MAX_VALUE,
                Double.MAX_VALUE
        );

        grid.add(mineBox, 0, 0);
        grid.add(timeBox, 1, 0);

        // Center the face button over the grid
        final StackPane faceContainer = new StackPane(faceButton);

        faceContainer.setAlignment(Pos.CENTER);

        getChildren().addAll(
                grid,
                faceContainer
        );

        setAlignment(Pos.CENTER);
    }

    /**
     * Updates the displayed number of remaining mines.
     *
     * @param mines number of remaining mines
     */
    public void updateMines(final int mines) {
        mineDisplay.setValue(mines);
    }

    /**
     * Updates the game timer.
     *
     * @param seconds elapsed game time in seconds
     */
    public void updateTime(final int seconds) {
        timeDisplay.setTime(seconds);
    }

    /**
     * Changes the face button appearance.
     *
     * @param face new face state
     */
    public void setFace(final FaceButton.Face face) {
        faceButton.setFace(face);
    }
}




