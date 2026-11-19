package model;

/**
 * An immutable object that stores information about a cell
 * at a specific point in time and allows it to be compared
 * with other snapshots.
 */
public final class CellSnapshot {

    private final CellContent content;
    private final CellState state;

    /**
     * Creates a snapshot of a cell with the specified content and state.
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

    /**
     * Returns the content stored in this snapshot.
     *
     * @return the cell content
     */
    public CellContent getContent() {
        return content;
    }

    /**
     * Returns the state stored in this snapshot.
     *
     * @return the cell state
     */
    public CellState getState() {
        return state;
    }

    /**
     * Checks whether this snapshot is equal to another object.
     *
     * @param object the object to compare with
     * @return true if both snapshots contain the same content and state
     */
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

    /**
     * Calculates the hash code based on the cell content and state.
     *
     * @return the hash code of this snapshot
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(content, state);
    }
}

