package org.evensen.ants;

public class FoodSource {
    private final Position position;
    private final int radius;
    private int foodAmount;

    public FoodSource(Position position, int radius, int foodAmount) {
        this.position = position;
        this.radius = radius;
        this.foodAmount = foodAmount;
    }

    public boolean containsFood(Position p) {
        return this.foodAmount > 0 && this.position.isWithinRadius(p, this.radius);
    }

    public boolean takeFood() {
        if (this.foodAmount > 0) {
            this.foodAmount--;
            return this.foodAmount > 0;
        }
        return false;
    }

    public Position getPosition() {
        return this.position;
    }

    public int getFoodAmount() {
        return this.foodAmount;
    }

    public int getRadius() {
        return this.radius;
    }
}