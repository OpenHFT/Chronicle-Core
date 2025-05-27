/*
 * Copyright 2016-2025 chronicle.software
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
 * SystemTimeProvider synthesises a nanosecond wall-clock time via System.nanoTime deltas in combination with System.currentTimeMillis.
 */
public enum SystemTimeProvider implements TimeProvider {
    INSTANCE;

    private static final int NANOS_PER_MILLI = 1_000_000;
    // Can be overridden for testing purposes.
    public static TimeProvider CLOCK = INSTANCE;

    static {
        // warmUp()
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 5) {
            INSTANCE.currentTimeNanos();
            Jvm.nanoPause();
        }
    }

    private long delta = 0;

    /**
     * Returns the current wall-clock time in milliseconds.
     *
     * @return milliseconds since the Unix epoch
     * @implSpec Thread-safe; the method holds no state.
     * @implNote Values may overflow after year 2262.
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the current wall-clock time in microseconds.
     *
     * @return microseconds since the Unix epoch
     * @implSpec Thread-safe; the method holds no state.
     * @implNote Values may overflow after year 2262.
     */
    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1000;
    }

    /**
     * Returns a nanosecond timestamp which is generally monotonically increasing.
     *
     * @return nanoseconds since the Unix epoch
     * @implSpec Thread-safe; the method holds no state.
     * @implNote Values may overflow after year 2262.
     */
    @Override
    public long currentTimeNanos() {
        long nowNS = System.nanoTime();
        long nowMS = currentTimeMillis() * NANOS_PER_MILLI;
        long estimate = nowNS + delta;

        if (estimate < nowMS) {
            delta = nowMS - nowNS;
            return nowMS;

        } else if (estimate > nowMS + NANOS_PER_MILLI) {
            nowMS += NANOS_PER_MILLI;
            delta = nowMS - nowNS;
            return nowMS;
        }
        return estimate;
    }
}
