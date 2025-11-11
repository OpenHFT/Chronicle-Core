/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.Jvm;

/**
 * Synthesises a nanosecond wall clock from the system clock.
 *
 * <p>This provider keeps a running {@code delta} between
 * {@code System.nanoTime()} and {@code System.currentTimeMillis()}.
 * The delta is adjusted whenever the calculated estimate falls behind
 * the millisecond tick or drifts more than one millisecond ahead. This
 * keeps the returned value monotonic and within roughly a millisecond of
 * the wall clock.
 *
 * <pre>
 * System.currentTimeMillis : |----|----|----|
 * System.nanoTime          : ---------&gt;
 *                             ^
 *                             | delta
 * currentTimeNanos()         : ---------&gt;
 * </pre>
 *
 * Typical call latency is about 40 ns on modern hardware.
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
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the current wall-clock time in microseconds.
     *
     * @return microseconds since the Unix epoch
     */
    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1000;
    }

    /**
     * Returns a nanosecond timestamp derived from {@code System.nanoTime()} and
     * adjusted by {@code delta}. The result is monotonic and kept within one
     * millisecond of {@code System.currentTimeMillis()}.
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
