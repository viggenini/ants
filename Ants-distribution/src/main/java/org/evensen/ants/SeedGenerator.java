package org.evensen.ants;

public enum SeedGenerator {
    ;
    private static final long globalSeed = 3;
    static Konadare192RNG seedRNG = new Konadare192RNG(globalSeed);

    public static long nextSeed() {
        return seedRNG.nextLong();
    }
}
