package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.NanoSampler;
import net.openhft.chronicle.core.util.StringUtils;
import net.openhft.chronicle.jlbh.JLBH;
import net.openhft.chronicle.jlbh.JLBHOptions;
import net.openhft.chronicle.jlbh.JLBHTask;

public class AsDoubleImplementationsPerfJLBH implements JLBHTask {

    private final long[] values = new long[]{21738192378192L, 12L, 13781678L, 124372189L};
    private final int[] exponents = new int[]{-50, -10, 0, 10, 50};
    private final boolean[] negative = new boolean[]{true, false};
    private final int[] decimalPlaces = new int[]{0, 10, 20, 50};
    private final int TOTAL_RESULTS = values.length * exponents.length * negative.length * decimalPlaces.length;
    private final double[] unsafeTextAsDoubleResults = new double[TOTAL_RESULTS];
    private final double[] stringUtilsAsDoubleResults = new double[TOTAL_RESULTS];
    private NanoSampler unsafeTextAsDouble;
    private NanoSampler stringUtilsAsDouble;
    private JLBH jlbh;

    public static void main(String[] args) {
        new JLBH(new JLBHOptions()
                .jlbhTask(new AsDoubleImplementationsPerfJLBH())
                .iterations(100_000)
                .warmUpIterations(10_000)
                .runs(3))
                .start();
    }

    @Override
    public void init(JLBH jlbh) {
        unsafeTextAsDouble = jlbh.addProbe("UnsafeText.asDouble");
        stringUtilsAsDouble = jlbh.addProbe("StringUtils.asDouble");
        this.jlbh = jlbh;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void run(long startTimeNS) {
        int counter = 0;
        long startTimeLocal = System.nanoTime();
        for (int v = 0; v < values.length; v++) {
            for (int e = 0; e < exponents.length; e++) {
                for (int n = 0; n < negative.length; n++) {
                    for (int d = 0; d < decimalPlaces.length; d++) {
                        unsafeTextAsDoubleResults[counter++] = Maths.asDouble(values[v], exponents[e], negative[n], decimalPlaces[d]);
                    }
                }
            }
        }

        unsafeTextAsDouble.sampleNanos(System.nanoTime() - startTimeLocal);
        counter = 0;

        // Test perf of StringUtils.asDouble
        startTimeLocal = System.nanoTime();
        for (int v = 0; v < values.length; v++) {
            for (int e = 0; e < exponents.length; e++) {
                for (int n = 0; n < negative.length; n++) {
                    for (int d = 0; d < decimalPlaces.length; d++) {
                        stringUtilsAsDoubleResults[counter++] = StringUtils.asDouble(values[v], exponents[e], negative[n], decimalPlaces[d]);
                    }
                }
            }
        }
        stringUtilsAsDouble.sampleNanos(System.nanoTime() - startTimeLocal);

        for (int i = 0; i < unsafeTextAsDoubleResults.length; i++) {
            if (unsafeTextAsDoubleResults[i] != stringUtilsAsDoubleResults[i]) {
                throw new AssertionError("They give different results");
            }
        }

        jlbh.sampleNanos(System.nanoTime() - startTimeLocal);
    }
}
