/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;

/**
 * A timer for timeouts which is resilient to pauses in the JVM, tickTime can only increase if the JVM hasn't been paused.
 * <p>
 * For this to work, currentTimeMillis (or one of the methods that calls it) must be called more frequently than
 * every millisecond; the EventLoop implementations in chronicle-threads do this.
 */
public final class Time {
    private Time() {
    }

    public static String uniqueId() {
        long l;
        try {
            l = UniqueMicroTimeProvider.INSTANCE.currentTimeMicros();
        } catch (IllegalStateException e) {
            l = SystemTimeProvider.INSTANCE.currentTimeMicros();
        }
        return Long.toString(l, 36);
    }
}
