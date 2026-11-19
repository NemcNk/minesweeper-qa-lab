package ui;

import core.BoardReader;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import model.CellSnapshot;
import model.Coord;
import java.util.function.BiConsumer;

/**
 * Represents the visual game board and manages its cell views.
 * Synchronizes the visual representation with the underlying board.
 */
public final class BoardView {

    private static final int GAP = 2;

    private final CellView[][] cells;
    private final BoardReader board;
    private final GridPane grid = new GridPane();
    private final int cellSize;
    private final BiConsumer<Coord, MouseEvent> clickHandler;

    /**
     * Creates a board view and initializes all cell components.
     *
     * @param board the board data source
     * @param cellSize the size of each cell in pixels
     * @param clickHandler the handler for cell click events
     */
    public BoardView(
            BoardReader board,
            int cellSize,
            BiConsumer<Coord, MouseEvent> clickHandler
    ) {
        this.board = board;
        this.cellSize = cellSize;
        this.clickHandler = clickHandler;

        // Initialize the cell array using the board dimensions.
        cells = new CellView[board.getColumns()][board.getRows()];

        createBoard();
        update();
    }

    /**
     * Returns the total width of the board, including gaps.
     *
     * @return the board width in pixels
     */
    public double getBoardWidth() {
        return board.getColumns() * cellSize
                + (board.getColumns() - 1) * GAP;
    }

    /**
     * Returns the total height of the board, including gaps.
     *
     * @return the board height in pixels
     */
    public double getBoardHeight() {
        return board.getRows() * cellSize
                + (board.getRows() - 1) * GAP;
    }

    /**
     * Creates the grid and initializes all cell views.
     * Each cell receives a click handler associated with its coordinates.
     */
    private void createBoard() {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {

                // Store the coordinates of the current cell.
                Coord coord = new Coord(x, y);

                CellView cell = new CellView(cellSize);

                cells[x][y] = cell;

                // Forward mouse click events to the provided handler.
                cell.setOnMouseClicked(event ->
                        clickHandler.accept(coord, event)
                );

                grid.add(cell, x, y);
            }
        }

        // Configure the grid layout.
        grid.setHgap(GAP);
        grid.setVgap(GAP);
        grid.setAlignment(Pos.CENTER);
    }

    /**
     * Returns the JavaFX node representing the game board.
     *
     * @return the board grid
     */
    public GridPane getNode() {
        return grid;
    }

    /**
     * Updates cell views when their underlying data changes.
     * Only modified cells are refreshed.
     */
    public void update() {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {

                // Retrieve the latest state of the current cell.
                CellSnapshot cell = board.getCell(new Coord(x, y));

                CellView cellView = cells[x][y];

                // Avoid unnecessary UI updates.
                if (!cellView.getCurrentCell().equals(cell)) {
                    cellView.setCell(cell);
                }
            }
        }
    }
}


