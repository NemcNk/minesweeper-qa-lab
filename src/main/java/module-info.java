module minesweeper {

    requires javafx.controls;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;

    opens images;
    opens styles;

    exports app;
    exports ui;
}
