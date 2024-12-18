package org.evensen.ants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class AntColony {
    private final List<Ant> ants;
    private long accTime;
    private int epochs;
    private final Supplier<PellAnt> createAnt;

    public AntColony(final int ants, final float pheromoneRate, final AntWorld w) {
        this.ants = new ArrayList<>();
        final SplittableRandom rng = new SplittableRandom(Hasher.hash(1));
        this.createAnt = new Supplier<PellAnt>() {
            @Override
            public PellAnt get() {
                return new PellAnt(new Position(
                        w.getWidth() - 5, (float) (w.getHeight() / 2 + rng.nextGaussian())),
                        pheromoneRate);
            }
        };
        for (int i = 0; i < ants; i++) {
            this.ants.add(this.createAnt.get());
        }
    }

    public void updateAnts(final AntWorld w) {
        final long startTime = System.nanoTime();
        synchronized (this) {
            this.ants.sort((o1, o2) -> {
                final float x1 = o1.getPosition().getY();
                final float x2 = o2.getPosition().getY();
                if (x1 < x2) {
                    return -1;
                } else if (x1 > x2) {
                    return 1;
                } else {
                    return 0;
                }
            });
            this.ants.forEach(a -> a.move(w));
        }
        this.accTime += System.nanoTime() - startTime;
        this.epochs++;
        if (0 == this.epochs % 100) {
            System.out.println("Time per epoch: " + (this.accTime / (double) this.epochs / 1_000_000_000) + " s");
        }
    }

    public List<Ant> getAnts() {
        return Collections.unmodifiableList(this.ants);
    }
}
