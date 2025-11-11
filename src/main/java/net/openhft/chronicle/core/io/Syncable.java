/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

/**
 * Implement for resources that are able to flush data to the underlying
 * medium.  Memory-mapped files and some {@code Bytes} implementations provide
 * this facility.
 */
public interface Syncable {

    /**
     * Performs a sync operation if the supplied object implements
     * {@code Syncable}.  Use when the concrete type may or may not support
     * syncing.
     *
     * @param o object to sync if possible
     */
    static void syncIfAvailable(Object o) {
        if (o instanceof Syncable)
            ((Syncable) o).sync();
    }

    /**
     * Flush data to the backing store up to the point that this handle has read
     * or written. Some implementations may ignore this call.
     */
    void sync();
}
