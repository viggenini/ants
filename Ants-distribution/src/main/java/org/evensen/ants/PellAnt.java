package org.evensen.ants;

import java.util.BitSet;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.random.RandomGenerator.SplittableGenerator;

import static org.evensen.ants.GraphicsMath.TAU;

public class PellAnt implements Ant {
    private static final float PI = (float) Math.PI;
    private static final float SCAN_ANGLE = TAU / 2.5f;
    private static final float SCAN_INCREMENT = (float) (SCAN_ANGLE / 19.0f);
    private static final float NO_FOOD_WEIGHT = -4.0f;
    private static final float SCAN_RADIUS = 30.0f;
    private static final float MIN_SCAN_RADIUS = 1.0f;
    private static final float RADIUS_INCREMENT = 5.0f;
    private static final float MOVE_RATE = 1.74f;
    private static final float CARRYING_MOVE_SCALE = 0.5f;
    private static final float PHEROMONE_DROP_RATE = 0.99f;
    private static final float TURN_RATE = 0.9f;
    private static final float SCENT_DEVIATION = 0.01f;
    private static final float[] RADII_WEIGHTS;
    private static final int DEFAULT_HIT_POINTS = 10;
    private static final float PHEROMONE_STRENGTH = 0.001f;

    static {
        RADII_WEIGHTS = new float[(int) ((SCAN_RADIUS - MIN_SCAN_RADIUS) / RADIUS_INCREMENT + 1)];
        int i = 0;
        for (float radius = MIN_SCAN_RADIUS; SCAN_RADIUS > radius; radius += RADIUS_INCREMENT) {
            RADII_WEIGHTS[i] = radius * radius;
            i++;
        }
    }

    private static final class BehaviourState {
        public final Function<AntWorld, Float> goalAngleScan;
        public final Function<AntWorld, Float> typeAngleScan;
        public final Consumer<AntWorld> dropPheromone;
        public final BiFunction<AntWorld, Boolean, Float> getPheromoneDirection;
        public final Consumer<AntWorld> goalStrategy;
        public Action currentGoal;
        public boolean carriesFood;

        public BehaviourState(final Function<AntWorld, Float> goalAngleScan,
                              final Function<AntWorld, Float> typeAngleScan,
                              final Consumer<AntWorld> dropPheromone,
                              final BiFunction<AntWorld, Boolean, Float> getPheromoneDirection,
                              final Consumer<AntWorld> goalStrategy) {
            this.goalAngleScan = goalAngleScan;
            this.typeAngleScan = typeAngleScan;
            this.dropPheromone = dropPheromone;
            this.getPheromoneDirection = getPheromoneDirection;
            this.goalStrategy = goalStrategy;
            this.currentGoal = Action.FORAGING;
            this.carriesFood = false;
        }
    }

    private final BehaviourState getCarryingState() {
        return new BehaviourState(
                (x -> scanForTypeAngle(x, x::isHome)),
                (x -> scanForTypeAngle(x, x::containsFood)),
                (x -> x.dropFoodPheromone(this.position,
                        this.pheromonesLeft * PHEROMONE_STRENGTH)),
                ((x, p) -> scanForScentAngle(x, p ?
                        x::getForagingStrength : x::getFoodStrength, null, 1).direction),
                (x -> {
                    if (this.carriesFood && x.isHome(this.position)) {
                        x.dropFood(this.position);
                        this.direction += PI + this.rng.nextFloat() * 0.1 - 0.05;
                        this.carriesFood = false;
                        this.pheromonesLeft = 1.0f;
                        this.currentState = this.foragingState;
                    }
                }));
    }

    private final BehaviourState getForagingState() {
        return new BehaviourState(
                (x -> scanForTypeAngle(x, x::containsFood)),
                (x -> scanForTypeAngle(x, x::isHome)),
                (x -> x.dropForagingPheromone(this.position,
                        this.pheromonesLeft * PHEROMONE_STRENGTH)),
                ((x, p) -> scanForScentAngle(x, x::getFoodStrength, x::getForagingStrength,
                        NO_FOOD_WEIGHT).direction),
                (x -> {
                    if (x.containsFood(this.position)) {
                        x.pickUpFood(this.position);
                        this.direction += PI + this.rng.nextFloat() * 0.1 - 0.05;
                        this.carriesFood = true;
                        this.pheromonesLeft = 1.0f;
                        this.currentState = this.carryingState;
                    }
                }));
    }

