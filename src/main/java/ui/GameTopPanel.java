package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;

public final class GameTopPanel extends StackPane {

    private final SevenSegmentDisplay mineDisplay =
            new SevenSegmentDisplay();

    private final SevenSegmentDisplay timeDisplay =
            new SevenSegmentDisplay(5, true);

    private final FaceButton faceButton;

    public GameTopPanel(Runnable restartAction) {

        faceButton = new FaceButton(restartAction);

        getStyleClass().add("game-top-panel");

        // Mine counter
        Label mineLabel = new Label("MINES");

        VBox mineBox = new VBox(
                4,
                mineLabel,
                mineDisplay
        );

        mineBox.setAlignment(Pos.CENTER_LEFT);

        // Timer
        Label timeLabel = new Label("TIME");

        VBox timeBox = new VBox(
                4,
                timeLabel,
                timeDisplay
        );

        timeBox.setAlignment(Pos.CENTER_RIGHT);

        // Two equally wide halves of the panel
        GridPane grid = new GridPane();

        ColumnConstraints leftColumn =
                new ColumnConstraints();

        leftColumn.setPercentWidth(50);
        leftColumn.setHgrow(Priority.ALWAYS);

        ColumnConstraints rightColumn =
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

        // The emoji is displayed over the grid, perfectly centered.
        StackPane faceContainer = new StackPane(faceButton);

        faceContainer.setAlignment(Pos.CENTER);

        getChildren().addAll(
                grid,
                faceContainer
        );

        setAlignment(Pos.CENTER);
    }

    public void updateMines(int mines) {
        mineDisplay.setValue(mines);
    }

    public void updateTime(int seconds) {
        timeDisplay.setTime(seconds);
    }

    public void setFace(FaceButton.Face face) {
        faceButton.setFace(face);
    }

}



