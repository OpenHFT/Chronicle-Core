/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.DoubleStream;

// TODO add a dummy histogram.
public class Histogram implements NanoSampler {
    static final DecimalFormat F3 = new DecimalFormat("0.000");
    static final DecimalFormat F2 = new DecimalFormat("0.00");
    static final DecimalFormat F1 = new DecimalFormat("0.0");
    private int fractionBits;
    private int powersOf2;
    private long overRange;
    private long totalCount;
    private long floor;
    private int[] sampleCount;

    public Histogram() {
        this(42, 7);
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
    @NotNull
    public static Histogram timeMicros() {
        return new Histogram(22 /* 4 seconds */, 3 /* 2 decimal places */, 1000.0 /* nano-seconds */);
    }

    public static double[] percentilesFor(long count) {
        List<Double> values = new ArrayList<>();
        values.add(50 / 100.0);
        values.add(90 / 100.0);
        values.add(99 / 100.0);
        if (count > 10_000) {
            values.add(99.7 / 100.0);
            if (count > 100_000) {
                values.add(99.9 / 100.0);
                if (count > 1_000_000) {
                    values.add(99.97 / 100.0);
                    if (count > 2_000_000) {
                        values.add(99.99 / 100.0);
                    }
                }
            }
        }
        values.add(100 / 100.0);
        return values.stream().mapToDouble(d -> d).toArray();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Histogram))
            return false;
        @NotNull Histogram h = (Histogram) obj;
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

    @NotNull
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

    public void add(@NotNull Histogram h) {
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

    public double min() {
        return percentile(0.0);
    }

    public double typical() {
        return percentile(0.5);
    }

    public double max() {
        return percentile(1.0);
    }

    public double percentile(double fraction) {
        if (fraction <= 0) {
            for (int i = 0; i < sampleCount.length; i++) {
                if (sampleCount[i] <= 0)
                    continue;
                long bits = ((((i + floor) << 1) + 1) << (51 - fractionBits));
                return Double.longBitsToDouble(bits);
            }
            return 1;
        }
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

    @NotNull
    public double[] getPercentiles() {
        return getPercentiles(percentilesFor(totalCount));
    }

    @NotNull
    public double[] getPercentiles(double[] percentileFor) {
        return DoubleStream.of(percentileFor).map(this::percentile).toArray();
    }

    @NotNull
    public String toMicrosFormat() {
        return toMicrosFormat(t -> t / 1e3);
    }

    @NotNull
    public String toMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
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

    @NotNull
    public String toLongMicrosFormat() {
        return toLongMicrosFormat(t -> t / 1e3);
    }

    @NotNull
    public String toLongMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
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

    @NotNull
    private String p(double v) {
        return v < 0.1 ? F3.format(v) :
                v < 1 ? F2.format(v) :
                        v < 10 ? F1.format(v) :
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
