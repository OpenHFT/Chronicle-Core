/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.alloc;

/**
 * Callback interface for native memory allocation events.
 * <p>
 * Implementations receive notifications when memory is allocated or freed
 * via {@link net.openhft.chronicle.core.UnsafeMemory}. Also serves as the
 * Chronicle Queue MethodWriter/MethodReader contract for the NMT agent.
 */
public interface AllocationListener {

    /**
     * Called after a native memory allocation succeeds.
     *
     * @param timestampMs millisecond wall-clock timestamp
     * @param address     the native memory address returned by allocateMemory
     * @param capacity    the number of bytes requested
     * @param trace       pooled stack trace — must be read/copied before returning
     */
    void onAllocate(long timestampMs, long address, long capacity, AllocationTrace trace);

    /**
     * Called before native memory is freed.
     *
     * @param timestampMs millisecond wall-clock timestamp
     * @param address     the native memory address about to be freed
     * @param size        the number of bytes being freed
     * @param trace       pooled stack trace — must be read/copied before returning
     */
    void onFree(long timestampMs, long address, long size, AllocationTrace trace);
}
