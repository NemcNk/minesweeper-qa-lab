package model;

/**
 * an unalterable object that stores information about a cell
 * at a specific point in time and allows it to be compared
 * with other images.
 */

public final class CellSnapshot {

    private final CellContent content;
    private final CellState state;

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

    public CellContent getContent() {
        return content;
    }

    public CellState getState() {
        return state;
    }

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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(content, state);
    }
}
