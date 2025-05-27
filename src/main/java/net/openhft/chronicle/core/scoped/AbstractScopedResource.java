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

import org.jetbrains.annotations.Nullable;

abstract class AbstractScopedResource<T> implements ScopedResource<T> {

    private final long createdTimeNanos;
    private final ScopedThreadLocal<T> scopedThreadLocal;

    protected AbstractScopedResource(ScopedThreadLocal<T> scopedThreadLocal) {
        this.scopedThreadLocal = scopedThreadLocal;
        this.createdTimeNanos = System.nanoTime();
    }

    @Override
    public void close() {
        scopedThreadLocal.returnResource(this);
    }

    /**
     * Do anything that needs to be done before returning a resource to a caller
     */
    void preAcquire() {
        // Do nothing by default
    }

    /**
     * Close the contained resource and clear any references
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
