package ui;

import core.Game;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import model.Coord;
import model.Difficulty;
import model.GameState;

import java.util.Objects;

import stats.StatsRepository;

/**
 * Represents the main Minesweeper game screen.
 * Manages the game board, timer, pause functionality,
 * and game result display.
 */
public final class GameScreen extends StackPane {

    private static final double BUTTON_WIDTH = 170;
    private static final double BUTTON_GAP = 10;
    private static final double BUTTONS_WIDTH =
            BUTTON_WIDTH * 2 + BUTTON_GAP;

    /** Delay before displaying the game result. */
    private static final Duration RESULT_DELAY =
            Duration.millis(1000);

    private static final String PAUSE_TEXT = "⏸   Pause";
    private static final String RESUME_TEXT = "▶   Resume";

    private final Difficulty difficulty;
    private final StatsRepository stats;
    private final Runnable backAction;

    private final Game game;
    private final BoardView boardView;
    private final GameTopPanel topPanel;
    private final CountdownTimer timer;
    private final PauseOverlay pauseOverlay = new PauseOverlay();
    private final ResultOverlay resultOverlay;
    private final Button pauseButton = new Button(PAUSE_TEXT);

    private PauseTransition resultDelay;

    private boolean paused;
    private boolean resultRecorded;

    /**
     * Creates the game screen and initializes its components.
     *
     * @param difficulty selected game difficulty
     * @param stats repository for storing game statistics
     * @param backAction action executed when returning to the menu
     */
    public GameScreen(
            Difficulty difficulty,
            StatsRepository stats,
            Runnable backAction
    ) {
        this.difficulty = Objects.requireNonNull(difficulty);
        this.stats = Objects.requireNonNull(stats);
        this.backAction = Objects.requireNonNull(backAction);

        getStyleClass().add("game-root");

        game = new Game(difficulty.createConfig());

        topPanel = new GameTopPanel(this::restart);

        timer = new CountdownTimer(
                difficulty.getTimeMinutes() * 60,
                new FxTicker(),
                topPanel::updateTime,
                this::onTimeExpired
        );

        boardView = new BoardView(
                game,
                BoardLayout.cellSize(difficulty),
                this::handleClick
        );

        resultOverlay = new ResultOverlay(
                this::restart,
                this::exitToMenu
        );

        // The top panel and the game board have the same fixed width.
        double boardWidth = boardView.getBoardWidth();

        fixWidth(topPanel, boardWidth);
        fixWidth(boardView.getNode(), boardWidth);

        StackPane boardStack = new StackPane(
                boardView.getNode(),
                pauseOverlay
        );
        fixWidth(boardStack, boardWidth);

        // Pausing is unavailable until the first move is made.
        pauseButton.getStyleClass().add("stats-link-button");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(event -> togglePause());
        fixWidth(pauseButton, BUTTON_WIDTH);

        Button backButton = new Button("←   Back to menu");
        backButton.getStyleClass().add("stats-link-button");
        backButton.setOnAction(event -> exitToMenu());
        fixWidth(backButton, BUTTON_WIDTH);

        HBox buttons = new HBox(BUTTON_GAP, backButton, pauseButton);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, topPanel, boardStack, buttons);
        content.setAlignment(Pos.CENTER);

        getChildren().addAll(content, resultOverlay);

        setMinHeight(400);

        topPanel.updateMines(game.getRemainingBombs());
        topPanel.updateTime(timer.getRemainingSeconds());

