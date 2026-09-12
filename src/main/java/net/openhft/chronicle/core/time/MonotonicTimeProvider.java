/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Synthesises a nanosecond wall clock from the system clock, with a
 * monotonicity guarantee that holds under concurrent calls.
 *
 * <p>Like {@link SystemTimeProvider}, this provider keeps a running
 * {@code delta} between {@code System.nanoTime()} and
 * {@code System.currentTimeMillis()}, adjusted whenever the estimate falls
 * behind the millisecond tick or drifts more than one millisecond ahead.
 * Unlike {@link SystemTimeProvider}, the result of
 * {@link #currentTimeNanos()} is then passed through an atomic maximum.
 * If one call completes before another begins, the latter returns at least
 * the former's value, even on another thread. Overlapping calls are ordered
 * by their atomic updates, which need not match the order in which they return.
 *
 * <p>Guarantees and costs compared with {@link SystemTimeProvider}:
 * <ul>
 *   <li>{@code currentTimeNanos()} never goes backwards, within a thread or
 *       between ordered calls on different threads.</li>
 *   <li>Values are not unique: concurrent callers can observe the same
 *       value. For unique timestamps use
 *       {@code net.openhft.chronicle.bytes.MappedUniqueTimeProvider} or
 *       {@code DistributedUniqueTimeProvider}.</li>
 *   <li>In normal operation the estimate tracks wall time to about one
 *       millisecond. After a backward clock adjustment, the result can remain
 *       ahead of wall time and stop advancing until the estimate catches up.
 *       Scheduling delays can also make the sampled time stale.</li>
 *   <li>Each call updates a shared atomic value. This adds overhead compared
 *       with {@link SystemTimeProvider}, and contention can limit throughput.</li>
 *   <li>{@code currentTimeMicros()} has the same ordering guarantee, but
 *       {@code currentTimeMillis()} reads wall time directly and can reverse.</li>
 * </ul>
 * <p>Wall-clock samples must fit in a signed {@code long} when converted to
 * nanoseconds, including the one-millisecond upper adjustment. Negative epoch
 * timestamps within that range are supported.
 *
 * <p>Set {@code -Dchronicle.time.monotonic=true} before
 * {@link SystemTimeProvider} is initialised to select this provider through
 * {@link SystemTimeProvider#CLOCK}. The default is false in 2026.x;
 * this provider may become the default in 2027.x.
 */
public enum MonotonicTimeProvider implements TimeProvider {
    INSTANCE;

    private static final int NANOS_PER_MILLI = 1_000_000;

    private final Clock clock = new Clock();

    /**
     * Returns the current wall-clock time in milliseconds.
     * This value is read directly from the system clock and can go
     * backwards if the wall clock is adjusted.
     *
     * @return milliseconds since the Unix epoch
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the current wall-clock time in microseconds, derived from
     * {@link #currentTimeNanos()} and therefore monotonic.
     *
     * @return microseconds since the Unix epoch
     */
    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1000;
    }

    /**
     * Returns a nanosecond timestamp derived from {@code System.nanoTime()}
     * and adjusted towards wall time, then ratcheted to preserve the ordering
     * guarantee described in the class Javadoc. Values need not be unique.
     */
    @Override
    public long currentTimeNanos() {
        return clock.currentTimeNanos(System.nanoTime(), currentTimeMillis());
    }

    // Separate clock sampling from state so tests can supply clock steps without
    // changing the host clock or the singleton's state.
    static final class Clock {
        private volatile long delta;
        private final AtomicLong lastReturned = new AtomicLong(Long.MIN_VALUE);

        long currentTimeNanos(long nowNS, long nowMillis) {
            long nowMS = nowMillis * NANOS_PER_MILLI;
            long estimate = nowNS + delta;

            if (estimate < nowMS) {
                delta = nowMS - nowNS;
                estimate = nowMS;

            } else if (estimate > nowMS + NANOS_PER_MILLI) {
                nowMS += NANOS_PER_MILLI;
                delta = nowMS - nowNS;
                estimate = nowMS;
            }
            // Racing delta updates may produce an older estimate, but cannot
            // decrease the atomic maximum.
            return lastReturned.accumulateAndGet(estimate, Math::max);
        }
    }
}
