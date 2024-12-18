package org.evensen.ants;

public enum FractalNoise {
    ;

    private static double hash(final double x0, final double y0, final long seed) {
        long x = (long) x0;
        final long y = (long) y0;
        for (int i = 1; 5 > i; i++) {
            x += i ^ seed;
            x *= 1111111111111111111L;
            x += y;
            x ^= x >>> 29;
        }
        return (x & ((1L << 53) - 1)) / (double) (1L << 53);
    }

    private static double interpolate(final double a0, final double a1, final double w) {
        return (a1 - a0) * ((w * (w * 6.0 - 15.0) + 10.0) * w * w * w) + a0;
    }

    /* Create pseudorandom direction vector
     */
    private static Vector2 randomGradient(final long ix, final long iy, final long seed) {
        final double random = hash(ix, iy, seed) * GraphicsMath.TAU;
        return new Vector2(Math.cos(random), Math.sin(random));
    }

    private static double dotGridGradient(final long ix, final long iy, final double x, final double y, final long seed) {
        final Vector2 gradient = randomGradient(ix, iy, seed);

        // Compute the distance vector
        final double dx = x - ix;
        final double dy = y - iy;

        // Compute the dot-product
        return dx * gradient.x + dy * gradient.y;
    }

    private static double perlin(final double x, final double y, final long seed) {
        final long x0 = (long) x;
        final long x1 = x0 + 1;
        final long y0 = (long) y;
        final long y1 = y0 + 1;

        final double sx = x - x0;
        final double sy = y - y0;

        final double n00 = dotGridGradient(x0, y0, x, y, seed);
        final double n01 = dotGridGradient(x1, y0, x, y, seed);
        final double ix0 = interpolate(n00, n01, sx);

        final double n10 = dotGridGradient(x0, y1, x, y, seed);
        final double n11 = dotGridGradient(x1, y1, x, y, seed);
        final double ix1 = interpolate(n10, n11, sx);

        return interpolate(ix0, ix1, sy) * 0.5 + 0.5;
    }

    /**
     * Generates some fractal noise with the properties that the same parameters alway will generate the same values.
     * @param x The x-value of the position to get noise for.
     * @param y The y-value of the position to get noise for.
     * @param persistence How rapidly the amplitude should fall off for each octave.
     * @param firstOctave The first octave -- set to 1 for large variations.
     * @param lastOctave The last octave -- set to 10 for a lot of small details (in combination with persistence >= 0.6)
     * @param seed Gives rise to a unique noise map.
     * @return The noise value for the above parameters.
     */
    public static double getNoise(final double x, final double y,
                                  final double persistence, final int firstOctave, final int lastOctave, final long seed) {
        double acc = 0;
        double ampSum = 0.0;
        double ampl = persistence;
        for (int i = firstOctave; i <= lastOctave; i++) {
            acc += perlin(x * (1 << i), y * (1 << i), seed) * ampl;
            ampSum += ampl;
            ampl *= persistence;
        }
        return acc / ampSum;
    }

    private static class Vector2 {
        final double x;
        final double y;

        Vector2(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
