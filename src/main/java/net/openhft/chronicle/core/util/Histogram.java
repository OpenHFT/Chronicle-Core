/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core.util;

import java.util.function.DoubleFunction;

/**
 * Created by peter on 10/07/15.
 */
// TODO add a dummy histogram.
public class Histogram implements NanoSampler {
    private final int fractionBits;
    private int powersOf2;
    private long totalCount, overRange;
    private int[] sampleCount;
    private long floor;

    public Histogram() {
        this(42, 4);
    }

    public Histogram(int powersOf2, int fractionBits) {
        this.powersOf2 = powersOf2;
        this.fractionBits = fractionBits;
        sampleCount = new int[powersOf2 << fractionBits];
        floor = Double.doubleToRawLongBits(1) >> (52 - fractionBits);
    }

    public void add(Histogram h) {
        assert powersOf2 == h.powersOf2;
        assert fractionBits == h.fractionBits;
        totalCount += h.totalCount;
        overRange += h.overRange;
        for (int i = 0; i < sampleCount.length; i++)
            sampleCount[i] += h.sampleCount[i];
    }

    public int sample(double time) {
        int bucket = (int) ((Double.doubleToRawLongBits(time) >> (52 - fractionBits)) - floor);
        if (bucket >= sampleCount.length)
            overRange++;
        else if (bucket >= 0)
            sampleCount[bucket]++;
        totalCount++;
        return bucket;
    }

    public double percentile(double fraction) {
        long value = (long) (totalCount * (1 - fraction));
        value -= overRange;
        if (value < 0)
            return Double.POSITIVE_INFINITY;
        for (int i = sampleCount.length - 1; i >= 0; i--) {
            value -= sampleCount[i];
            if (value < 0) {
                long bits = ((((i + floor) << 1) + 1) << (51 - fractionBits));
                return Double.longBitsToDouble(bits);
            }
        }
        return 1;
    }

    public double[] getPercentiles(){
        return new double[]{
            percentile(0.5),
            percentile(0.9),
            percentile(0.99),
            percentile(0.999),
            percentile(0.9999),
            percentile(1)
        };
    }

    public String toMicrosFormat() {
        return toMicrosFormat(t -> t / 1e3);
    }

    public String toMicrosFormat(DoubleFunction<Double> toMicros) {
        if (totalCount < 1_000_000)
            return "50/90 99/99.9 99.99 - worst was " +
                    p(toMicros.apply(percentile(0.5))) + " / " +
                    p(toMicros.apply(percentile(0.9))) + "  " +
                    p(toMicros.apply(percentile(0.99))) + " / " +
                    p(toMicros.apply(percentile(0.999))) + "  " +
                    p(toMicros.apply(percentile(0.9999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        if (totalCount < 10_000_000)
            return "50/90 99/99.9 99.99/99.999 - worst was " +
                    p(toMicros.apply(percentile(0.5))) + " / " +
                    p(toMicros.apply(percentile(0.9))) + "  " +
                    p(toMicros.apply(percentile(0.99))) + " / " +
                    p(toMicros.apply(percentile(0.999))) + "  " +
                    p(toMicros.apply(percentile(0.9999))) + " / " +
                    p(toMicros.apply(percentile(0.99999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        return "50/90 99/99.9 99.99/99.999 99.9999/worst was " +
                p(toMicros.apply(percentile(0.5))) + " / " +
                p(toMicros.apply(percentile(0.9))) + "  " +
                p(toMicros.apply(percentile(0.99))) + " / " +
                p(toMicros.apply(percentile(0.999))) + "  " +
                p(toMicros.apply(percentile(0.9999))) + " / " +
                p(toMicros.apply(percentile(0.99999))) + "  " +
                p(toMicros.apply(percentile(0.999999))) + " / " +
                p(toMicros.apply(percentile(1)));
    }

    public String toLongMicrosFormat(DoubleFunction<Double> toMicros) {
        if (totalCount < 1_000_000)
            return "50/90 93/99 99.3/99.9 99.93/99.99 - worst was " +
                    p(toMicros.apply(percentile(0.5))) + " / " +
                    p(toMicros.apply(percentile(0.9))) + "  " +
                    p(toMicros.apply(percentile(0.93))) + " / " +
                    p(toMicros.apply(percentile(0.99))) + "  " +
                    p(toMicros.apply(percentile(0.993))) + " / " +
                    p(toMicros.apply(percentile(0.999))) + "  " +
                    p(toMicros.apply(percentile(0.9993))) + " / " +
                    p(toMicros.apply(percentile(0.9999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        if (totalCount < 10_000_000)
            return "50/90 93/99 99.3/99.9 99.93/99.99 99.993/99.999 - worst was " +
                    p(toMicros.apply(percentile(0.5))) + " / " +
                    p(toMicros.apply(percentile(0.9))) + "  " +
                    p(toMicros.apply(percentile(0.93))) + " / " +
                    p(toMicros.apply(percentile(0.99))) + "  " +
                    p(toMicros.apply(percentile(0.993))) + " / " +
                    p(toMicros.apply(percentile(0.999))) + "  " +
                    p(toMicros.apply(percentile(0.9993))) + " / " +
                    p(toMicros.apply(percentile(0.9999))) + "  " +
                    p(toMicros.apply(percentile(0.99993))) + " / " +
                    p(toMicros.apply(percentile(0.99999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        return "50/90 93/99 99.3/99.9 99.93/99.99 99.993/99.999 99.9993/99.9999 - worst was " +
                p(toMicros.apply(percentile(0.5))) + " / " +
                p(toMicros.apply(percentile(0.9))) + "  " +
                p(toMicros.apply(percentile(0.93))) + " / " +
                p(toMicros.apply(percentile(0.99))) + "  " +
                p(toMicros.apply(percentile(0.993))) + " / " +
                p(toMicros.apply(percentile(0.999))) + "  " +
                p(toMicros.apply(percentile(0.9993))) + " / " +
                p(toMicros.apply(percentile(0.9999))) + "  " +
                p(toMicros.apply(percentile(0.99993))) + " / " +
                p(toMicros.apply(percentile(0.99999))) + "  " +
                p(toMicros.apply(percentile(0.999993))) + " / " +
                p(toMicros.apply(percentile(0.999999))) + " - " +
                p(toMicros.apply(percentile(1)));
    }

    private String p(double v) {
        return v < 0.1 ? String.format("%.3f", v) :
                v < 1 ? String.format("%.2f", v) :
                        v < 10 ? String.format("%.1f", v) :
                                v < 1000 ? Long.toString(Math.round(v)) :
                                        String.format("%,d", Math.round(v / 10) * 10);
    }

    public long totalCount() {
        return totalCount;
    }

    public void reset(){
        sampleCount = new int[powersOf2 << fractionBits];
        totalCount = overRange = 0;
    }

    @Override
    public void sampleNanos(long nanos) {
        sample(nanos);
    }
}
