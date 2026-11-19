package ui;

import java.util.Objects;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import model.Difficulty;
import stats.StatsRepository;

/**
 * Controls navigation between the main menu, game screen,
 * and statistics screen.
 */
public final class MinesweeperController {

    /** Root container used to display application screens. */
    private final StackPane root = new StackPane();

    /** Main application window. */
    private final Stage stage;

    /** Repository used to access game statistics. */
    private final StatsRepository stats;

    /**
     * Creates a controller for the Minesweeper application.
     *
     * @param stage the main application window
     * @param stats the repository for game statistics
     */
    public MinesweeperController(Stage stage, StatsRepository stats) {
        this.stage = Objects.requireNonNull(stage);
        this.stats = Objects.requireNonNull(stats);
    }

    /**
     * Creates and returns the main application content.
     *
     * @return the root container
     */
    public StackPane createContent() {
        showMenu();

        return root;
    }

    /**
     * Displays the main menu.
     */
    private void showMenu() {
        MainMenuView menu = new MainMenuView(
                this::startGame,
                this::showStatistics
        );

        root.getChildren().setAll(menu);

        resizeWindow(420, 850);
    }

    /**
     * Starts a new game with the selected difficulty.
     *
     * @param difficulty the selected game difficulty
     */
    private void startGame(Difficulty difficulty) {
        GameScreen gameScreen = new GameScreen(
                difficulty,
                stats,
                this::showMenu
        );

        root.getChildren().setAll(gameScreen);

        // Wait until the game screen has been rendered
        // before calculating its actual dimensions.
        Platform.runLater(() -> {
            double width = gameScreen.getContentWidth();
            double height = gameScreen.getContentHeight();

            resizeWindow(width + 40, height + 60);
        });
    }

    /**
     * Displays the game statistics screen.
     */
    private void showStatistics() {
        StatisticsView statistics = new StatisticsView(
                stats,
                this::showMenu
        );

        root.getChildren().setAll(statistics);

        resizeWindow(520, 420);
    }

    /**
     * Resizes the application window and centers it on the screen.
     *
     * @param width the desired window width
     * @param height the desired window height
     */
    private void resizeWindow(double width, double height) {
        stage.setWidth(width);
        stage.setHeight(height);

        stage.centerOnScreen();
    }
}








