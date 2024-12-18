package org.evensen.ants;

public enum Hasher {
    ;
    private static final long SQRT3 = 0xBB67AE8584CAA73BL;

    public static long hash(final Object... objs) {
        long h = 1;
        for (final Object o : objs) {
            h += o.hashCode();
            h *= SQRT3;
            h ^= h >>> 28;
        }
        for (int i = 0; i < 3; i++) {
            h *= SQRT3;
            h ^= h >>> 28;
        }
        return h;
    }
}
