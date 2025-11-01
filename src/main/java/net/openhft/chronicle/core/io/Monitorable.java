/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

/**
 * Implement for resources that may be tracked by tooling such as
 * {@code CloseableUtils}. Calling {@link #unmonitor()} stops that tracking and
 * is typically used just before a resource is discarded.
 */
public interface Monitorable {

    /**
     * Stops monitoring the resource.
     * <p>
     * Implementations of this method should ensure that the resource and any resources it uses
     * are no longer being tracked for any purpose such as cleanup, resource management, or debugging.
     * This is particularly important for resources that are explicitly managed to avoid leaks.
     */
    void unmonitor();

    /**
     * Stops the monitoring of the specified object.
     *
     * @param t The object to stop monitoring
     */
    static void unmonitor(final Object t) {
        if (t instanceof Monitorable)
            ((Monitorable) t).unmonitor();
    }
}
