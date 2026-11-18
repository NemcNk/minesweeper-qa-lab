package ui;

import model.CellContent;
import model.CellSnapshot;
import model.CellState;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public final class CellView extends StackPane {

    private static final double GLYPH_SCALE = 0.78;
    private static final String FLAG_COLOR = "#e63946";

    private final int cellSize;
    private final Label numberLabel = new Label();

    private CellSnapshot currentCell;

    public CellView(int cellSize) {

        this.cellSize = cellSize;

        setMinSize(cellSize, cellSize);
        setPrefSize(cellSize, cellSize);
        setMaxSize(cellSize, cellSize);

        setAlignment(Pos.CENTER);

        getStyleClass().add("cell");

        numberLabel.getStyleClass().add("cell-number");
        numberLabel.setStyle(fontSizeStyle());

        setCell(new CellSnapshot(
                CellContent.ZERO,
                CellState.CLOSED
        ));
    }

    public void setCell(CellSnapshot cell) {

        currentCell = Objects.requireNonNull(
                cell,
                "Cell cannot be null"
        );

        getChildren().clear();
        getStyleClass().removeAll("closed", "opened", "bombed");

        switch (cell.getState()) {

            case CLOSED -> getStyleClass().add("closed");

            case FLAGGED -> showFlag();

            case EXPLODED -> showExploded();

            case WRONG_FLAG -> showWrongFlag();

            case OPENED -> showOpened(cell.getContent());
        }
    }

    public CellSnapshot getCurrentCell() {
        return currentCell;
    }

    // state of the game

    private void showFlag() {

        getStyleClass().add("closed");
        getChildren().add(createGlyph("⚑", FLAG_COLOR));
    }

    private void showExploded() {

        getStyleClass().addAll("opened", "bombed");
        getChildren().add(new MineIcon(cellSize));
    }

    private void showWrongFlag() {

        getStyleClass().add("opened");
        getChildren().add(createGlyph("✕", FLAG_COLOR));
    }

    private void showOpened(CellContent content) {

        getStyleClass().add("opened");

        if (content == CellContent.BOMB) {
            getChildren().add(new MineIcon(cellSize));
            return;
        }

        int number = content.getNumber();

        if (number > 0) {

            numberLabel.setText(String.valueOf(number));

            numberLabel.getStyleClass()
                    .removeIf(style -> style.startsWith("number-"));
            numberLabel.getStyleClass().add("number-" + number);

            getChildren().add(numberLabel);
        }
    }

    // helpers

    private Label createGlyph(String text, String color) {

        Label glyph = new Label(text);

        glyph.setStyle(
                fontSizeStyle() + "-fx-text-fill: " + color + ";"
        );

        return glyph;
    }

    private String fontSizeStyle() {
        return "-fx-font-size: " + (cellSize * GLYPH_SCALE) + "px;";
    }
}




