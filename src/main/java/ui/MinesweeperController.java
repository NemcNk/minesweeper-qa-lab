package ui;

import model.Difficulty;
import stats.StatsRepository;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public final class MinesweeperController {

    private final StackPane root = new StackPane();

    private final Stage stage;
    private final StatsRepository stats;

    public MinesweeperController(Stage stage, StatsRepository stats) {
        this.stage = Objects.requireNonNull(stage);
        this.stats = Objects.requireNonNull(stats);
    }

    public StackPane createContent() {

        showMenu();

        return root;
    }

    // Main menu
    private void showMenu() {

        MainMenuView menu = new MainMenuView(
                this::startGame,
                this::showStatistics
        );

        root.getChildren().setAll(menu);

        resizeWindow(420, 850);
    }

    // launch of the game
    private void startGame(Difficulty difficulty) {

        GameScreen gameScreen = new GameScreen(
                difficulty,
                stats,
                this::showMenu
        );

        root.getChildren().setAll(gameScreen);

        // we obtain the actual dimensions of the game screen.
        Platform.runLater(() -> {

            double width = gameScreen.getContentWidth();
            double height = gameScreen.getContentHeight();

            resizeWindow(width + 40, height + 60);
        });
    }

    // statistics screen
    private void showStatistics() {

        StatisticsView statistics = new StatisticsView(
                stats,
                this::showMenu
        );

        root.getChildren().setAll(statistics);

        resizeWindow(520, 420);
    }

    // resizing the window
    private void resizeWindow(double width, double height) {

        stage.setWidth(width);
        stage.setHeight(height);

        stage.centerOnScreen();
    }
}







