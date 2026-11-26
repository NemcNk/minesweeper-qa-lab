package app;

import javafx.application.Application;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ui.MinesweeperApp;

import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for the {@link Main} class.
 */
class MainTest {

    /**
     * Verifies that the JavaFX application is launched
     * with the correct application class and command-line arguments.
     */
    @Test
    void shouldLaunchJavaFxApplication() {
        String[] args = {};

        // Mock the static methods of the JavaFX Application class.
        try (MockedStatic<Application> applicationMock =
                     mockStatic(Application.class)) {

            // Execute the application entry point.
            Main.main(args);

            // Verify that the application is launched with the expected arguments.
            applicationMock.verify(() ->
                    Application.launch(MinesweeperApp.class, args)
            );
        }
    }
}
