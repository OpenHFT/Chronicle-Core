/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

/**
 * Supplies wall clock timestamps in milliseconds, microseconds and nanoseconds.
 * <p>
 * Implementations typically delegate to the operating system clock, so the value returned can
 * move backwards if the wall clock is corrected. Key implementations include
 * {@link PosixTimeProvider} and {@link SystemTimeProvider}. The {@code PosixTimeProvider} is often
 * preferred for its enhanced speed, accuracy and stability, though it relies on native code and thus
 * may have platform specific dependencies.
 * <p>
 * This interface is crucial in contexts where precise time measurements are vital, such as in performance
 * monitoring, timestamping events, or handling time-sensitive operations.
 *
 * <p>Use {@link UniqueMicroTimeProvider} if monotonic timestamps are required.
 *
 * @see PosixTimeProvider
 * @see SystemTimeProvider
 */
@FunctionalInterface
public interface TimeProvider {

    /**
     * Retrieves the current time in milliseconds.
     * <p>
     * This method returns the current time with millisecond precision, measured from the Unix epoch
     * (00:00:00 UTC on 1 January 1970). Implementations must guarantee thread-safe access.
     *
     * @return the current time in milliseconds since the Unix epoch
     */
    long currentTimeMillis();

    /**
     * Retrieves the current time in microseconds.
     * <p>
     * This default implementation offers microsecond precision by scaling the millisecond value from
     * {@link #currentTimeMillis()} by a factor of 1000. Implementations may override this for higher
     * accuracy if available.
     *
     * @return the current time in microseconds since the Unix epoch
     * @throws IllegalStateException if the time value cannot be accurately determined or converted
     */
    default long currentTimeMicros() throws IllegalStateException {
        return currentTimeMillis() * 1000;
    }

    /**
     * Retrieves the current time in nanoseconds.
     * <p>
     * This default method provides nanosecond precision by further scaling the microsecond value
     * from {@link #currentTimeMicros()} by 1000. Implementations may provide more precise or direct
     * measurements if their underlying system supports it.
     *
     * @return the current time in nanoseconds since the Unix epoch
     * @throws IllegalStateException if the time value cannot be accurately determined or converted
     */
    default long currentTimeNanos() throws IllegalStateException {
        return currentTimeMicros() * 1000;
    }
}
