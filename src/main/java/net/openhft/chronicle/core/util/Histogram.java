/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

import net.openhft.chronicle.core.annotation.SingleThreaded;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * A {@link Histogram} class represents a histogram of samples. The histogram is defined by a set of buckets,
 * each of which counts the number of samples that fall into its range of values. The range of values for each
 * bucket is determined by a combination of the number of powers of 2 and the number of fraction bits.
 * <p>
 * Note that this class is marked as {@link SingleThreaded}, which means it is not safe for use by multiple threads
 * simultaneously.
 * <p>
 * This class implements the {@link NanoSampler} interface, which provides a method for sampling values in nanoseconds.
 */
@SingleThreaded
public class Histogram implements NanoSampler {
    // Decimal formats for various precisions
    private static final DecimalFormat F3 = new DecimalFormat("0.000");
    private static final DecimalFormat F2 = new DecimalFormat("0.00");
    private static final DecimalFormat F1 = new DecimalFormat("0.0");

    // Parameters defining the histogram
    private int fractionBits; // Number of bits used for fractions
    private int powersOf2; // Number of powers of 2 to use
    private long overRange; // Count of samples over the maximum range
    private long totalCount; // Total count of all samples
    private long floor; // Minimum value bucketed in the histogram
    private int[] sampleCount; // Array to hold sample counts for each bucket

    /**
     * Creates a new Histogram with default parameters. The default number of powers of 2 is 42 and the default
     * number of fraction bits is 8.
     */
    public Histogram() {
        this(42, 8);
    }

    /**
     * Creates a new Histogram with the specified number of powers of 2 and fraction bits.
     *
     * @param powersOf2    the number of powers of 2 to use in the histogram
     * @param fractionBits the number of fraction bits to use in the histogram
     */
    public Histogram(int powersOf2, int fractionBits) {
        this(powersOf2, fractionBits, 1.0);
    }

    /**
     * Creates a new Histogram with the specified number of powers of 2, fraction bits, and minimum value.
     *
     * @param powersOf2    the number of powers of 2 to use in the histogram
     * @param fractionBits the number of fraction bits to use in the histogram
     * @param minValue     the minimum value for the histogram
     */
    public Histogram(int powersOf2, int fractionBits, double minValue) {
        this.powersOf2 = powersOf2;
        this.fractionBits = fractionBits;
        // Initialize the sample count array based on powers of 2 and fraction bits
        sampleCount = new int[powersOf2 << fractionBits];
        // Calculate the floor based on the minimum value and fraction bits
        floor = Double.doubleToRawLongBits(minValue) >> (52 - fractionBits);
    }

    /**
     * Returns a Histogram designed for use with System.nanoTime() for timing up to 4 seconds delay.
     *
     * @return A Histogram configured for microsecond precision timing.
     */
    @NotNull
    public static Histogram timeMicros() {
        return new Histogram(22 /* 4 seconds */, 3 /* 2 decimal places */, 1000.0 /* nano-seconds */);
    }

