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
 * keeps the returned value within roughly a millisecond of the wall clock.
 *
 * <p>This provider is intended to be as light weight as possible: the
 * shared {@code delta} is a plain field and is updated without
 * synchronisation. With one caller and a non-decreasing wall clock,
 * {@link #currentTimeNanos()} is monotonic. A backward wall-clock adjustment
 * can reverse its value even with one caller. Under concurrent calls a thread
 * can also observe the value step back, typically by less than a millisecond.
 * Callers that need a monotonic guarantee despite either condition
 * should use {@link MonotonicTimeProvider}; callers that need unique
 * timestamps should use the time providers in Chronicle Bytes.
 * Set {@code -Dchronicle.time.monotonic=true} before initialisation to select
 * {@code MonotonicTimeProvider} through {@link #CLOCK}. The default is false;
 * direct calls to {@link #INSTANCE} always retain this provider's behaviour.
 * The monotonic provider may become the default for {@code CLOCK} in 2027.x.
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
    /**
     * The shared clock, initially selected using
     * {@link Jvm#getBoolean(String) Jvm.getBoolean("chronicle.time.monotonic")}.
     * False (the default) selects {@link #INSTANCE}; true selects
     * {@link MonotonicTimeProvider#INSTANCE}. Selection happens once during
     * class initialisation. This field can still be overridden for testing.
     * The monotonic provider may become the default in 2027.x.
     */
    public static TimeProvider CLOCK = INSTANCE;

    static {
        if (Jvm.getBoolean("chronicle.time.monotonic"))
            CLOCK = MonotonicTimeProvider.INSTANCE;

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
     * adjusted by {@code delta}, kept within one millisecond of
     * {@code System.currentTimeMillis()}. Concurrent calls or a backward
     * wall-clock adjustment can make the result decrease; see the class
     * Javadoc and {@link MonotonicTimeProvider} for a monotonic alternative.
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
