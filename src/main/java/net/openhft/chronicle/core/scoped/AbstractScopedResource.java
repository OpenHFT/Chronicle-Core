/*
 * Copyright 2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.scoped;

/**
 * Base implementation of {@link ScopedResource} instances returned by
 * {@link ScopedThreadLocal}. Each instance is confined to the thread that
 * created it and records the creation time using {@link System#nanoTime()}.
 * The timestamp allows {@link ScopedThreadLocal} to discard the oldest
 * resource when the per-thread capacity is exceeded.
 */

import org.jetbrains.annotations.Nullable;

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
     * @implSpec
     * Invoked by {@link ScopedThreadLocal#get()} on the owning thread just
     * before returning the resource. The default implementation does nothing.
     *
     * @implNote
     * Implementations may assume thread confinement and should avoid heavy
     * allocation if possible.
     */
    void preAcquire() {
        // Do nothing by default
    }

    /**
     * Close the contained resource and clear any references.
     *
     * @implSpec
     * Called when a resource is permanently discarded from the pool or when the
     * thread-local stack is closed.
     *
     * @implNote
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
