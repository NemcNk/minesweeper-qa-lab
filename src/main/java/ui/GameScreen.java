package ui;

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

import session.GameResult;
import session.GameSession;
import session.GameSessionListener;

/**
 * Represents the main Minesweeper game screen.
 *
 * <p>The screen only draws and forwards input. The rules (timer, pause,
 * restart, recording the result) live in {@link GameSession}; the screen
 * redraws itself from the session callbacks.
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

    private final GameSession session;
    private final Runnable backAction;

    private final BoardView boardView;
    private final GameTopPanel topPanel;
    private final PauseOverlay pauseOverlay = new PauseOverlay();
    private final ResultOverlay resultOverlay;
    private final Button pauseButton = new Button(PAUSE_TEXT);

    private PauseTransition resultDelay;

    /**
     * Creates the game screen and initializes its components.
     *
     * @param difficulty selected difficulty, defines the cell size
     * @param session game rules and state shown by this screen
     * @param backAction action executed when returning to the menu
     */
    public GameScreen(
            Difficulty difficulty,
            GameSession session,
            Runnable backAction
    ) {
        Objects.requireNonNull(difficulty);

        this.session = Objects.requireNonNull(session);
        this.backAction = Objects.requireNonNull(backAction);

        getStyleClass().add("game-root");
        setId(UiIds.GAME_SCREEN);
        pauseOverlay.setId(UiIds.PAUSE_OVERLAY);

        topPanel = new GameTopPanel(session::restart);

        boardView = new BoardView(
                session.getBoard(),
                BoardLayout.cellSize(difficulty),
                this::handleClick
        );

        resultOverlay = new ResultOverlay(
                session::restart,
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
        pauseButton.setId(UiIds.PAUSE_BUTTON);
        pauseButton.setDisable(!session.isPauseAvailable());
        pauseButton.setOnAction(event -> session.togglePause());
        fixWidth(pauseButton, BUTTON_WIDTH);

        Button backButton = new Button("←   Back to menu");
        backButton.getStyleClass().add("stats-link-button");
        backButton.setId(UiIds.BACK_BUTTON);
        backButton.setOnAction(event -> exitToMenu());
        fixWidth(backButton, BUTTON_WIDTH);

        HBox buttons = new HBox(BUTTON_GAP, backButton, pauseButton);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, topPanel, boardStack, buttons);
        content.setAlignment(Pos.CENTER);

        getChildren().addAll(content, resultOverlay);

        setMinHeight(400);

        topPanel.updateMines(session.getRemainingBombs());
        topPanel.updateTime(session.getRemainingSeconds());

        session.setListener(new SessionObserver());

        fadeIn();
    }

    // Input

    /**
     * Forwards a mouse click on the board to the session.
     * During the pause the session ignores clicks as well as the
     * PauseOverlay, which covers the board.
     *
     * @param coord coordinate of the clicked cell
     * @param event mouse event containing the pressed button
     */
    private void handleClick(Coord coord, MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            session.leftClick(coord);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            session.rightClick(coord);
        }
    }

    // Reactions to the session

    /**
     * Redraws the screen when the session changes.
     */
    private final class SessionObserver implements GameSessionListener {

        @Override
        public void boardChanged() {
            boardView.update();
        }

        @Override
        public void minesChanged(int remaining) {
            topPanel.updateMines(remaining);
        }

        @Override
        public void timeChanged(int remainingSeconds) {
            topPanel.updateTime(remainingSeconds);
        }

        @Override
        public void pauseChanged(boolean paused) {
            pauseOverlay.setVisible(paused);
            pauseButton.setText(paused ? RESUME_TEXT : PAUSE_TEXT);
        }

        @Override
        public void pauseAvailabilityChanged(boolean available) {
            pauseButton.setDisable(!available);
        }

        @Override
        public void finished(GameResult result) {
            topPanel.setFace(
                    result.isWon()
                            ? FaceButton.Face.WON
                            : FaceButton.Face.LOST
            );

            showResult(result);
        }

        @Override
        public void restarted() {
            stopResultDelay();
            resultOverlay.hide();

            topPanel.setFace(FaceButton.Face.PLAYING);
        }
    }

    /**
     * Schedules the display of the game result after a short delay.
     *
     * @param result how the game ended
     */
    private void showResult(GameResult result) {
        String subtitle = subtitleFor(result.state());

        stopResultDelay();

        resultDelay = new PauseTransition(RESULT_DELAY);
        resultDelay.setOnFinished(event -> resultOverlay.show(
                result.isWon(),
                subtitle,
                result.elapsedSeconds()
        ));
        resultDelay.play();
    }

    /**
     * Returns the message shown under the result title.
     *
     * @param state final state of the game
     * @return message for the player
     */
    private static String subtitleFor(GameState state) {
        return switch (state) {
            case LOST_BOMB -> "You hit a mine!";
            case TIMEOUT -> "Time's up!";
            case WON -> "Congratulations!";
            case PLAYING -> throw new IllegalStateException(
                    "Unexpected game state: " + state
            );
        };
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
     * Stops active transitions and returns to the main menu.
     */
    private void exitToMenu() {
        stopResultDelay();
        session.dispose();

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
