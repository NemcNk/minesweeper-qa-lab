package ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Victory / Defeat Screen: A result card displayed over the game.
 */
public final class ResultOverlay extends StackPane {

    private final FireworksLayer fireworks = new FireworksLayer();
    private final StackPane iconBox = new StackPane();
    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final Label timeValueLabel = new Label();
    private final Button primaryButton = new Button();
    private final Image bombImage;

    private TrophyIcon trophy;
    private PauseTransition bombDelay;

    /**
     * Creates the result overlay.
     *
     * @param onPlayAgain action executed when the player chooses to play again
     * @param onMainMenu action executed when the player returns to the main menu
     */
    public ResultOverlay(Runnable onPlayAgain, Runnable onMainMenu) {

        var stream = getClass().getResourceAsStream("/images/iconModern.png");

        if (stream == null) {
            throw new IllegalStateException("iconModern.png not found");
        }

        bombImage = new Image(stream);

        getStyleClass().add("result-overlay");
        setAlignment(Pos.CENTER);
        setVisible(false);

        titleLabel.getStyleClass().add("result-title");
        subtitleLabel.getStyleClass().add("result-subtitle");

        Region divider = new Region();
        divider.getStyleClass().add("result-divider");
        divider.setMaxHeight(1);
        divider.setMinHeight(1);

        Label timeCaption = new Label("Your time");
        timeCaption.getStyleClass().add("result-time-caption");
        timeValueLabel.getStyleClass().add("result-time-value");

        primaryButton.getStyleClass().add("result-primary-button");
        primaryButton.setMaxWidth(Double.MAX_VALUE);
        primaryButton.setOnAction(e -> onPlayAgain.run());

        Button menuButton = new Button("Main Menu");
        menuButton.getStyleClass().add("result-link-button");
        menuButton.setOnAction(e -> onMainMenu.run());

        // Fixed size for the icon.
        iconBox.setMinSize(150, 150);
        iconBox.setPrefSize(150, 150);
        iconBox.setMaxSize(150, 150);
        iconBox.setViewOrder(-1);

        VBox card = new VBox(
                6, iconBox, titleLabel, subtitleLabel, divider,
                timeCaption, timeValueLabel, primaryButton, menuButton
        );
        card.getStyleClass().add("result-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(300);
        card.setMaxHeight(USE_PREF_SIZE);

        getChildren().addAll(fireworks, card);
    }

    /**
     * Displays the result screen.
     *
     * @param won whether the player won the game
     * @param subtitle additional result message
     * @param seconds elapsed game time in seconds
     */
    public void show(boolean won, String subtitle, int seconds) {

        stopEffects();

        titleLabel.setText(won ? "YOU WON!" : "GAME OVER");
        subtitleLabel.setText(subtitle);
        timeValueLabel.setText(
                String.format("%02d:%02d", seconds / 60, seconds % 60)
        );
        primaryButton.setText(won ? "Play Again" : "Try Again");

        // An opaque background covers the game board and underlying controls.
        setStyle(won
                ? "-fx-background-color: #1c2333;"
                : "-fx-background-color: #faf9f6;");

        if (won) {
            showTrophy();
        } else {
            showExplodingBomb();
        }

        setOpacity(0);
        setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.millis(250), this);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Hides the result screen and stops all animations.
     */
    public void hide() {

        stopEffects();
        setVisible(false);
    }

    /**
     * Displays the trophy animation and fireworks.
     */
    private void showTrophy() {

        trophy = new TrophyIcon();
        iconBox.getChildren().setAll(trophy);

        trophy.start();
        fireworks.start();
    }

    /**
     * Displays the bomb animation followed by an explosion.
     */
    private void showExplodingBomb() {

        BombIconAnimation bomb = new BombIconAnimation(bombImage, false);
        iconBox.getChildren().setAll(bomb);

        bombDelay = new PauseTransition(Duration.millis(450));
        bombDelay.setOnFinished(e -> bomb.explodeThen(() -> { }));
        bombDelay.play();
    }

    /**
     * Stops all active animations and clears the icon container.
     */
    private void stopEffects() {

        fireworks.stop();

        if (trophy != null) {
            trophy.stop();
            trophy = null;
        }

        if (bombDelay != null) {
            bombDelay.stop();
            bombDelay = null;
        }

        iconBox.getChildren().clear();
    }
}
