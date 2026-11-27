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
 * Displays the victory or defeat screen over the game.
 */
public final class ResultOverlay extends StackPane {

    private static final String WON_CLASS = "won";
    private static final String LOST_CLASS = "lost";

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

        var stream = getClass().getResourceAsStream(
                "/images/iconModern.png"
        );

        if (stream == null) {
            throw new IllegalStateException("iconModern.png not found");
        }

        bombImage = new Image(stream);

        getStyleClass().add("result-overlay");
        setId(UiIds.RESULT_OVERLAY);
        setAlignment(Pos.CENTER);
        setVisible(false);

        titleLabel.getStyleClass().add("result-title");
        titleLabel.setId(UiIds.RESULT_TITLE);

        subtitleLabel.getStyleClass().add("result-subtitle");
        subtitleLabel.setId(UiIds.RESULT_SUBTITLE);

        Region divider = new Region();
        divider.getStyleClass().add("result-divider");
        divider.setMaxHeight(1);
        divider.setMinHeight(1);

        Label timeCaption = new Label("Your time");
        timeCaption.getStyleClass().add("result-time-caption");

        timeValueLabel.getStyleClass().add("result-time-value");
        timeValueLabel.setId(UiIds.RESULT_TIME);

        primaryButton.getStyleClass().add("result-primary-button");
        primaryButton.setId(UiIds.RESULT_PRIMARY_BUTTON);
        primaryButton.setMaxWidth(Double.MAX_VALUE);
        primaryButton.setOnAction(event -> onPlayAgain.run());

        Button menuButton = new Button("Main Menu");
        menuButton.getStyleClass().add("result-link-button");
        menuButton.setId(UiIds.RESULT_MENU_BUTTON);
        menuButton.setOnAction(event -> onMainMenu.run());

        // Configure the fixed icon container.
        iconBox.setMinSize(150, 150);
        iconBox.setPrefSize(150, 150);
        iconBox.setMaxSize(150, 150);
        iconBox.setViewOrder(-1);

        VBox card = new VBox(
                6,
                iconBox,
                titleLabel,
                subtitleLabel,
                divider,
                timeCaption,
                timeValueLabel,
                primaryButton,
                menuButton
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

        updateResultStyle(won);

        if (won) {
            showTrophy();
        } else {
            showExplodingBomb();
        }

        setOpacity(0);
        setVisible(true);

        FadeTransition fade = new FadeTransition(
                Duration.millis(250),
                this
        );

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
     * Updates the overlay appearance according to the game result.
     *
     * @param won whether the player won the game
     */
    private void updateResultStyle(boolean won) {
        getStyleClass().removeAll(WON_CLASS, LOST_CLASS);

        getStyleClass().add(won ? WON_CLASS : LOST_CLASS);
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
        BombIconAnimation bomb = new BombIconAnimation(
                bombImage,
                false
        );

        iconBox.getChildren().setAll(bomb);

        bombDelay = new PauseTransition(Duration.millis(450));
        bombDelay.setOnFinished(event ->
                bomb.explodeThen(() -> { })
        );

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

