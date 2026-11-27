package model;

/** Represents an immutable snapshot of a cell at a specific point in time.
 * The snapshot stores the cell content and state and can be compared
 * with other snapshots.*/
public final class CellSnapshot {

    /** The content stored in this snapshot.*/
    private final CellContent content;

    /** The state of the cell stored in this snapshot.*/
    private final CellState state;

    /** Creates a snapshot of a cell with the specified content and state.
     *
     * @param content the content of the cell
     * @param state the current state of the cell
     * @throws IllegalArgumentException if content or state is null
     */
    public CellSnapshot(
            CellContent content,
            CellState state
    ) {
        if (content == null || state == null) {
            throw new IllegalArgumentException(
                    "Cell data cannot be null"
            );
        }

        this.content = content;
        this.state = state;
    }

    /** Returns the content stored in this snapshot.
     *
     * @return the cell content.*/
    public CellContent getContent() {
        return content;
    }

    /** Returns the state stored in this snapshot.
     *
     * @return the cell state.*/
    public CellState getState() {
        return state;
    }

    /** Compares this snapshot with the specified object.
     * Two snapshots are considered equal if they contain
     * the same cell content and state.
     *
     * @param object the object to compare with this snapshot
     * @return true if the specified object is an equal snapshot,
     *         false otherwise.*/
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CellSnapshot cell)) {
            return false;
        }

        return content == cell.content
                && state == cell.state;
    }

    /** Calculates the hash code using the cell content and state.
     *
     * @return the hash code of this snapshot.*/
    @Override
    public int hashCode() {
        return java.util.Objects.hash(content, state);
    }
}


