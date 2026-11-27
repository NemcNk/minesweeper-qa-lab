package ui;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.util.Objects;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import stats.JsonStatsRepository;

/** Main application class responsible for initializing and launching Minesweeper.
 */
public final class MinesweeperApp extends Application {

    /** Defines the path to the application icon resource. */
    private static final String ICON_PATH = "/images/iconModern.png";

    /** Defines the path to the application stylesheet resource. */
    private static final String CSS_PATH = "/styles/application.css";

    /** Initializes the application window, scene, styles, and icons.
     * @param stage the primary application stage.*/
    @Override
    public void start(Stage stage) {
        // Load and set the application window icon.
        stage.getIcons().add(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(ICON_PATH),
                        "Icon not found: " + ICON_PATH
                )
        ));

        // Initialize the application controller and statistics repository.
        MinesweeperController controller = new MinesweeperController(
                stage,
                new JsonStatsRepository(JsonStatsRepository.defaultFile())
        );

        // Create the main scene using the content provided by the controller.
        Scene scene = new Scene(controller.createContent());

        // Load the application stylesheet.
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource(CSS_PATH),
                        "CSS file not found: " + CSS_PATH
                ).toExternalForm()
        );

        // Configure and display the main application window.
        stage.setTitle("Minesweeper");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Set the application icon in the macOS Dock when supported.
        setDockIcon();
    }

    /** Sets the application icon in the macOS Dock when supported.
     *
     * <p>The icon is updated only if the current platform supports
     * the required taskbar functionality.</p>*/
    private void setDockIcon() {
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();

                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    // Retrieve the application icon resource.
                    var url = Objects.requireNonNull(
                            getClass().getResource(ICON_PATH),
                            "Icon not found: " + ICON_PATH
                    );

                    // Apply the icon to the system taskbar.
                    taskbar.setIconImage(
                            Toolkit.getDefaultToolkit().getImage(url)
                    );
                }
            }
        } catch (Exception e) {
            // Ignore icon configuration errors because they do not affect gameplay.
            e.printStackTrace();
        }
    }
}








