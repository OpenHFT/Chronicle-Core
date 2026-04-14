/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.posix.ClockId;
import net.openhft.posix.PosixAPI;

/**
 * Provides time based on the Posix standard, particularly utilizing native code
 * to access high-resolution time.
 * <p>
 * This provider interfaces directly with the native method {@code clock_gettime}
 * to fetch nanosecond precision wall-clock time on platforms that support it. It is
 * important to note that this time provider relies on native system calls, thus is
 * platform-dependent. It is typically more accurate and has finer resolution than
 * standard Java time providers.
 */
public enum PosixTimeProvider implements TimeProvider {
    INSTANCE;

    /**
     * Returns the current time in milliseconds using the standard Java system clock.
     *
     * @return the current time in milliseconds since the Unix epoch
     */
    @Override
    public long currentTimeMillis() {
        // CQTimeApiIndirection keep System.currentTimeMillis here because time-provider implementations must not delegate through another provider instance.
        return System.currentTimeMillis();
    }

    /**
     * Returns the current time in microseconds.
     * <p>
     * This method provides microsecond precision by dividing the nanosecond value by 1000.
     *
     * @return the current time in microseconds since the Unix epoch
     * @throws IllegalStateException if the time cannot be determined or converted
     */
    @Override
    public long currentTimeMicros() throws IllegalStateException {
        return currentTimeNanos() / 1000;
    }

    /**
     * Returns the current time in nanoseconds.
     * <p>
     * This method directly calls the native {@code clock_gettime} function, providing
     * highly precise and accurate nanosecond resolution time. It uses {@link ClockId#CLOCK_REALTIME}
     * for fetching the current time.
     *
     * @return the current time in nanoseconds since the Unix epoch
     * @throws IllegalStateException if the native call fails
     */
    @Override
    public long currentTimeNanos() {
        return PosixAPI.posix().clock_gettime(ClockId.CLOCK_REALTIME);
    }
}
