/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import java.util.Arrays;
import java.util.function.DoubleFunction;

/**
 * Created by peter on 10/07/15.
 */
// TODO add a dummy histogram.
public class Histogram implements NanoSampler {
    private int fractionBits;
    private int powersOf2;
    private long overRange;
    private long totalCount;
    private long floor;
    private int[] sampleCount;

    public Histogram() {
        this(42, 4);
    }

    public Histogram(int powersOf2, int fractionBits) {
        this(powersOf2, fractionBits, 1.0);
    }

    public Histogram(int powersOf2, int fractionBits, double minValue) {
        this.powersOf2 = powersOf2;
        this.fractionBits = fractionBits;
        sampleCount = new int[powersOf2 << fractionBits];
        floor = Double.doubleToRawLongBits(minValue) >> (52 - fractionBits);
    }

    /**
     * @return Histogram for use with System.nanoTime() up to 4 second delay.
     */
    public static Histogram timeMicros() {
        return new Histogram(22 /* 4 seconds */, 3 /* 2 decimal places */, 1000.0 /* nano-seconds */);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Histogram))
            return false;
        Histogram h = (Histogram) obj;
        if (!(powersOf2 == h.powersOf2
                && fractionBits == h.fractionBits
                && floor == h.floor))
            return false;
        int size = powersOf2 << fractionBits;
        for (int i = 0; i < size; i++) {
            if (sampleCount[i] != h.sampleCount[i])
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Histogram{" +
                "fractionBits=" + fractionBits +
                ", powersOf2=" + powersOf2 +
                ", overRange=" + overRange +
                ", totalCount=" + totalCount +
                ", floor=" + floor +
                ", sampleCount=" + Arrays.toString(sampleCount) +
                '}';
    }

    /**
     * Re initialise this histogram from deserialized data
     */
    public void init(int powersOf2, int fractionBits, long overRange, long totalCount, long floor) {
        this.powersOf2 = powersOf2;
        this.fractionBits = fractionBits;
        this.overRange = overRange;
        this.totalCount = totalCount;
        this.floor = floor;

        int minSampleCountLength = powersOf2 << fractionBits;
        if (sampleCount.length < minSampleCountLength)
            sampleCount = new int[minSampleCountLength];
    }

    public int fractionBits() {
        return fractionBits;
    }

    public int powersOf2() {
        return powersOf2;
    }

    public long overRange() {
        return overRange;
    }

    public int[] sampleCount() {
        return sampleCount;
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

    public double[] getPercentiles() {
        if (totalCount < 1_000_000) {
            return new double[]{
                    percentile(0.5),
                    percentile(0.9),
                    percentile(0.99),
                    percentile(0.999),
                    percentile(0.9999),
                    percentile(1)
            };
        }

        if (totalCount < 10_000_000) {
            return new double[]{
                    percentile(0.5),
                    percentile(0.9),
                    percentile(0.99),
                    percentile(0.999),
                    percentile(0.9999),
                    percentile(0.99999),
                    percentile(1)
            };
        }

        return new double[]{
                percentile(0.5),
                percentile(0.9),
                percentile(0.99),
                percentile(0.999),
                percentile(0.9999),
                percentile(0.99999),
                percentile(0.999999),
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
            return "50/90 97/99 99.7/99.9 99.97/99.99 - worst was " +
                    p(toMicros.apply(percentile(0.5))) + " / " +
                    p(toMicros.apply(percentile(0.9))) + "  " +
                    p(toMicros.apply(percentile(0.97))) + " / " +
                    p(toMicros.apply(percentile(0.99))) + "  " +
                    p(toMicros.apply(percentile(0.997))) + " / " +
                    p(toMicros.apply(percentile(0.999))) + "  " +
                    p(toMicros.apply(percentile(0.9997))) + " / " +
                    p(toMicros.apply(percentile(0.9999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        if (totalCount < 10_000_000)
            return "50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 - worst was " +
                    p(toMicros.apply(percentile(0.5))) + " / " +
                    p(toMicros.apply(percentile(0.9))) + "  " +
                    p(toMicros.apply(percentile(0.97))) + " / " +
                    p(toMicros.apply(percentile(0.99))) + "  " +
                    p(toMicros.apply(percentile(0.997))) + " / " +
                    p(toMicros.apply(percentile(0.999))) + "  " +
                    p(toMicros.apply(percentile(0.9997))) + " / " +
                    p(toMicros.apply(percentile(0.9999))) + "  " +
                    p(toMicros.apply(percentile(0.99997))) + " / " +
                    p(toMicros.apply(percentile(0.99999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        return "50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was " +
                p(toMicros.apply(percentile(0.5))) + " / " +
                p(toMicros.apply(percentile(0.9))) + "  " +
                p(toMicros.apply(percentile(0.97))) + " / " +
                p(toMicros.apply(percentile(0.99))) + "  " +
                p(toMicros.apply(percentile(0.997))) + " / " +
                p(toMicros.apply(percentile(0.999))) + "  " +
                p(toMicros.apply(percentile(0.9997))) + " / " +
                p(toMicros.apply(percentile(0.9999))) + "  " +
                p(toMicros.apply(percentile(0.99997))) + " / " +
                p(toMicros.apply(percentile(0.99999))) + "  " +
                p(toMicros.apply(percentile(0.999997))) + " / " +
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

    public long floor() {
        return floor;
    }

    public void reset() {
        totalCount = overRange = 0;

        Arrays.fill(sampleCount, 0);
    }

    @Override
    public void sampleNanos(long nanos) {
        sample(nanos);
    }
}
