package org.evensen.ants;

import java.util.Objects;

/**
 * Position class to handle some basic operations on {@code <x, y>}-coordinates.
 */
public final class Position {
    private final float x;
    private final float y;

    public Position(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns a new position, at distance {@code radius} and
     * angle {@code theta} (in radians).
     * @param radius The distance to offset this position.
     * @param theta The angle, in radians.
     * @return A new {@code Position} with offset applied.
     */
    public Position move(final float radius, final float theta) {
        final float xOffset = (float) Math.cos(theta) * radius;
        final float yOffset = (float) Math.sin(theta) * radius;
        return new Position(this.x + xOffset, this.y + yOffset);
    }

    /**
     * Returns {@code true} if this {@code Position} is within the rectangle
     * with lower left corner {@code <0, 0>} and upper right corner
     * {@code <width, height>}. Otherwise {@code false} will be returned.
     * @param width Rightmost value.
     * @param height Topmost value.
     * @return {@code true} if this {@code Position} is within the rectangle described above, {@code false} otherwise.
     */
    public boolean isInBounds(final int width, final int height) {
        return 0 <= this.x && this.x < width && 0 <= this.y && this.y < height;
    }

    public boolean isWithinRadius(final Position other, final float radius) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        return (dx * dx + dy * dy) <= radius * radius;
//        return (float) Math.hypot(this.x - other.x, this.y - other.y) <= radius;
    }

    /**
     * @return This {@code Position}'s x-value.
     */
    public float getX() {
        return this.x;
    }

    /**
     * @return This {@code Position}'s y-value.
     */
    public float getY() {
        return this.y;
    }

    /**
     * Two {@code Position}s are considered equal
     * if their x and y values match exactly.
     * @param o Object to compare to.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        final Position position = (Position) o;
        return this.x == position.x && this.y == position.y;
    }

    @Override
    public String toString() {
        return "Position{" + "x=" + this.x + ", y=" + this.y + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    /**
     * Translates this {@code Position} by {@code <deltaX, deltaY>} and
     * returns the result as a new {@code Position}.
     * @param deltaX The x offset.
     * @param deltaY The y offset.
     * @return A new position offset by {@code <deltaX, deltaY>}.
     */
    public Position offset(final float deltaX, final float deltaY) {
        return new Position(this.x + deltaX, this.y + deltaY);
    }
}
