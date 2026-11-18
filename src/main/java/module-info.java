module minesweeper {

    requires javafx.controls;
    requires java.desktop;

    opens images;
    opens styles;

    exports app;
    exports ui;
}
