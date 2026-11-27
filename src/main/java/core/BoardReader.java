package core;

import model.CellSnapshot;
import model.Coord;

/** Read-only view of a board. This is all a renderer needs to know.
 */
public interface BoardReader {

    int getColumns();

    int getRows();

    /** Returns a snapshot of the specified cell.
     *
     * @param coord a cell on the board
     * @return the snapshot, never {@code null}
     * @throws IndexOutOfBoundsException if {@code coord} is {@code null}
     *                                   or outside the board.*/
    CellSnapshot getCell(Coord coord);
}
