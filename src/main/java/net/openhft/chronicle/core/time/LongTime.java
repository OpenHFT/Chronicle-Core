/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.time;

/**
 * Utility class for time-related constants and conversion methods.
 * This class provides methods to work with different time units (seconds, milliseconds,
 * microseconds, nanoseconds) and to convert between them, based on long values representing time.
 * <p>
 * The class also defines various constants representing maximum possible values for each time unit,
 * based on the maximum value of a long (`Long.MAX_VALUE`), as well as epoch constants.
 */
public final class LongTime {

    // Suppresses default constructor, ensuring non-instantiability.
    private LongTime() {
    }

    // Maximum values for each time unit, calculated based on Long.MAX_VALUE.
    public static final long MAX_NANOS = Long.MAX_VALUE; // 2262-04-11T23:47:16.854775807
    public static final long MAX_MICROS = MAX_NANOS / 1000; // 2262-04-11T23:47:16.854775
    public static final long MAX_MILLIS = MAX_MICROS / 1000; // 2262-04-11T23:47:16.854
    public static final long MAX_SECS = MAX_MILLIS / 1000; // 2262-04-11T23:47:16

    // Epoch constants for each time unit, representing the start of the Unix epoch.
    public static final long EPOCH_SECS = 0; // 1970-01-01T00:00:00
    public static final long EPOCH_MILLIS = MAX_SECS + 1; // 1970-04-17T18:02:52.037
    public static final long EPOCH_MICROS = EPOCH_MILLIS * 1000; // 1970-04-17T18:02:52.037
    public static final long EPOCH_NANOS = EPOCH_MICROS * 1000; // 1970-04-17T18:02:52.037

    /**
     * Checks if the given time value is in seconds.
     *
     * @param time the time value to check.
     * @return {@code true} if the time is in seconds, {@code false} otherwise.
     */
    public static boolean isSecs(long time) {
        return EPOCH_SECS <= time && time <= MAX_SECS;
    }

    /**
     * Checks if the given time value is in milliseconds.
     *
     * @param time the time value to check.
     * @return {@code true} if the time is in milliseconds, {@code false} otherwise.
     */
    public static boolean isMillis(long time) {
        return EPOCH_MILLIS <= time && time <= MAX_MILLIS;
    }

    /**
     * Checks if the given time value is in microseconds.
     *
     * @param time the time value to check.
     * @return {@code true} if the time is in microseconds, {@code false} otherwise.
     */
    public static boolean isMicros(long time) {
        return EPOCH_MICROS <= time && time <= MAX_MICROS;
    }

    /**
     * Checks if the given time value is in nanoseconds.
     *
     * @param time the time value to check.
     * @return {@code true} if the time is in nanoseconds, {@code false} otherwise.
     */
    public static boolean isNanos(long time) {
        return EPOCH_NANOS <= time /*&& time <= MAX_NANOS*/;
    }

    /**
     * Converts the given time value to seconds.
     *
     * @param time the time value to convert.
     * @return the time in seconds.
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
     * Converts the given time value to milliseconds.
     *
     * @param time the time value to convert.
     * @return the time in milliseconds.
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
     * Converts the given time value to microseconds.
     *
     * @param time the time value to convert.
     * @return the time in microseconds.
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
     * Converts the given time value to nanoseconds.
     *
     * @param time the time value to convert.
     * @return the time in nanoseconds.
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
