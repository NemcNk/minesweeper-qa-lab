package app;

import javafx.application.Application;
import ui.MinesweeperApp;

/**
 * The entry point of the Minesweeper application.
 */
public final class Main {

    /**
     * Prevents instantiation of the Main class.
     */
    private Main() {
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Application.launch(
                MinesweeperApp.class,
                args
        );
    }
}


