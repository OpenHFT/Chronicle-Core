/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import org.jetbrains.annotations.Nullable;

/**
 * Base implementation of {@link ScopedResource} instances returned by
 * {@link ScopedThreadLocal}. Each instance is confined to the thread that
 * created it and records the creation time using {@link System#nanoTime()}.
 * The timestamp allows {@link ScopedThreadLocal} to discard the oldest
 * resource when the per-thread capacity is exceeded.
 */
abstract class AbstractScopedResource<T> implements ScopedResource<T> {

    private final long createdTimeNanos;
    private final ScopedThreadLocal<T> scopedThreadLocal;

    /**
     * Create a new instance bound to the provided {@code scopedThreadLocal}.
     *
     * @param scopedThreadLocal the pool managing this resource for the current thread
     */
    protected AbstractScopedResource(ScopedThreadLocal<T> scopedThreadLocal) {
        this.scopedThreadLocal = scopedThreadLocal;
        this.createdTimeNanos = System.nanoTime();
    }

    @Override
    public void close() {
        scopedThreadLocal.returnResource(this);
    }

    /**
     * Prepare the resource before it is handed to the caller.
     *
     * <p>
     * Invoked by {@link ScopedThreadLocal#get()} on the owning thread just
     * before returning the resource. The default implementation does nothing.
     *
     * <p>
     * Implementations may assume thread confinement and should avoid heavy
     * allocation if possible.
     */
    void preAcquire() {
        // Do nothing by default
    }

    /**
     * Close the contained resource and clear any references.
     *
     * <p>
     * Called when a resource is permanently discarded from the pool or when the
     * thread-local stack is closed.
     *
     * <p>
     * Implementations should release all state and must be idempotent.
     */
    abstract void closeResource();

    /**
     * The time this resource was created
     *
     * @return the {@link System#nanoTime()} of creation
     */
    public long getCreatedTimeNanos() {
        return createdTimeNanos;
    }

    /**
     * Get the type of object contained, may return null
     *
     * @return the type of object contained, or null if it can't be determined
     */
    @Nullable
    public abstract Class<?> getType();
}
