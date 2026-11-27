package model;

import java.util.Objects;

/** Represents an immutable coordinate on a two-dimensional plane.
 */
public final class Coord {

    /** The X coordinate. */
    private final int coordinateX;

    /** The Y coordinate. */
    private final int coordinateY;

    /** Creates a coordinate with the specified X and Y values.
     * @param coordinateX the X coordinate
     * @param coordinateY the Y coordinate.*/
    public Coord(int coordinateX, int coordinateY) {
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
    }

    /** Returns the X coordinate.
     * @return the X coordinate.*/
    public int getX() {
        return coordinateX;
    }

    /** Returns the Y coordinate.
     * @return the Y coordinate.*/
    public int getY() {
        return coordinateY;
    }

    /** Checks whether the specified object is equal to this coordinate.
     *
     * @param object the object to compare with this coordinate
     * @return true if both coordinates have the same X and Y values,
     *         false otherwise.*/
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Coord coord)) {
            return false;
        }

        return coordinateX == coord.coordinateX
                && coordinateY == coord.coordinateY;
    }

    /** Returns the hash code of this coordinate.
     * @return the hash code based on the X and Y coordinates.*/
    @Override
    public int hashCode() {
        return Objects.hash(coordinateX, coordinateY);
    }
}