        fadeIn();
    }

    // Input

    /**
     * Handles mouse clicks on the game board.
     * Starts the timer on the first move and updates the game interface.
     *
     * @param coord coordinate of the clicked cell
     * @param event mouse event containing the pressed button
     */
    private void handleClick(Coord coord, MouseEvent event) {
        // During the pause, clicks are blocked by the PauseOverlay.
        if (game.getState() != GameState.PLAYING) {
            return;
        }

        if (event.getButton() == MouseButton.PRIMARY) {
            boolean wasStarted = game.isStarted();

            game.pressLeftButton(coord);

            if (!wasStarted && game.isStarted()) {
                timer.start();
                pauseButton.setDisable(false);
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            game.pressRightButton(coord);
        }

        boardView.update();
        topPanel.updateMines(game.getRemainingBombs());

        updateGameState();
    }

    /**
     * Toggles the game pause state and updates the timer.
     */
    private void togglePause() {
        if (game.getState() != GameState.PLAYING || !game.isStarted()) {
            return;
        }

        paused = !paused;

        if (paused) {
            timer.pause();
        } else {
            timer.resume();
        }

        updatePauseUi();
    }

    /**
     * Updates the pause overlay and pause button text.
     */
    private void updatePauseUi() {
        pauseOverlay.setVisible(paused);
        pauseButton.setText(paused ? RESUME_TEXT : PAUSE_TEXT);
    }

    // Game state

    /**
     * Handles the expiration of the game timer.
     * Changes the game state to timeout and updates the board.
     */
    private void onTimeExpired() {
        game.timeout();

        boardView.update();

        updateGameState();
    }

    /**
     * Updates the interface according to the current game state.
     */
    private void updateGameState() {
        GameState state = game.getState();

        switch (state) {
            case LOST_BOMB -> finish(false, "You hit a mine!");
            case TIMEOUT -> finish(false, "Time's up!");
            case WON -> finish(true, "Congratulations!");
            case PLAYING -> {
                // The game continues.
            }
            default -> throw new IllegalStateException(
                    "Unexpected game state: " + state
            );
        }
    }

    /**
     * Finishes the game, stops the timer and displays the result.
     *
     * @param won true if the player won, otherwise false
     * @param subtitle message displayed with the game result
     */
    private void finish(boolean won, String subtitle) {
        timer.stop();
        pauseButton.setDisable(true);

        topPanel.setFace(
                won ? FaceButton.Face.WON : FaceButton.Face.LOST
        );

        recordResult(won);
        showResult(won, subtitle);
    }

    /**
     * Records the game result in the statistics repository.
     * Prevents the same result from being recorded more than once.
     *
     * @param won true if the player won, otherwise false
     */
    private void recordResult(boolean won) {
        if (resultRecorded) {
            return;
        }

        resultRecorded = true;

        if (won) {
            stats.recordWin(difficulty, timer.getElapsedSeconds());
        } else {
            stats.recordLoss(difficulty);
        }
    }

    /**
     * Schedules the display of the game result after a short delay.
     *
     * @param won true if the player won, otherwise false
     * @param subtitle message displayed with the game result
     */
    private void showResult(boolean won, String subtitle) {
        int elapsed = timer.getElapsedSeconds();

        stopResultDelay();

        resultDelay = new PauseTransition(RESULT_DELAY);
        resultDelay.setOnFinished(
                event -> resultOverlay.show(won, subtitle, elapsed)
        );
        resultDelay.play();
    }

    /**
     * Stops the pending result display animation.
     */
    private void stopResultDelay() {
        if (resultDelay != null) {
            resultDelay.stop();
            resultDelay = null;
        }
    }

    // Screen controls

    /**
     * Restarts the current game and resets its interface.
     */
    private void restart() {
        stopResultDelay();
        resultOverlay.hide();

        timer.reset();

        paused = false;
        updatePauseUi();
        pauseButton.setDisable(true);

        resultRecorded = false;

        game.start();
        boardView.update();

        topPanel.setFace(FaceButton.Face.PLAYING);
        topPanel.updateMines(game.getRemainingBombs());
    }

    /**
     * Stops active transitions and returns to the main menu.
     */
    private void exitToMenu() {
        stopResultDelay();
        timer.stop();

        backAction.run();
    }

    /**
     * Plays the fade-in animation when the game screen appears.
     */
    private void fadeIn() {
        setOpacity(0);

        FadeTransition fade = new FadeTransition(
                Duration.millis(300),
                this
        );

        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Sets the minimum, preferred and maximum width of a region.
     *
     * @param region JavaFX region whose width is being configured
     * @param width target width
     */
    private static void fixWidth(Region region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }

    // Sizes

    /**
     * Returns the preferred width of the game screen content.
     *
     * @return calculated content width
     */
    public double getContentWidth() {
        return Math.max(
                Math.max(boardView.getBoardWidth(), topPanel.prefWidth(-1)),
                BUTTONS_WIDTH
        );
    }

    /**
     * Returns the preferred height of the game screen content.
     *
     * @return calculated content height
     */
    public double getContentHeight() {
        return boardView.getBoardHeight()
                + topPanel.prefHeight(-1)
                + 10
                + 40
                + 20;
    }
}




