/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
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

import org.jetbrains.annotations.Nullable;

/**
 * An abstract base class for scoped resources, providing basic functionalities and lifecycle management.
 * <p>
 * This class manages resources that are scoped to a specific context or thread, ensuring that resources
 * are properly initialized, acquired, and released. It implements the {@link ScopedResource} interface
 * and provides a standard implementation for handling resource creation time and scoped thread-local resources.
 * </p>
 *
 * @param <T> The type of the resource being managed.
 */
abstract class AbstractScopedResource<T> implements ScopedResource<T> {

    // Time at which this resource was created, in nanoseconds.
    private final long createdTimeNanos;

    // Scoped thread-local storage for this resource type.
    private final ScopedThreadLocal<T> scopedThreadLocal;

    /**
     * Constructs an AbstractScopedResource with the specified {@link ScopedThreadLocal}.
     *
     * @param scopedThreadLocal The thread-local storage associated with this resource.
     */
    protected AbstractScopedResource(ScopedThreadLocal<T> scopedThreadLocal) {
        this.scopedThreadLocal = scopedThreadLocal;
        this.createdTimeNanos = System.nanoTime();  // Record creation time
    }

    /**
     * Releases this resource, returning it to the scoped thread-local storage.
     * <p>
     * This method is called when the resource is no longer needed, ensuring that it is properly returned
     * and can be reused if necessary.
     * </p>
     */
    @Override
    public void close() {
        scopedThreadLocal.returnResource(this);
    }

    /**
     * Prepares the resource for acquisition.
     * <p>
     * This method is called before a resource is returned to a caller, allowing any necessary
     * preparation steps to be performed. The default implementation does nothing.
     * </p>
     */
    void preAcquire() {
        // Default implementation does nothing
    }

    /**
     * Closes the contained resource and clears any references.
     * <p>
     * This method must be implemented by subclasses to define the specific behavior for closing
     * the resource and cleaning up any references or associated data.
     * </p>
     */
    abstract void closeResource();

    /**
     * Returns the time this resource was created, in nanoseconds.
     *
     * @return The {@link System#nanoTime()} value at the time of creation.
     */
    public long getCreatedTimeNanos() {
        return createdTimeNanos;
    }

    /**
     * Gets the type of the object contained by this resource.
     * <p>
     * This method can return null if the type of the contained object cannot be determined.
     * </p>
     *
     * @return The type of the contained object, or null if it can't be determined.
     */
    @Nullable
    public abstract Class<?> getType();
}
