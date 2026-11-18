package ui;

import core.Game;

import model.Coord;
import model.Difficulty;
import model.GameState;
import stats.StatsRepository;

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

import java.util.Objects;

public final class GameScreen extends StackPane {

    private static final double BUTTON_WIDTH = 170;
    private static final double BUTTON_GAP = 10;
    private static final double BUTTONS_WIDTH =
            BUTTON_WIDTH * 2 + BUTTON_GAP;

    // Delay before the results screen: allows the player to see the revealed mines.
    private static final Duration RESULT_DELAY = Duration.millis(1000);

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

    private void updatePauseUi() {

        pauseOverlay.setVisible(paused);
        pauseButton.setText(paused ? RESUME_TEXT : PAUSE_TEXT);
    }

    // game state

    private void onTimeExpired() {

        game.timeout();

        boardView.update();

        updateGameState();
    }

    private void updateGameState() {

        GameState state = game.getState();

        switch (state) {
            case LOST_BOMB -> finish(false, "You hit a mine!");
            case TIMEOUT -> finish(false, "Time's up!");
            case WON -> finish(true, "Congratulations!");
            case PLAYING -> {
                // the game continues
            }
        }
    }

    private void finish(boolean won, String subtitle) {

        timer.stop();
        pauseButton.setDisable(true);

        topPanel.setFace(
                won ? FaceButton.Face.WON : FaceButton.Face.LOST
        );

        recordResult(won);
        showResult(won, subtitle);
    }

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

    private void showResult(boolean won, String subtitle) {

        int elapsed = timer.getElapsedSeconds();

        stopResultDelay();

        resultDelay = new PauseTransition(RESULT_DELAY);
        resultDelay.setOnFinished(
                event -> resultOverlay.show(won, subtitle, elapsed)
        );
        resultDelay.play();
    }

    private void stopResultDelay() {

        if (resultDelay != null) {
            resultDelay.stop();
            resultDelay = null;
        }
    }

    // Screen controls

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

    private void exitToMenu() {

        stopResultDelay();
        timer.stop();

        backAction.run();
    }

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

    private static void fixWidth(Region region, double width) {

        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }

    // sizes

    public double getContentWidth() {

        return Math.max(
                Math.max(boardView.getBoardWidth(), topPanel.prefWidth(-1)),
                BUTTONS_WIDTH
        );
    }

    public double getContentHeight() {

        return boardView.getBoardHeight()
                + topPanel.prefHeight(-1)
                + 10
                + 40
                + 20;
    }
}



