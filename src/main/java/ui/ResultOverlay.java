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

/** Displays the victory or defeat screen over the game.*/
public final class ResultOverlay extends StackPane {

    /** Defines the CSS style class for a victory result. */
    private static final String WON_CLASS = "won";

    /** Defines the CSS style class for a defeat result. */
    private static final String LOST_CLASS = "lost";

    /** Displays fireworks when the player wins the game. */
    private final FireworksLayer fireworks = new FireworksLayer();

    /** Contains the trophy or bomb animation. */
    private final StackPane iconBox = new StackPane();

    /** Displays the main result message. */
    private final Label titleLabel = new Label();

    /** Displays additional information about the game result. */
    private final Label subtitleLabel = new Label();

    /** Displays the elapsed game time. */
    private final Label timeValueLabel = new Label();

    /** Provides the primary action for playing another game. */
    private final Button primaryButton = new Button();

    /** Stores the image used for the bomb animation. */
    private final Image bombImage;

    /** Stores the trophy animation instance when the player wins. */
    private TrophyIcon trophy;

    /** Controls the delay before the bomb explosion animation starts. */
    private PauseTransition bombDelay;

    /** Creates the result overlay with game result information and action buttons.
     * @param onPlayAgain action executed when the player chooses to play again
     * @param onMainMenu action executed when the player returns to the main menu
     */
    public ResultOverlay(Runnable onPlayAgain, Runnable onMainMenu) {

        // Load the image used by the bomb animation.
        var stream = getClass().getResourceAsStream(
                "/images/iconModern.png"
        );

        if (stream == null) {
            throw new IllegalStateException("iconModern.png not found");
        }

        bombImage = new Image(stream);

        // Configure the overlay container and its initial visibility.
        getStyleClass().add("result-overlay");
        setId(UiIds.RESULT_OVERLAY);
        setAlignment(Pos.CENTER);
        setVisible(false);

        // Configure the main result title.
        titleLabel.getStyleClass().add("result-title");
        titleLabel.setId(UiIds.RESULT_TITLE);

        // Configure the additional result message.
        subtitleLabel.getStyleClass().add("result-subtitle");
        subtitleLabel.setId(UiIds.RESULT_SUBTITLE);

        // Create a horizontal divider separating the result and time sections.
        Region divider = new Region();
        divider.getStyleClass().add("result-divider");
        divider.setMaxHeight(1);
        divider.setMinHeight(1);

        // Create the caption displayed above the elapsed time.
        Label timeCaption = new Label("Your time");
        timeCaption.getStyleClass().add("result-time-caption");

        // Configure the label displaying the formatted game time.
        timeValueLabel.getStyleClass().add("result-time-value");
        timeValueLabel.setId(UiIds.RESULT_TIME);

        // Configure the primary button used to restart the game.
        primaryButton.getStyleClass().add("result-primary-button");
        primaryButton.setId(UiIds.RESULT_PRIMARY_BUTTON);
        primaryButton.setMaxWidth(Double.MAX_VALUE);
        primaryButton.setOnAction(event -> onPlayAgain.run());

        // Create the button used to return to the main menu.
        Button menuButton = new Button("Main Menu");
        menuButton.getStyleClass().add("result-link-button");
        menuButton.setId(UiIds.RESULT_MENU_BUTTON);
        menuButton.setOnAction(event -> onMainMenu.run());

        // Configure the fixed-size container for result animations.
        iconBox.setMinSize(150, 150);
        iconBox.setPrefSize(150, 150);
        iconBox.setMaxSize(150, 150);
        iconBox.setViewOrder(-1);

        // Arrange all result elements inside the central card.
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

        // Add the fireworks layer behind the result card.
        getChildren().addAll(fireworks, card);
    }

    /** Displays the result screen with the appropriate message and animation.
     * @param won whether the player won the game
     * @param subtitle additional result message
     * @param seconds elapsed game time in seconds.*/
    public void show(boolean won, String subtitle, int seconds) {

        // Stop previous animations before displaying a new result.
        stopEffects();

        // Update the result messages and formatted elapsed time.
        titleLabel.setText(won ? "YOU WON!" : "GAME OVER");
        subtitleLabel.setText(subtitle);

        timeValueLabel.setText(
                String.format("%02d:%02d", seconds / 60, seconds % 60)
        );

        // Set the primary button text according to the game result.
        primaryButton.setText(won ? "Play Again" : "Try Again");

        // Apply the appropriate visual style and animation.
        updateResultStyle(won);

        if (won) {
            showTrophy();
        } else {
            showExplodingBomb();
        }

        // Make the overlay visible and prepare its fade-in animation.
        setOpacity(0);
        setVisible(true);

        FadeTransition fade = new FadeTransition(
                Duration.millis(250),
                this
        );

        fade.setToValue(1);
        fade.play();
    }

    /** Hides the result screen and stops all active animations.
     */
    public void hide() {
        stopEffects();
        setVisible(false);
    }

    /** Updates the overlay appearance according to the game result.
     * @param won whether the player won the game.*/
    private void updateResultStyle(boolean won) {

        // Remove previous result styles before applying the new one.
        getStyleClass().removeAll(WON_CLASS, LOST_CLASS);

        getStyleClass().add(won ? WON_CLASS : LOST_CLASS);
    }

    /** Displays the trophy animation and starts the fireworks effect.
     */
    private void showTrophy() {

        // Create and display a new trophy animation.
        trophy = new TrophyIcon();
        iconBox.getChildren().setAll(trophy);

        // Start the trophy and fireworks animations simultaneously.
        trophy.start();
        fireworks.start();
    }

    /** Displays the bomb animation followed by an explosion.
     */
    private void showExplodingBomb() {

        // Create and display the bomb animation without automatic playback.
        BombIconAnimation bomb = new BombIconAnimation(
                bombImage,
                false
        );

        iconBox.getChildren().setAll(bomb);

        // Delay the explosion to allow the bomb animation to be displayed.
        bombDelay = new PauseTransition(Duration.millis(450));
        bombDelay.setOnFinished(event ->
                bomb.explodeThen(() -> { })
        );

        bombDelay.play();
    }

    /** Stops all active animations and clears the icon container.
     */
    private void stopEffects() {

        // Stop the fireworks animation.
        fireworks.stop();

        // Stop and release the trophy animation if it exists.
        if (trophy != null) {
            trophy.stop();
            trophy = null;
        }

        // Stop and release the pending bomb animation delay.
        if (bombDelay != null) {
            bombDelay.stop();
            bombDelay = null;
        }

        // Remove all animation nodes from the icon container.
        iconBox.getChildren().clear();
    }
}


