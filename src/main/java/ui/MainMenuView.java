package ui;

import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import model.Difficulty;

/**
 * Main menu view for the Minesweeper application.
 */
public final class MainMenuView extends VBox {

    private static final double ACTION_BUTTON_HEIGHT = 46;

    private Difficulty selectedDifficulty = null;

    /**
     * Creates the main menu with difficulty selection and action buttons.
     *
     * @param onPlay callback executed when a game starts
     * @param onViewStatistics callback executed when statistics are requested
     */
    public MainMenuView(
            Consumer<Difficulty> onPlay,
            Runnable onViewStatistics
    ) {
        setAlignment(Pos.CENTER);
        setSpacing(18);

        setMinWidth(360);
        setPrefWidth(380);
        setMaxWidth(380);

        getStyleClass().add("menu-root");

        var iconStream = getClass().getResourceAsStream(
                "/images/iconModern.png"
        );

        if (iconStream == null) {
            throw new IllegalStateException(
                    "iconModern.png not found"
            );
        }

        Image image = new Image(iconStream);

        BombIconAnimation iconContainer = new BombIconAnimation(image);
        iconContainer.getStyleClass().add("menu-icon");

        Label title = new Label("Minesweeper");

        title.getStyleClass().add("menu-title");
        title.setMinWidth(USE_PREF_SIZE);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextOverrun(OverrunStyle.CLIP);

        VBox difficultyButtons = new VBox(12);

        for (Difficulty difficulty : Difficulty.values()) {
            final Difficulty currentDifficulty = difficulty;

            Label name = new Label(currentDifficulty.getTitle());
            Label details = new Label(
                    currentDifficulty.getSize()
                            + "×"
                            + currentDifficulty.getSize()
                            + " · "
                            + currentDifficulty.getTimeMinutes()
                            + " min"
            );

            name.setPrefWidth(100);
            name.setMinWidth(100);
            name.setAlignment(Pos.CENTER_LEFT);

            details.setMaxWidth(Double.MAX_VALUE);
            details.setAlignment(Pos.CENTER_RIGHT);

            HBox content = new HBox(8, name, details);

            content.setAlignment(Pos.CENTER_LEFT);
            content.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(details, Priority.ALWAYS);

            Button button = new Button();
            button.setGraphic(content);
            button.getStyleClass().add("difficulty-button");
            button.setMaxWidth(Double.MAX_VALUE);

            button.setOnAction(event -> {
                selectedDifficulty = currentDifficulty;

                difficultyButtons.getChildren().forEach(node ->
                        node.getStyleClass().remove("selected")
                );

                button.getStyleClass().add("selected");
            });

            difficultyButtons.getChildren().add(button);
        }

        // Play Game button
        Button playButton = new Button("▷   Play Game");

        playButton.getStyleClass().add("play-button");
        playButton.setMaxWidth(Double.MAX_VALUE);
        applyCompactSize(playButton);

        playButton.setOnAction(event -> {
            if (selectedDifficulty == null) {
                return;
            }

            Difficulty chosen = selectedDifficulty;
            iconContainer.explodeThen(() -> onPlay.accept(chosen));
        });

        // View Statistics button
        Button statsButton = new Button("View Statistics");

        statsButton.getStyleClass().add("stats-link-button");
        statsButton.setMaxWidth(Double.MAX_VALUE);

        HBox chartIcon = new HBox(
                3,
                bar(7),
                bar(13),
                bar(10)
        );

        chartIcon.setAlignment(Pos.BOTTOM_CENTER);

        statsButton.setGraphic(chartIcon);
        statsButton.setGraphicTextGap(10);

        statsButton.setOnAction(event -> onViewStatistics.run());

        getChildren().addAll(
                iconContainer,
                title,
                difficultyButtons,
                playButton,
                statsButton
        );
    }

    /**
     * Applies a unified compact height to an action button.
     *
     * @param button button to resize
     */
    private void applyCompactSize(Button button) {
        button.setMinHeight(ACTION_BUTTON_HEIGHT);
        button.setPrefHeight(ACTION_BUTTON_HEIGHT);
        button.setMaxHeight(ACTION_BUTTON_HEIGHT);
        button.setStyle("-fx-font-size: 16px;");
    }

    /**
     * Creates a vertical bar for the statistics chart icon.
     *
     * @param height height of the bar
     * @return configured rectangle representing a chart bar
     */
    private Rectangle bar(double height) {
        Rectangle rectangle = new Rectangle(4, height);

        rectangle.getStyleClass().add("stats-bar");
        rectangle.setArcWidth(2);
        rectangle.setArcHeight(2);

        return rectangle;
    }
}



