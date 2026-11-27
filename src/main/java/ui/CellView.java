package ui;

import java.util.Objects;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import model.CellContent;
import model.CellSnapshot;
import model.CellState;

/** Represents a single cell in the Minesweeper game interface.
 * Displays the visual representation of the current cell state.
 */
public final class CellView extends StackPane {

    /** Defines the width and height of the cell in pixels. */
    private final int cellSize;

    /** Displays the numerical value of an opened cell. */
    private final Label numberLabel = new Label();

    /** Stores the current snapshot representing the cell state and content. */
    private CellSnapshot currentCell;

    /** Creates a cell view with the specified size.
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

        // Initialize the cell as closed and empty.
        setCell(new CellSnapshot(
                CellContent.ZERO,
                CellState.CLOSED
        ));
    }

    /** Updates the cell appearance according to its current state.
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

    /** Returns the current cell snapshot.
     * @return the current cell snapshot*/
    public CellSnapshot getCurrentCell() {
        return currentCell;
    }

    /** Displays a flag on a closed cell.*/
    private void showFlag() {
        getStyleClass().add("closed");
        getChildren().add(createGlyph("⚑", "cell-flag"));
    }

    /** Displays an exploded mine.*/
    private void showExploded() {
        getStyleClass().addAll("opened", "bombed");
        getChildren().add(new MineIcon(cellSize));
    }

    /** Displays a cross indicating an incorrect flag.*/
    private void showWrongFlag() {
        getStyleClass().add("opened");
        getChildren().add(createGlyph("✕", "cell-wrong-flag"));
    }

    /** Displays the content of an opened cell.
     * @param content the content of the opened cell*/
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
                    .removeIf(style ->
                            style.startsWith("number-")
                                    || style.startsWith("cell-number-")
                    );

            numberLabel.getStyleClass().addAll(
                    "number-" + number,
                    getGlyphSizeClass()
            );

            getChildren().add(numberLabel);
        }
    }

    /**
     * Creates a graphical label with the specified CSS class.
     *
     * @param text the glyph to display
     * @param styleClass the CSS class applied to the glyph
     * @return the configured label
     */
    private Label createGlyph(String text, String styleClass) {
        Label glyph = new Label(text);

        glyph.getStyleClass().addAll(
                "cell-glyph",
                getGlyphSizeClass(),
                styleClass
        );

        return glyph;
    }

    /**
     * Determines the CSS size class according to the cell dimensions.
     *
     * @return the CSS class representing the glyph size
     */
    private String getGlyphSizeClass() {
        if (cellSize <= 21) {
            return "cell-glyph-small";
        }

        if (cellSize <= 30) {
            return "cell-glyph-medium";
        }

        return "cell-glyph-large";
    }
}