    private final BehaviourState carryingState;
    private final BehaviourState foragingState;
    private BehaviourState currentState;

    private final float pheromoneRate;
    private final SplittableGenerator rng;
    private final float moveRate;
    private final float carryingMoveRate;
    private final Action currentGoal;
    private Position position;
    private boolean carriesFood;
    private float pheromonesLeft;
    private float direction;

    private int hitPoints;

    public PellAnt(final Position startingPosition, final float pheromoneRate) {
        this.position = startingPosition;
        this.rng = new Konadare192RNG(startingPosition.hashCode() * 9999L + Float.hashCode(pheromoneRate));
        this.pheromoneRate = pheromoneRate + this.rng.nextFloat() * pheromoneRate * 0.1f;
        this.direction = this.rng.nextFloat() * TAU;
        this.moveRate = MOVE_RATE + this.rng.nextFloat() * MOVE_RATE * 0.1f;
        this.carryingMoveRate = this.moveRate * CARRYING_MOVE_SCALE;
        this.currentGoal = Action.FORAGING;
        this.hitPoints = DEFAULT_HIT_POINTS;
        this.carriesFood = false;
        this.carryingState = getCarryingState();
        this.foragingState = getForagingState();
        this.currentState = this.foragingState;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        final PellAnt ant = (PellAnt) o;
        return 0 == Float.compare(ant.pheromoneRate, this.pheromoneRate) &&
                0 == Float.compare(ant.direction, this.direction) && Objects.equals(this.position, ant.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pheromoneRate, this.position, this.direction);
    }

    private Scent scanForScentAngle(final AntWorld w, final Function<Position, Float> primaryScentFunction,
                                    Function<Position, Float> secondaryScentFunction,
                                    final float noFoodWeight) {
        if (null == secondaryScentFunction) {
            secondaryScentFunction = p -> w.isObstacle(p) ? 0.1f : 0f;
        }
        final float strongestScent = 0;
        final float strongestAngle = this.direction;
        final SortedSet<Scent> bestScents = new TreeSet<>();

        for (float i = -SCAN_ANGLE / 2.0f; SCAN_ANGLE / 2.0 >= i; i += SCAN_INCREMENT) {
            final float theta = this.direction + i;
            float scentAcc = 0.0f;
            float negativeScentAcc = 0.0f;
            final float xOff = (float) Math.cos(theta) * this.moveRate;
            final float yOff = (float) Math.sin(theta) * this.moveRate;
            int radiusIndex = 0;
            for (float radius = MIN_SCAN_RADIUS; SCAN_RADIUS > radius; radius += RADIUS_INCREMENT) {
                final Position offset = this.position.offset(xOff * radius, yOff * radius);
                if (w.isObstacle(offset)) {
                    scentAcc -= RADII_WEIGHTS[radiusIndex];
                    break;
                }
                scentAcc += primaryScentFunction.apply(offset) * RADII_WEIGHTS[radiusIndex];
                negativeScentAcc += secondaryScentFunction.apply(offset) * RADII_WEIGHTS[radiusIndex];
                radiusIndex++;
            }
            scentAcc -= negativeScentAcc;
            scentAcc *= ((float) this.rng.nextGaussian() * SCENT_DEVIATION + 2.0f);
            if (scentAcc > 0) {
                bestScents.add(new Scent(theta, scentAcc));
            }
        }

        for (final Scent s : bestScents) {
            if (isPathClear(w, s.direction, 2.0f, this.moveRate * SCAN_RADIUS)) {
                return s;
            }
        }

        return new Scent(this.direction, 0);
    }

    private boolean isPathClear(final AntWorld w, final float direction, final float baseRadius,
                                final float maxRadius) {
        final float xOff = (float) Math.cos(direction);
        final float yOff = (float) Math.sin(direction);
        for (float radius = baseRadius; radius <= maxRadius; radius++) {
            if (w.isObstacle(this.position.offset(xOff * radius, yOff * radius))) {
                return false;
            }
        }
        return true;
    }

