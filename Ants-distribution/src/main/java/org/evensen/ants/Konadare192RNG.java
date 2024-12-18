package org.evensen.ants;

import java.util.random.RandomGenerator.SplittableGenerator;
import java.util.stream.Stream;

/**
 * An implementation of a very fast, long period generator,
 * "Konadare192Px++".
 */
public class Konadare192RNG implements SplittableGenerator {
    private static final long KONADARE192_INC = 0xBB67AE8584CAA73BL; /* SQRT3 */
    private static final int KONADARE192_R1 = 20;
    private static final int KONADARE192_R2 = 43;
    private static final long UMASK = (1L << 63) - 1;
    private static final long UMASK_I = (1L << 31) - 1;
    private long a, b, c;
    private boolean hasNextGaussian;
    private double nextGaussian;

    public Konadare192RNG(final long seed) {
        final long[] eSeed = {seed, seed + 1, seed + 2};
        mix(eSeed);
        this.a = eSeed[0];
        this.b = eSeed[1];
        this.c = eSeed[2];
    }

    private Konadare192RNG(final long a, final long b, final long c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    private static void mix(final long[] arr) {
        long acc = arr[arr.length - 1];
        for (int i = 1; i <= 3; i++) {
            for (int j = 0; j < arr.length; j++) {
                acc += arr[j] + (long) i * arr.length + j;
                acc *= KONADARE192_INC;
                acc ^= acc >>> 14 ^ acc >>> 34;
                arr[j] = acc;
            }
        }
    }

    public long nextLong() {
        final long out = this.b ^ this.c;
        final long a0 = this.a ^ (this.a >>> 32);
        this.a += KONADARE192_INC;
        this.b = Long.rotateRight(this.b + a0, KONADARE192_R1);
        this.c = Long.rotateRight(this.c + this.b, KONADARE192_R2);

        return out;
    }

    @Override
    public SplittableGenerator split() {
        final long[] eSeed = {this.a, this.b, this.c};
        mix(eSeed);
        this.nextLong();
        return new Konadare192RNG(eSeed[0], eSeed[1], eSeed[2]);
    }

    @Override
    public SplittableGenerator split(final SplittableGenerator source) {
        return split();
    }

    @Override
    public Stream<SplittableGenerator> splits(final long streamSize) {
        return null;
    }

    @Override
    public Stream<SplittableGenerator> splits(final SplittableGenerator source) {
        return null;
    }

    @Override
    public Stream<SplittableGenerator> splits(final long streamSize, final SplittableGenerator source) {
        return null;
    }


    // Adaptation of Daniel Lemire's "Fast Random Integer Generation in an Interval" by Pelle Evensen
    /**
     * Gives an {@code long} variate expected to be uniform on {@code [0, bound)}.
     * @param bound the upper bound (exclusive) for the returned value. Must be positive.
     *
     * @return a pseudorandom {@code long} on {@code [0, bound)}.
     */
    @Override
    public long nextLong(final long bound) {
        if (bound < 1) {
            throw new IllegalArgumentException("s must be strictly positive (was " + bound + ")");
        }
        if (bound > (UMASK >>> 1)) {
            long x;
            do {
                x = nextLong();
            } while (x >= bound);
            return x;
        }
        // Only use the 63 LSB of the word due to
        // Java's lack of unsigned types.
        long x = nextLong() & UMASK;

        // s multiplied by two since x is divided by two.
        long mHi = Math.multiplyHigh(x, bound << 1);
        long mLo = x * bound;
        if (0 > Long.compareUnsigned(mLo, bound)) {
            // final long t = -s % s;
            final long t = Long.remainderUnsigned(-bound, bound);
            while (0 > Long.compareUnsigned(mLo, t)) {
                x = nextLong() & UMASK;
                mHi = Math.multiplyHigh(x, bound << 1);
                mLo = x * bound;
            }
        }
        return mHi;
    }

    /**
     * Gives an {@code int} variate expected to be uniform on {@code [0, bound)}.
     * @param bound the upper bound (exclusive) for the returned value. Must be positive.
     *
     * @return a pseudorandom {@code int} on {@code [0, bound)}.
     */
    @Override
    public int nextInt(final int bound) {
        if (bound < 0) {
            throw new IllegalArgumentException("s must be >= 0 and < 2^31 (was " + bound + ")");
        }
        int x = (int) (nextLong() & UMASK_I);
        long m = x * ((long) bound << 1);
        int l = x * bound;
        if (0 > Integer.compareUnsigned(l, bound)) {
            final int t = Integer.remainderUnsigned(-bound, bound);
            while (0 > Integer.compareUnsigned(l, t)) {
                x = (int) (nextLong() & UMASK_I);
                m = x * ((long) bound << 1);
                l = x * bound;
            }
        }
        return (int) (m >>> 32);
    }

    /**
     * Reasonably good approximation of a standard normal random variate.
     * Do not use for critical simulations -- both for performance and statistical reasons.
     * @return A pseudo random {@code double} (quite) closely following the distribution of N(0, 1).
     */
    @Override
    public double nextGaussian() {
        final int terms = 5;
        double acc = 0.0;
        for (int i = 0; i < terms; i++) {
            acc += nextDouble();
        }
        return (acc - (terms * 0.5)) * (12.0 / terms);
    }

}
