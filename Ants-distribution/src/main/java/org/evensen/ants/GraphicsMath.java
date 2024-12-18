package org.evensen.ants;

public enum GraphicsMath {
    ;
    public static final float TAU = (float) (Math.PI * 2.0);
    public static final float TAU_INV = (float) (1.0 / TAU);
    public static final float TAU16 = (float) (TAU * 16);
    public static final float PI = (float) Math.PI;

    private static final int TAB_EXPONENT = 4;
    private static final int TAB_SIZE = 1 << TAB_EXPONENT;
    private static final int TAB_MASK = TAB_SIZE - 1;
    private static final float[] SIN_TABLE = new float[TAB_SIZE];
    private static final float M_OFFSET = (float) TAU / TAB_SIZE;
    private static final int TAB_COS_OFFSET = 1 << (TAB_EXPONENT - 2);

    static {
        for (int i = 0; TAB_SIZE > i; i++) {
            SIN_TABLE[i] = (float) (Math.sin(i / (double) TAB_SIZE * Math.TAU + Math.PI * 0.5 * 1.0));
        }
    }

    public static float bias(final float x, final float b) {
        return (x / ((((1.0f / b) - 2.0f) * (1.0f - x)) + 1.0f));
    }

    public static float angularDifference(float theta1, float theta2) {
        float minTheta = Math.min(theta1, theta2);
        float maxTheta = Math.max(theta1, theta2);

        return Math.min(Math.abs(maxTheta - minTheta), Math.abs(minTheta + TAU - maxTheta));
    }

    public static float bound(final float min, final float max, final float x) {
        return Math.max(min, Math.min(max, x));
    }
}
