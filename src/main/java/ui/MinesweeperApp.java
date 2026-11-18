package ui;

import stats.InMemoryStatsRepository;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.util.Objects;

public final class MinesweeperApp extends Application {

    private static final String ICON_PATH = "/images/iconModern.png";
    private static final String CSS_PATH = "/styles/application.css";

    @Override
    public void start(Stage stage) {

        // window icon (Windows / Linux)
        stage.getIcons().add(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(ICON_PATH),
                        "Icon not found: " + ICON_PATH
                )
        ));

        MinesweeperController controller = new MinesweeperController(
                stage,
                new InMemoryStatsRepository()
        );

        Scene scene = new Scene(controller.createContent());

        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource(CSS_PATH),
                        "CSS file not found: " + CSS_PATH
                ).toExternalForm()
        );

        stage.setTitle("Minesweeper");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Dock icon (macOS)
        setDockIcon();
    }

    private void setDockIcon() {

        try {
            if (Taskbar.isTaskbarSupported()) {

                Taskbar taskbar = Taskbar.getTaskbar();

                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {

                    var url = Objects.requireNonNull(
                            getClass().getResource(ICON_PATH)
                    );

                    taskbar.setIconImage(
                            Toolkit.getDefaultToolkit().getImage(url)
                    );
                }
            }
        } catch (Exception e) {
            // the icon is not essential.
            // It serves a purely decorative purpose
            // and does not affect the functionality of Minesweeper.
            e.printStackTrace();
        }
    }
}







