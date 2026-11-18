package ui;

import model.Difficulty;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

public final class MainMenuView extends VBox {

    private static final double ACTION_BUTTON_HEIGHT = 46;

    private Difficulty selectedDifficulty = null;

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
        title.setMinWidth(Region.USE_PREF_SIZE);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextOverrun(OverrunStyle.CLIP);

        VBox difficultyButtons = new VBox(12);

        for (Difficulty difficulty : Difficulty.values()) {

            Button button = new Button();

            Label name = new Label(difficulty.getTitle());
            Label details = new Label(
                    difficulty.getSize()
                            + "×"
                            + difficulty.getSize()
                            + " · "
                            + difficulty.getTimeMinutes()
                            + " min"
            );

            name.setPrefWidth(100);
            name.setMinWidth(100);
            name.setAlignment(Pos.CENTER_LEFT);

            details.setMaxWidth(Double.MAX_VALUE);
            details.setAlignment(Pos.CENTER_RIGHT);

            HBox content = new HBox(
                    8,
                    name,
                    details
            );

            content.setAlignment(Pos.CENTER_LEFT);
            content.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(details, Priority.ALWAYS);

            button.setGraphic(content);
            button.getStyleClass().add("difficulty-button");
            button.setMaxWidth(Double.MAX_VALUE);

            button.setOnAction(event -> {

                selectedDifficulty = difficulty;

                difficultyButtons.getChildren()
                        .forEach(node ->
                                node.getStyleClass()
                                        .remove("selected")
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

    // an unified compact size for action buttons.
    private void applyCompactSize(Button button) {

        button.setMinHeight(ACTION_BUTTON_HEIGHT);
        button.setPrefHeight(ACTION_BUTTON_HEIGHT);
        button.setMaxHeight(ACTION_BUTTON_HEIGHT);
        button.setStyle("-fx-font-size: 16px;");
    }

    // A vertical column for the chart icon.
    private Rectangle bar(double height) {

        Rectangle rectangle = new Rectangle(4, height);
        rectangle.getStyleClass().add("stats-bar");   // Use the color defined in CSS.
        rectangle.setArcWidth(2);
        rectangle.setArcHeight(2);

        return rectangle;
    }
}



