/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.scoped.ScopedResourcePool;
import net.openhft.chronicle.core.scoped.ScopedThreadLocal;

/**
 * This class provides a pool of StringBuilder objects for efficient string building operations.
 * Each thread gets its own StringBuilder instance via a ThreadLocal,
 * ensuring thread-safety while avoiding synchronization overhead.
 */
public final class StringBuilderPool {

    private static final int DEFAULT_STRING_BUILDER_POOL_SIZE_PER_THREAD = Jvm.getInteger("chronicle.stringBuilderPool.instancesPerThread", 4);

    private StringBuilderPool() {
        // utility
    }

    /**
     * Returns a scoped-thread-local pool of StringBuilders.
     *
     * @return a new pool for the current thread
     */
    public static ScopedResourcePool<StringBuilder> createThreadLocal() {
        return createThreadLocal(DEFAULT_STRING_BUILDER_POOL_SIZE_PER_THREAD);
    }

    /**
     * Create a scoped-thread-local pool of StringBuilders
     *
     * @param instancesPerThread The maximum number of instances to retain for a thread
     * @return the pool of StringBuilders
     */
    public static ScopedResourcePool<StringBuilder> createThreadLocal(int instancesPerThread) {
        return new ScopedThreadLocal<>(
                () -> new StringBuilder(128),
                sb -> sb.setLength(0),
                instancesPerThread);
    }
}
