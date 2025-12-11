/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;
/**
 * Extends {@link ReferenceCountedTracer} with the ability to suppress discard
 * warnings. When a resource is marked as unmonitored the
 * {@link ReferenceCountedTracer#warnAndReleaseIfNotReleased()} path will not log
 * a warning if the user forgets to release a reservation.
 */
public interface MonitorReferenceCounted extends ReferenceCountedTracer {

    /**
     * Sets the monitored state of the object.
     *
     * @param unmonitored {@code true} to set the object as unmonitored, {@code false} to set it as monitored.
     */
    void unmonitored(boolean unmonitored);

    /**
     * Indicates whether monitoring is disabled for this instance.
     *
     * @return {@code true} if the object is unmonitored, {@code false} if it is monitored.
     */
    boolean unmonitored();
}
