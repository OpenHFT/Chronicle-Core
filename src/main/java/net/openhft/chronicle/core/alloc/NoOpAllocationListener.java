/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.alloc;

/**
 * No-op singleton used as the default listener.
 * Identity-checked by {@link net.openhft.chronicle.core.UnsafeMemory} to skip
 * ThreadLocal access and stack capture on the hot path.
 */
public enum NoOpAllocationListener implements AllocationListener {
    INSTANCE;

    @Override
    public void onAllocate(long timestampMs, long address, long capacity, AllocationTrace trace) {
        // no-op
    }

    @Override
    public void onFree(long timestampMs, long address, long size, AllocationTrace trace) {
        // no-op
    }
}
