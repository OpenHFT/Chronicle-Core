/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleFunction;

import static net.openhft.chronicle.core.time.SystemTimeProvider.CLOCK;

/**
 * An extension of the {@link Histogram} class which not only samples and records data,
 * but also keeps track of the top 5 or 10 longest samples including the delay from the last reset.
 *
 * <p>Example output:
 * <pre>
 * { 50/90 99/99.9 99.99 - worst  was: 500 / 900  1000 / 1000  1000 - 1000, top: [{ off: 10.0, dur: 1000.0 }, { off: 11.0, dur: 950.0 }, { off: 9.0, dur: 900.0 }, { off: 12.0, dur: 850.0 }, { off: 8.0, dur: 800.0 }] }
 * </pre>
 */
public class RecordingHistogram extends Histogram {

    private final Top10 top10 = new Top10();
    private long start;
    private int sampleCount;

    /**
     * Constructs a new RecordingHistogram with specified parameters.
     */
    public RecordingHistogram() {
        super(26, 8, 1e6 / 1024);
    }

    /**
     * Records a sample duration in nanoseconds and keeps track of the top durations.
     *
     * @param durationNs The sample duration in nanoseconds.
     */
    @Override
    public void sampleNanos(long durationNs) {
        super.sampleNanos(durationNs);
        if (start == 0)
            start = currentTimeNanos();
        top10.add(durationNs);
    }

    /**
     * Retrieves the current time in nanoseconds.
     *
     * @return The current time in nanoseconds.
     */
    protected long currentTimeNanos() {
        return CLOCK.currentTimeNanos();
    }

    /**
     * Formats the histogram to a string with duration in microseconds.
     *
     * @param toMicros A function to convert nanoseconds to microseconds.
     * @return A formatted string representation of the histogram.
     */
    @Override
    public @NotNull String toMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
        final String s = super.toMicrosFormat(toMicros);
        return "{ " + s + ", top: " + top10.asString(toMicros, 5) + " }";
    }

    /**
     * Formats the histogram to a long string with duration in microseconds.
     *
     * @param toMicros A function to convert nanoseconds to microseconds.
     * @return A formatted string representation of the histogram.
     */
    @Override
    public @NotNull String toLongMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
        final String s = super.toLongMicrosFormat(toMicros);
        return "{ " + s + ", top: " + top10.asString(toMicros, 10) + " }";
    }

    /**
     * Resets the state of this RecordingHistogram.
     */
    @Override
    public void reset() {
        super.reset();
        sampleCount = 0;
        top10.reset();
    }

    @Override
    protected String was() {
        return " was: ";
    }

    /**
     * Inner class for tracking the top 10 recorded sample durations.
     */
    class Top10 {
        final long[] top = new long[20];
        int count;

        /**
         * Adds a duration to the tracked samples.
         *
         * @param duration The sample duration in nanoseconds.
         */
        void add(long duration) {
            if (count == 0 || duration > top[count * 2 - 1])
                add(currentTimeNanos(), duration);
        }

        void add(long time, long duration) {
            for (int i = 0; i < 20 && i < count * 2; i += 2) {
                long duration2 = top[i + 1];
                if (duration2 < duration) {
                    long time2 = top[i];
                    top[i] = time;
                    top[i + 1] = duration;
                    time = time2;
                    duration = duration2;
                }
            }
            if (count < 10) {
                top[count * 2] = time;
                top[count * 2 + 1] = duration;
                count++;
            }
        }

        /**
         * Resets the state of Top10.
         */
        void reset() {
            count = 0;
        }

        /**
         * Converts the tracked samples to a formatted string.
         *
         * @param toMicros A function to convert nanoseconds to microseconds.
         * @param max      The maximum number of top samples to include in the string.
         * @return A formatted string representation of the top samples.
         */
        public String asString(DoubleFunction<Double> toMicros, int max) {
            if (count == 0)
                return "";
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            String sep = "";
            for (int i = 0, lim = Math.min(Math.min(20, max * 2), count * 2); i < lim; i += 2) {
                // ensure subtraction is performed in floating-point to avoid long overflow prior to conversion
                double offset = toMicros.apply(((double) top[i]) - ((double) start));
                double duration = toMicros.apply(top[i + 1]);
                sb.append(sep).append("{ off: ").append(offset).append(", dur: ").append(duration).append(" }");
                sep = ", ";
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
