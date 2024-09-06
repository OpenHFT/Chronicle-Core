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

package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.Jvm;

/**
 * Provides a system-based time provider that synthesizes nanosecond-precision wall-clock time
 * using `System.nanoTime` in combination with `System.currentTimeMillis`. This implementation
 * ensures that the time is generally monotonically increasing, adjusting for discrepancies
 * between the nanoTime and currentTimeMillis readings.
 * <p>
 * This enum implements the {@link TimeProvider} interface and is used for scenarios where
 * high-resolution time is required and a monotonic clock is beneficial.
 * </p>
 */
public enum SystemTimeProvider implements TimeProvider {
    /**
     * Singleton instance of {@link SystemTimeProvider}.
     */
    INSTANCE;

    private static final int NANOS_PER_MILLI = 1_000_000;

    /**
     * This can be overridden for testing purposes to provide a different time provider implementation.
     */
    public static TimeProvider CLOCK = INSTANCE;

    static {
        // Warm up the time provider to stabilize initial readings
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 5) {
            INSTANCE.currentTimeNanos();
            Jvm.nanoPause();
        }
    }

    private long delta = 0;

    /**
     * Returns the current time in milliseconds since the Unix epoch.
     * This method delegates to {@link System#currentTimeMillis()} to get the time.
     *
     * @return the current time in milliseconds since the Unix epoch.
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the current time in microseconds since the Unix epoch.
     * This method derives the time in microseconds by dividing the nanosecond time by 1,000.
     *
     * @return the current time in microseconds since the Unix epoch.
     */
    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1000;
    }

    /**
     * Returns a synthesized nanosecond-precision time stamp that is generally
     * monotonically increasing. This method combines `System.nanoTime()` with
     * `System.currentTimeMillis()` to produce a time that approximates the wall-clock
     * time but with nanosecond precision.
     * <p>
     * If the estimated time is less than the current milliseconds time, it adjusts
     * the delta to match. If it is more than a millisecond ahead, it also adjusts the delta.
     * </p>
     *
     * @return the current time in nanoseconds since the Unix epoch.
     */
    @Override
    public long currentTimeNanos() {
        long nowNS = System.nanoTime();  // Current time in nanoseconds
        long nowMS = currentTimeMillis() * NANOS_PER_MILLI;  // Current time in milliseconds converted to nanoseconds
        long estimate = nowNS + delta;  // Estimated time based on nanoTime and delta

        // Adjust delta if estimated time is behind or ahead of the millisecond time
        if (estimate < nowMS) {
            delta = nowMS - nowNS;
            return nowMS;

        } else if (estimate > nowMS + NANOS_PER_MILLI) {
            nowMS += NANOS_PER_MILLI;
            delta = nowMS - nowNS;
            return nowMS;
        }
        return estimate;  // Return the estimated time if within a valid range
    }
}
