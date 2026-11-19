package ui;

import java.util.Objects;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import model.CellContent;
import model.CellSnapshot;
import model.CellState;

/**
 * Represents a single cell in the Minesweeper game interface.
 * Displays the visual representation of the current cell state.
 */
public final class CellView extends StackPane {

    private static final double GLYPH_SCALE = 0.78;
    private static final String FLAG_COLOR = "#e63946";

    private final int cellSize;
    private final Label numberLabel = new Label();

    private CellSnapshot currentCell;

    /**
     * Creates a cell view with the specified size.
     *
     * @param cellSize the width and height of the cell in pixels
     */
    public CellView(int cellSize) {
        this.cellSize = cellSize;

        setMinSize(cellSize, cellSize);
        setPrefSize(cellSize, cellSize);
        setMaxSize(cellSize, cellSize);

        setAlignment(Pos.CENTER);

        getStyleClass().add("cell");

        numberLabel.getStyleClass().add("cell-number");
        numberLabel.setStyle(fontSizeStyle());

        // Initialize the cell as closed and empty.
        setCell(new CellSnapshot(
                CellContent.ZERO,
                CellState.CLOSED
        ));
    }

    /**
     * Updates the cell appearance according to its current state.
     *
     * @param cell the new cell snapshot
     * @throws NullPointerException if the provided cell is null
     */
    public void setCell(CellSnapshot cell) {
        currentCell = Objects.requireNonNull(
                cell,
                "Cell cannot be null"
        );

        getChildren().clear();
        getStyleClass().removeAll("closed", "opened", "bombed");

        // Display the appropriate appearance for each cell state.
        switch (cell.getState()) {
            case CLOSED -> getStyleClass().add("closed");

            case FLAGGED -> showFlag();

            case EXPLODED -> showExploded();

            case WRONG_FLAG -> showWrongFlag();

            case OPENED -> showOpened(cell.getContent());

            default -> throw new IllegalStateException(
                    "Unknown cell state: " + cell.getState()
            );
        }
    }

    /**
     * Returns the current cell snapshot.
     *
     * @return the current cell snapshot
     */
    public CellSnapshot getCurrentCell() {
        return currentCell;
    }

    // Cell state rendering methods.

    /**
     * Displays a flag on a closed cell.
     */
    private void showFlag() {
        getStyleClass().add("closed");
        getChildren().add(createGlyph("⚑", FLAG_COLOR));
    }

    /**
     * Displays an exploded mine.
     */
    private void showExploded() {
        getStyleClass().addAll("opened", "bombed");
        getChildren().add(new MineIcon(cellSize));
    }

    /**
     * Displays a cross indicating an incorrect flag.
     */
    private void showWrongFlag() {
        getStyleClass().add("opened");
        getChildren().add(createGlyph("✕", FLAG_COLOR));
    }

    /**
     * Displays the content of an opened cell.
     *
     * @param content the content of the opened cell
     */
    private void showOpened(CellContent content) {
        getStyleClass().add("opened");

        // Display a mine icon if the cell contains a bomb.
        if (content == CellContent.BOMB) {
            getChildren().add(new MineIcon(cellSize));
            return;
        }

        int number = content.getNumber();

        // Display the number of adjacent mines when greater than zero.
        if (number > 0) {
            numberLabel.setText(String.valueOf(number));

            numberLabel.getStyleClass()
                    .removeIf(style -> style.startsWith("number-"));

            numberLabel.getStyleClass().add("number-" + number);

            getChildren().add(numberLabel);
        }
    }

    // Helper methods.

    /**
     * Creates a graphical label with the specified text and color.
     *
     * @param text the glyph to display
     * @param color the glyph color
     * @return the configured label
     */
    private Label createGlyph(String text, String color) {
        Label glyph = new Label(text);

        glyph.setStyle(
                fontSizeStyle() + "-fx-text-fill: " + color + ";"
        );

        return glyph;
    }

    /**
     * Calculates the font size based on the cell dimensions.
     *
     * @return the CSS font size declaration
     */
    private String fontSizeStyle() {
        return "-fx-font-size: " + (cellSize * GLYPH_SCALE) + "px;";
    }
}





