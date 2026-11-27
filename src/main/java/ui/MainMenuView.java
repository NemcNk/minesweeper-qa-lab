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

/** Main menu view for the Minesweeper application.*/
public final class MainMenuView extends VBox {

    /** Defines the fixed height of action buttons in the main menu. */
    private static final double ACTION_BUTTON_HEIGHT = 46;

    /** Stores the currently selected game difficulty. */
    private Difficulty selectedDifficulty = null;

    /** Creates the main menu with difficulty selection and action buttons.
     *
     * @param onPlay callback executed when a game starts
     * @param onViewStatistics callback executed when statistics are requested
     */
    public MainMenuView(
            Consumer<Difficulty> onPlay,
            Runnable onViewStatistics
    ) {
        // Configure the main menu layout and dimensions.
        setAlignment(Pos.CENTER);
        setSpacing(18);

        setMinWidth(360);
        setPrefWidth(380);
        setMaxWidth(380);

        getStyleClass().add("menu-root");

        // Load the application icon from the resources directory.
        var iconStream = getClass().getResourceAsStream(
                "/images/iconModern.png"
        );

        if (iconStream == null) {
            throw new IllegalStateException(
                    "iconModern.png not found"
            );
        }

        Image image = new Image(iconStream);

        // Create the animated bomb icon displayed above the menu title.
        BombIconAnimation iconContainer = new BombIconAnimation(image);
        iconContainer.getStyleClass().add("menu-icon");

        // Create and configure the application title.
        Label title = new Label("Minesweeper");

        title.getStyleClass().add("menu-title");
        title.setId(UiIds.MENU_TITLE);
        title.setMinWidth(USE_PREF_SIZE);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextOverrun(OverrunStyle.CLIP);

        // Create a container for the available difficulty options.
        VBox difficultyButtons = new VBox(12);

        // Generate one selection button for each available difficulty.
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

            // Align the difficulty name and its configuration details.
            name.setPrefWidth(100);
            name.setMinWidth(100);
            name.setAlignment(Pos.CENTER_LEFT);

            details.setMaxWidth(Double.MAX_VALUE);
            details.setAlignment(Pos.CENTER_RIGHT);

            HBox content = new HBox(8, name, details);

            content.setAlignment(Pos.CENTER_LEFT);
            content.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(details, Priority.ALWAYS);

            // Create and configure the difficulty selection button.
            Button button = new Button();
            button.setGraphic(content);
            button.getStyleClass().add("difficulty-button");
            button.setId(UiIds.difficultyButton(currentDifficulty));
            button.setMaxWidth(Double.MAX_VALUE);

            // Update the selected difficulty and button appearance.
            button.setOnAction(event -> {
                selectedDifficulty = currentDifficulty;

                difficultyButtons.getChildren().forEach(node ->
                        node.getStyleClass().remove("selected")
                );

                button.getStyleClass().add("selected");
            });

            difficultyButtons.getChildren().add(button);
        }

        // Create and configure the button used to start a new game.
        Button playButton = new Button("▷   Play Game");

        playButton.getStyleClass().add("play-button");
        playButton.setId(UiIds.PLAY_BUTTON);
        playButton.setMaxWidth(Double.MAX_VALUE);
        applyCompactSize(playButton);

        // Start the selected game after the icon explosion animation.
        playButton.setOnAction(event -> {
            if (selectedDifficulty == null) {
                return;
            }

            Difficulty chosen = selectedDifficulty;
            iconContainer.explodeThen(() -> onPlay.accept(chosen));
        });

        // Create and configure the button for opening game statistics.
        Button statsButton = new Button("View Statistics");

        statsButton.getStyleClass().add("stats-link-button");
        statsButton.setId(UiIds.STATISTICS_BUTTON);
        statsButton.setMaxWidth(Double.MAX_VALUE);

        // Build a small bar chart icon for the statistics button.
        HBox chartIcon = new HBox(
                3,
                bar(7),
                bar(13),
                bar(10)
        );

        chartIcon.setAlignment(Pos.BOTTOM_CENTER);

        statsButton.setGraphic(chartIcon);
        statsButton.setGraphicTextGap(10);

        // Open the statistics view when the button is clicked.
        statsButton.setOnAction(event -> onViewStatistics.run());

        // Add all menu components in their intended display order.
        getChildren().addAll(
                iconContainer,
                title,
                difficultyButtons,
                playButton,
                statsButton
        );
    }

    /** Applies a unified compact height and appearance to an action button.
     * @param button button to resize.*/
    private void applyCompactSize(Button button) {
        button.setMinHeight(ACTION_BUTTON_HEIGHT);
        button.setPrefHeight(ACTION_BUTTON_HEIGHT);
        button.setMaxHeight(ACTION_BUTTON_HEIGHT);

        button.getStyleClass().add("compact");
    }

    /** Creates a vertical bar for the statistics chart icon.
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