    /**
     * Calculates the percentiles for a given count.
     *
     * @param count the count of values
     * @return an array of doubles representing the calculated percentiles
     */
    public static double[] percentilesFor(long count) {
        List<Double> values = new ArrayList<>();
        values.add(50 / 100.0);
        values.add(90 / 100.0);
        values.add(99 / 100.0);
        if (count > 10_000)
            values.add(0.997);
        for (int x = 1000; x <= 10_000_000; x *= 10) {
            if (count < 100L * x)
                break;
            values.add(1 - 1.0 / x);
            if (count < 300L * x)
                break;
            values.add(1 - 3.0 / (10 * x));
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

    @Override
    public int hashCode() {
        int result = fractionBits;
        result = 31 * result + powersOf2;
        result = 31 * result + (int) (overRange ^ (overRange >>> 32));
        result = 31 * result + (int) (totalCount ^ (totalCount >>> 32));
        result = 31 * result + (int) (floor ^ (floor >>> 32));
        result = 31 * result + Arrays.hashCode(sampleCount);
        return result;
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

    /**
     * Gets the number of fraction bits used by the histogram.
     *
     * @return the number of fraction bits
     */
    public int fractionBits() {
        return fractionBits;
    }

    /**
     * Gets the number of powers of 2 used by the histogram.
     *
     * @return the number of powers of 2
     */
    public int powersOf2() {
        return powersOf2;
    }

    /**
     * Gets the number of values that are over the range of the histogram's buckets.
     *
     * @return the over range count
     */
    public long overRange() {
        return overRange;
    }

    /**
     * Gets the array of sample counts per bucket.
     *
     * @return the array of sample counts
     */
    public int[] sampleCount() {
        return sampleCount;
    }

    /**
     * Adds the contents of another histogram to this one. The other histogram must have the same number of powers of 2
     * and fraction bits as this one.
     *
     * @param h the other histogram
     * @throws AssertionError if the other histogram does not have the same number of powers of 2 and fraction bits
     */
    public void add(@NotNull Histogram h) {
        assert powersOf2 == h.powersOf2;
        assert fractionBits == h.fractionBits;
        totalCount += h.totalCount;
        overRange += h.overRange;
        for (int i = 0; i < sampleCount.length; i++)
            sampleCount[i] += h.sampleCount[i];
    }

    /**
     * Samples a value and updates the histogram accordingly. The value is placed into a bucket based on its size,
     * with the bucket ranges determined by the powers of 2 and fraction bits specified when the histogram was created.
     *
     * @param time the value to sample
     * @return the bucket that the value was placed in
     */
    public int sample(double time) {
        int bucket = (int) ((Double.doubleToRawLongBits(time) >> (52 - fractionBits)) - floor);
        if (bucket >= sampleCount.length)
            overRange++;
        else if (bucket >= 0)
            sampleCount[bucket]++;
        totalCount++;
        return bucket;
    }

    /**
     * Gets the minimum value in the histogram.
     * This is equivalent to the 0th percentile.
     *
     * @return The minimum value represented in the histogram.
     */
    public double min() {
        return percentile(0.0);
    }

    /**
     * Gets the median value in the histogram.
     * This is equivalent to the 50th percentile, also known as the typical value.
     *
     * @return The median value represented in the histogram.
     */
    public double typical() {
        return percentile(0.5);
    }

    /**
     * Gets the maximum value in the histogram.
     * This is equivalent to the 100th percentile.
     *
     * @return The maximum value represented in the histogram.
     */
    public double max() {
        return percentile(1.0);
    }

    /**
     * Calculates the value at a given percentile in the histogram.
     * The percentile is represented as a fraction between 0.0 and 1.0, where 0.0 corresponds to the minimum value,
     * 0.5 corresponds to the median, and 1.0 corresponds to the maximum value.
     *
     * @param fraction The percentile as a fraction (0.0 for the minimum, 1.0 for the maximum).
     * @return The value at the given percentile.
     */
    public double percentile(double fraction) {
        if (fraction <= 0) {
            // Find the smallest value in the histogram that has a non-zero count
            for (int i = 0; i < sampleCount.length; i++) {
                if (sampleCount[i] <= 0)
                    continue; // Skip empty buckets
                // Calculate the double value corresponding to the histogram bucket
                long bits = ((((i + floor) << 1) + 1) << (51 - fractionBits));
                return Double.longBitsToDouble(bits);
            }
            return 1; // Return default value if all buckets are empty
        }

        // Calculate the target count based on the total count and the desired percentile
        long value = (long) (totalCount * (1 - fraction));
        value -= overRange; // Adjust for samples that are over the maximum range
        if (value < 0)
            return Double.POSITIVE_INFINITY; // Return infinity if value is less than zero
        // Find the histogram bucket corresponding to the desired percentile
        for (int i = sampleCount.length - 1; i >= 0; i--) {
            value -= sampleCount[i];
            if (value < 0) {
                // Calculate the double value corresponding to the histogram bucket
                long bits = ((((i + floor) << 1) + 1) << (51 - fractionBits));
                return Double.longBitsToDouble(bits);
            }
        }

        return 1; // Return default value if no bucket is found
    }

    /**
     * Calculates the percentage of values that are less than the given time.
     *
     * @param time the time to compare the values against
     * @return the percentage of values less than the given time
     */
    public double percentageLessThan(double time) {
        int bucket = (int) ((Double.doubleToRawLongBits(time) >> (52 - fractionBits)) - floor);
        long perthousand = 1000L * IntStream.rangeClosed(0, bucket).mapToLong(i -> sampleCount[i]).sum() / totalCount;
        return perthousand / 10.0;
    }

    /**
     * Calculates an array of percentiles based on the total count.
     *
     * @return an array of percentiles
     */
    public double @NotNull [] getPercentiles() {
        return getPercentiles(percentilesFor(totalCount));
    }

    /**
     * Calculates an array of percentiles for the given set of values.
     *
     * @param percentileFor an array of values for which to calculate percentiles
     * @return an array of calculated percentiles
     */
    public double @NotNull [] getPercentiles(double[] percentileFor) {
        return DoubleStream.of(percentileFor).map(this::percentile).toArray();
    }

    /**
     * Returns a string representation of the histogram in a format suitable for display in microseconds.
     * This is a convenience method that uses a default function to convert the values to microseconds.
     *
     * @return a string representation of the histogram
     */
    @NotNull
    public String toMicrosFormat() {
        return toMicrosFormat(t -> t / 1e3);
    }

    /**
     * Returns a string representation of the histogram in a format suitable for display in microseconds.
     * The given function is used to convert the values to microseconds.
     *
     * @param toMicros a function that converts a value to microseconds
     * @return a string representation of the histogram
     */
    @NotNull
    public String toMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
        if (totalCount < 1_000_000)
            return "50/90 99/99.9 99.99 - worst " + was() +
                    p(toMicros.apply(percentile(0.5))) + " / " +
                    p(toMicros.apply(percentile(0.9))) + "  " +
                    p(toMicros.apply(percentile(0.99))) + " / " +
                    p(toMicros.apply(percentile(0.999))) + "  " +
                    p(toMicros.apply(percentile(0.9999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        if (totalCount < 10_000_000)
            return "50/90 99/99.9 99.99/99.999 - worst " + was() +
                    p(toMicros.apply(percentile(0.5))) + " / " +
                    p(toMicros.apply(percentile(0.9))) + "  " +
                    p(toMicros.apply(percentile(0.99))) + " / " +
                    p(toMicros.apply(percentile(0.999))) + "  " +
                    p(toMicros.apply(percentile(0.9999))) + " / " +
                    p(toMicros.apply(percentile(0.99999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        return "50/90 99/99.9 99.99/99.999 99.9999/worst " + was() +
                p(toMicros.apply(percentile(0.5))) + " / " +
                p(toMicros.apply(percentile(0.9))) + "  " +
                p(toMicros.apply(percentile(0.99))) + " / " +
                p(toMicros.apply(percentile(0.999))) + "  " +
                p(toMicros.apply(percentile(0.9999))) + " / " +
                p(toMicros.apply(percentile(0.99999))) + "  " +
                p(toMicros.apply(percentile(0.999999))) + " / " +
                p(toMicros.apply(percentile(1)));
    }

    /**
     * Returns a string representation of the histogram in a long format suitable for display in microseconds.
     * This is a convenience method that uses a default function to convert the values to microseconds.
     *
     * @return a string representation of the histogram
     */
    @NotNull
    public String toLongMicrosFormat() {
        return toLongMicrosFormat(t -> t / 1e3);
    }

    /**
     * Returns a string representation of the histogram in a long format suitable for display in microseconds.
     * The given function is used to convert the values to microseconds.
     *
     * @param toMicros a function that converts a value to microseconds
     * @return a string representation of the histogram
     */
    @NotNull
    public String toLongMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
        if (totalCount < 1_000_000)
            return "50/90 97/99 99.7/99.9 99.97/99.99 - worst " + was() +
                    first4nines(toMicros) + " - " +
                    p(toMicros.apply(percentile(1)));

        if (totalCount < 10_000_000)
            return "50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 - worst " + was() +
                    first4nines(toMicros) + "  " +
                    p(toMicros.apply(percentile(0.99997))) + " / " +
                    p(toMicros.apply(percentile(0.99999))) + " - " +
                    p(toMicros.apply(percentile(1)));

        return "50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst " + was() +
                first4nines(toMicros) + "  " +
                p(toMicros.apply(percentile(0.99997))) + " / " +
                p(toMicros.apply(percentile(0.99999))) + "  " +
                p(toMicros.apply(percentile(0.999997))) + " / " +
                p(toMicros.apply(percentile(0.999999))) + " - " +
                p(toMicros.apply(percentile(1)));
    }

    /**
     * Returns a string indicating a past tense state, used for formatting.
     *
     * @return A string "was ".
     */
    protected String was() {
        return "was ";
    }

    /**
     * Generates a string representation of specific percentile values, formatted to show precision up to four nines (99.99%).
     * This method converts percentile values to microseconds and formats them using predefined precision rules.
     *
     * @param toMicros A function to convert the percentile values to microseconds.
     * @return A formatted string representing the percentile values at various levels of precision.
     */
    @NotNull
    private String first4nines(@NotNull DoubleFunction<Double> toMicros) {
        return p(toMicros.apply(percentile(0.5))) + " / " +
                p(toMicros.apply(percentile(0.9))) + "  " +
                p(toMicros.apply(percentile(0.97))) + " / " +
                p(toMicros.apply(percentile(0.99))) + "  " +
                p(toMicros.apply(percentile(0.997))) + " / " +
                p(toMicros.apply(percentile(0.999))) + "  " +
                p(toMicros.apply(percentile(0.9997))) + " / " +
                p(toMicros.apply(percentile(0.9999)));
    }

    /**
     * Formats a double value according to its range, using different precision formats for different ranges.
     * This method uses static non-thread-safe fields for formatting, hence it is synchronized on the {@link Histogram} class.
     *
     * @param v The value to be formatted.
     * @return The formatted string representation of the value.
     */
    @NotNull
    private String p(double v) {
        double v2 = v * 100 / (1 << fractionBits);
        // Uses non thread-safe static fields for formatting, hence synchronized.
        synchronized (Histogram.class) {
            return v2 < 1 ? F3.format(v) :
                    v2 < 10 ? F2.format(v) :
                            v2 < 100 ? F1.format(v) :
                                    v2 < 1000 ? Long.toString(Math.round(v)) :
                                            String.format("%,d", Math.round(v / 10) * 10);
        }
    }

    /**
     * Returns the total count of samples in the histogram.
     *
     * @return The total count of samples.
     */
    public long totalCount() {
        return totalCount;
    }

    /**
     * Returns the minimum value bucketed in the histogram (floor value).
     *
     * @return The floor value.
     */
    public long floor() {
        return floor;
    }

    /**
     * Resets the histogram, clearing all samples and resetting counts.
     */
    public void reset() {
        totalCount = overRange = 0; // Reset total count and over-range count
        Arrays.fill(sampleCount, 0); // Reset all bucket counts to zero
    }

    @Override
    public void sampleNanos(long durationNs) {
        sample(durationNs);
    }
}
