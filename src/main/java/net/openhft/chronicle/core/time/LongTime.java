//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.time;

public final class LongTime {
    private LongTime() {
    }

    public static final long MAX_NANOS = Long.MAX_VALUE; // 2262-04-11T23:47:16.854775807
    public static final long MAX_MICROS = MAX_NANOS / 1000; // 2262-04-11T23:47:16.854775
    public static final long MAX_MILLIS = MAX_MICROS / 1000; // 2262-04-11T23:47:16.854
    public static final long MAX_SECS = MAX_MILLIS / 1000; // 2262-04-11T23:47:16
    public static final long EPOCH_SECS = 0; // 1970-01-01T00:00:00
    public static final long EPOCH_MILLIS = MAX_SECS + 1; // 1970-04-17T18:02:52.037
    public static final long EPOCH_MICROS = EPOCH_MILLIS * 1000; // 1970-04-17T18:02:52.037
    public static final long EPOCH_NANOS = EPOCH_MICROS * 1000; // 1970-04-17T18:02:52.037

    /**
     * Tests whether the supplied value appears to be a second based timestamp.
     *
     * @param time candidate timestamp, expected to be no earlier than {@link #EPOCH_SECS}
     * @return {@code true} if {@code time} falls between {@link #EPOCH_SECS} and {@link #MAX_SECS}
     */
    public static boolean isSecs(long time) {
        return EPOCH_SECS <= time && time <= MAX_SECS;
    }

    /**
     * Tests whether the supplied value appears to be a millisecond based timestamp.
     *
     * @param time candidate timestamp, expected to be within the millisecond range
     * @return {@code true} if {@code time} lies between {@link #EPOCH_MILLIS} and {@link #MAX_MILLIS}
     */
    public static boolean isMillis(long time) {
        return EPOCH_MILLIS <= time && time <= MAX_MILLIS;
    }

    /**
     * Tests whether the supplied value appears to be a microsecond based timestamp.
     *
     * @param time candidate timestamp, expected to be within the microsecond range
     * @return {@code true} if {@code time} lies between {@link #EPOCH_MICROS} and {@link #MAX_MICROS}
     */
    public static boolean isMicros(long time) {
        return EPOCH_MICROS <= time && time <= MAX_MICROS;
    }

    /**
     * Tests whether the supplied value appears to be a nanosecond based timestamp.
     *
     * @param time candidate timestamp, expected to be at least {@link #EPOCH_NANOS}
     * @return {@code true} if {@code time} is not less than {@link #EPOCH_NANOS}
     */
    public static boolean isNanos(long time) {
        return EPOCH_NANOS <= time /*&& time <= MAX_NANOS*/;
    }

    /**
     * Converts the supplied time to seconds.
     *
     * @param time timestamp in seconds, milliseconds, microseconds or nanoseconds
     * @return the equivalent value in seconds
     */
    public static long toSecs(long time) {
        if (time < EPOCH_MILLIS) // || time < EPOCH_SECS
            return time;
        if (time < EPOCH_MICROS)
            return time / 1000;
        if (time < EPOCH_NANOS)
            return time / 1000_000;
        return time / 1000_000_000;
    }

    /**
     * Converts the supplied time to milliseconds.
     *
     * @param time timestamp in seconds, milliseconds, microseconds or nanoseconds
     * @return the equivalent value in milliseconds
     */
    public static long toMillis(long time) {
        if (time < EPOCH_SECS)
            return time;
        if (time < EPOCH_MILLIS)
            return time * 1000;
        if (time < EPOCH_MICROS)
            return time;
        if (time < EPOCH_NANOS)
            return time / 1000;
        return time / 1000_000;
    }

    /**
     * Converts the supplied time to microseconds.
     *
     * @param time timestamp in seconds, milliseconds, microseconds or nanoseconds
     * @return the equivalent value in microseconds
     */
    public static long toMicros(long time) {
        if (time < EPOCH_SECS)
            return time;
        if (time < EPOCH_MILLIS)
            return time * 1000_000;
        if (time < EPOCH_MICROS)
            return time * 1000;
        if (time < EPOCH_NANOS)
            return time;
        return time / 1_000;
    }

    /**
     * Converts the supplied time to nanoseconds.
     *
     * @param time timestamp in seconds, milliseconds, microseconds or nanoseconds
     * @return the equivalent value in nanoseconds
     */
    public static long toNanos(long time) {
        if (time >= EPOCH_NANOS)
            return time;
        if (time >= EPOCH_MICROS)
            return time * 1000;
        if (time >= EPOCH_MILLIS)
            return time * 1000_000;
        if (time >= EPOCH_SECS)
            return time * 1000_000_000;
        return time;
    }
}
