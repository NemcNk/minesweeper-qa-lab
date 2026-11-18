package ui;

import core.BoardReader;

import model.CellSnapshot;
import model.Coord;

import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

public final class BoardView {

    private static final int GAP = 2;

    private final CellView[][] cells;
    private final BoardReader board;
    private final GridPane grid = new GridPane();
    private final int cellSize;
    private final BiConsumer<Coord, MouseEvent> clickHandler;

    public BoardView(
            BoardReader board,
            int cellSize,
            BiConsumer<Coord, MouseEvent> clickHandler
    ) {

        this.board = board;
        this.cellSize = cellSize;
        this.clickHandler = clickHandler;

        cells = new CellView[
                board.getColumns()
                ][
                board.getRows()
                ];

        createBoard();

        update();
    }

    public double getBoardWidth() {

        return board.getColumns() * cellSize
                + (board.getColumns() - 1) * GAP;
    }

    public double getBoardHeight() {

        return board.getRows() * cellSize
                + (board.getRows() - 1) * GAP;
    }

    private void createBoard() {

        for (int y = 0; y < board.getRows(); y++) {

            for (int x = 0; x < board.getColumns(); x++) {

                Coord coord = new Coord(x, y);

                CellView cell = new CellView(cellSize);

                cells[x][y] = cell;

                cell.setOnMouseClicked(event ->
                        clickHandler.accept(coord, event)
                );

                grid.add(cell, x, y);
            }
        }

        grid.setHgap(GAP);
        grid.setVgap(GAP);
        grid.setAlignment(Pos.CENTER);
    }

    public GridPane getNode() {
        return grid;
    }

    public void update() {

        for (int y = 0; y < board.getRows(); y++) {

            for (int x = 0; x < board.getColumns(); x++) {

                CellSnapshot cell = board.getCell(new Coord(x, y));

                CellView cellView = cells[x][y];

                if (!cellView.getCurrentCell().equals(cell)) {
                    cellView.setCell(cell);
                }
            }
        }
    }
}

