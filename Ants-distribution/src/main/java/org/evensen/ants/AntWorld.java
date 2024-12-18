package org.evensen.ants;

/**
 * A description of a world for ants using two different types of pheromones.
 */
public interface AntWorld {
    /**
     * Gives the width of the world.
     *
     * @return world width.
     */
    int getWidth();

    /**
     * Gives the height of the world.
     *
     * @return world height.
     */
    int getHeight();

    int getFoodSources();

    /**
     * Should return {@code true} for positions where there's an obstacle.
     * Highly advisable: return {@code true} for all positions that are off the map, i.e.
     *
     * @return true if there's an obstacle at {@code p}, {@code false} otherwise.
     * @code x < 0 || y < 0 || x >= width || y >= height} where {@code x = (int) p.getX(); y = (int) p.getY()}
     */
    boolean isObstacle(Position p);

    /**
     * Increases the foraging pheromone level at the cell closest to {@code p}.
     * Closest is in this case defined as "having the indices of {@code <(int) p.getX(), (int) p.getY()>}"
     *
     * @param p      The position where the pheromone should be dropped.
     * @param amount The amount to be dropped. Note that this *increases* rather than sets the current level.
     */
    void dropForagingPheromone(Position p, float amount);

    /**
     * Increases the food pheromone level at the cell closest to {@code p}.
     * Closest is in this case defined as "having the indices of {@code <(int) p.getX(), (int) p.getY()>}"
     *
     * @param p      The position where the pheromone should be dropped.
     * @param amount The amount to be dropped. Note that this *increases* rather than sets the current level.
     */
    void dropFoodPheromone(Position p, float amount);

    /**
     * Notifies the world that food has been dropped.
     * This could be used to handle success if there's more than one stack.
     * If implemented, could also give an indication about the colony's efficiency w.r.t. finding and delivering food.
     *
     * @param p The position to drop food at.
     */
    void dropFood(Position p);

    /**
     * Picks up a food unit closest to {@code p}. Well behaved ants only does this when
     * {@code containsFood(p) == true}
     *
     * @param p The position to pick food from.
     */
    void pickUpFood(Position p);

    /**
     * Possible future extension -- ants should avoid other dead ants.
     */
    float getDeadAntCount(Position p);

    /**
     * Gives the current foraging pheromone level closest to {@code p}.
     * Closest is in this case defined as "having the indices of {@code <(int) p.getX(), (int) p.getY()>}"
     *
     * @param p The position to get the foraging pheromone level for.
     * @return The current foraging pheromone level closest to {@code p}.
     */
    float getForagingStrength(Position p);

    /**
     * Gives the current food pheromone level closest to {@code p}.
     * Closest is in this case defined as "having the indices of {@code <(int) p.getX(), (int) p.getY()>}"
     *
     * @param p The position to get the food pheromone level for.
     * @return The current food pheromone level closest to {@code p}.
     */
    float getFoodStrength(Position p);

    /**
     * @param p The position to check for food.
     * @return {@code true} if the position contains food, {@code false} otherwise.
     */
    boolean containsFood(Position p);

    /**
     * @return home much food has been delivered by way of calling {@code dropFood()}.
     */
    long getFoodCount();

    /**
     * @param p The position to check for homeness.
     * @return {@code true} if the {@code p} could be considered to be within an ant-home, {@code false} otherwise.
     */
    boolean isHome(Position p);

    /**
     * Should let pheromones evaporate as well as spread over the world.
     */
    void dispersePheromones();

    /**
     * Adds/removes an obstacle closest to {@code p}, depending on the parameter {@code add}.
     * @param p The position to add/remove an obstacle to.
     * @param add If {@code true}, adds an obstacle at {@code p}, otherwise removes at {@code p}.
     */
    void setObstacle(Position p, boolean add);

    /**
     * Could be used to handle obstacles that could wear down from ant interaction.
     * @param p The position to hit.
     * @param strength Could be used for anything.
     */
    void hitObstacle(Position p, float strength);
}
