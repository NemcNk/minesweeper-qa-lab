package app;

import javafx.application.Application;
import ui.MinesweeperApp;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {

        Application.launch(
                MinesweeperApp.class,
                args
        );
    }
}

