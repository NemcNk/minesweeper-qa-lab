package ui;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.util.Objects;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import stats.JsonStatsRepository;

/**
 * Main application class responsible for initializing and launching Minesweeper.
 */
public final class MinesweeperApp extends Application {

    private static final String ICON_PATH = "/images/iconModern.png";
    private static final String CSS_PATH = "/styles/application.css";

    /**
     * Initializes the application window, scene, styles, and icons.
     *
     * @param stage the primary application stage
     */
    @Override
    public void start(Stage stage) {
        // Set the application window icon.
        stage.getIcons().add(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(ICON_PATH),
                        "Icon not found: " + ICON_PATH
                )
        ));

        MinesweeperController controller = new MinesweeperController(
                stage,
                new JsonStatsRepository(JsonStatsRepository.defaultFile())
        );

        Scene scene = new Scene(controller.createContent());

        // Load the application stylesheet.
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

        // Set the application icon in the macOS Dock.
        setDockIcon();
    }

    /**
     * Sets the application icon in the macOS Dock when supported.
     */
    private void setDockIcon() {
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();

                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    var url = Objects.requireNonNull(
                            getClass().getResource(ICON_PATH),
                            "Icon not found: " + ICON_PATH
                    );

                    taskbar.setIconImage(
                            Toolkit.getDefaultToolkit().getImage(url)
                    );
                }
            }
        } catch (Exception e) {
            // The application icon is purely decorative and does not affect functionality.
            e.printStackTrace();
        }
    }
}