    private float scanForTypeAngle(final AntWorld w, final Function<Position, Boolean> typeMapping) {
        float typeDistance = 1.0E10f;
        float strongestAngle = this.direction;
        for (float i = -SCAN_ANGLE / 2.0f; SCAN_ANGLE / 2.0f >= i; i += SCAN_INCREMENT) {
            final float theta = this.direction + i;
            final float xOff = (float) Math.cos(theta) * this.moveRate;
            final float yOff = (float) Math.sin(theta) * this.moveRate;
            for (float radius = MIN_SCAN_RADIUS;
                 SCAN_RADIUS > radius && radius < typeDistance; radius += RADIUS_INCREMENT) {
                final Position offset = this.position.offset(xOff * radius, yOff * radius);
                if (w.isObstacle(offset)) {
                    break;
                }
                if (typeMapping.apply(offset)) {
                    if (isPathClear(w, theta, MIN_SCAN_RADIUS, radius * this.moveRate)) {
                        strongestAngle = (theta + TAU) % TAU;
                        typeDistance = radius;
                    }
                    break;
                }
            }
        }

        return 1.0E5 > typeDistance ? strongestAngle : -1;
    }

    private void replenishPheromones(AntWorld w) {
        if (w.isHome(this.position) || w.containsFood(this.position)) {
            this.pheromonesLeft = 1.0f;
        }
    }

    private void tryMove(final AntWorld w, final Position newPosition, float newDirection,
                         final Consumer<AntWorld> goalStrategy) {
        if (!w.isObstacle(newPosition)) {
            this.position = newPosition;
            float angularDiff = GraphicsMath.angularDifference(newDirection, this.direction) * GraphicsMath.TAU_INV;
            this.pheromonesLeft *= 1.0f - angularDiff * 0.1f;
            this.direction = newDirection;
            goalStrategy.accept(w);
        } else {
            final boolean reachedHorizontalBorder = newPosition.getX() <= 0 || w.getWidth() <= newPosition.getX() - 1;
            final boolean reachedVerticalBorder = newPosition.getY() <= 0 || w.getHeight() <= newPosition.getY() - 1;
            if (reachedVerticalBorder || reachedHorizontalBorder) {
                if (reachedHorizontalBorder && reachedVerticalBorder) {
                    this.direction = this.direction + PI;
                } else if (reachedHorizontalBorder) {
                    this.direction = PI - this.direction;
                } else if (reachedVerticalBorder) {
                    this.direction = TAU - this.direction;
                }
            } else {
                this.direction = this.direction + (float) (this.rng.nextGaussian() * 0.5);
            }
            w.hitObstacle(newPosition, 1.0f);
        }
    }

    public void move(final AntWorld w) {
        if (isDead()) {
            return;
        }
        replenishPheromones(w);
        final boolean hasPheromones = 1.0E-4f < this.pheromonesLeft;

        float bestDirection = this.currentState.goalAngleScan.apply(w);
        if (hasPheromones) {
            if (this.rng.nextFloat() < this.pheromoneRate) {
                this.currentState.dropPheromone.accept(w);
                this.pheromonesLeft *= PHEROMONE_DROP_RATE;
            }
        } else {
            if (bestDirection < 0) {
                bestDirection = this.currentState.typeAngleScan.apply(w);
            }
        }
        if (bestDirection < 0) {
            bestDirection = this.currentState.getPheromoneDirection.apply(w, hasPheromones);
        }
        if (bestDirection < 0) {
            bestDirection = (float) (this.direction + this.rng.nextGaussian() * 0.01);
        }

        final float newDirection = bestDirection;
        final Position newPosition = this.position.move(this.carriesFood ? this.carryingMoveRate : this.moveRate,
                newDirection);
        tryMove(w, newPosition, newDirection, this.currentState.goalStrategy);
    }


    public float getDirection() {
        return this.direction;
    }

    public Position getPosition() {
        return this.position;
    }

    public boolean hasFood() {
        return this.carriesFood;
    }

    private enum Action {
        FORAGING,
        CARRYING
    }

    private static class Scent implements Comparable<Scent> {
        private final float direction;
        private final float strength;

        private Scent(float direction, float strength) {
            this.direction = direction;
            this.strength = strength;
        }

        @Override
        public int compareTo(final Scent o) {
            return this.strength < o.strength ? 1 : -1;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (null == obj || this.getClass() != obj.getClass()) {
                return false;
            }
            final Scent o = (Scent) obj;
            return o.direction == this.direction;
        }

        @Override
        public int hashCode() {
            return Float.floatToRawIntBits(this.direction);
        }
    }

    public void damage(int strength) {
        this.hitPoints -= strength;
    }

    public boolean isDead() {
        return this.hitPoints <= 0;
    }
}