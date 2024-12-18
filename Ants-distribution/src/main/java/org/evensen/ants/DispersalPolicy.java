package org.evensen.ants;

public interface DispersalPolicy {
    /**
     * Returns the new pheromone levels for position {@code p} in {@code w} as an array of floats.
     *
     * @param w The world to extract pheromone levels from.
     * @param p The position to get levels from.
     * @return A new array with suggested new pheromone levels.
     */
    float[] getDispersedValue(AntWorld w, Position p);
}
