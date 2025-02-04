package model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a position in the maze.
 */
public class Position implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;    public final int row;
    public final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (row != position.row) return false;
        return col == position.col;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        return result;
    }
}
