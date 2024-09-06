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

import java.io.Closeable;

/**
 * Represents a scoped resource that is managed within a defined scope.
 * <p>
 * A {@code ScopedResource} is obtained from a pool and is intended to be used within a limited scope.
 * When the scope is exited, the resource should be returned to the pool by calling {@link #close()}.
 * </p>
 * <p>
 * Implementations of this interface are responsible for managing the lifecycle of the resource, ensuring
 * that it is properly acquired and released. It is important not to keep references to the resource
 * beyond the scope to prevent resource leaks or unintended behavior.
 * </p>
 *
 * @param <T> The type of the resource being managed.
 */
public interface ScopedResource<T> extends Closeable {

    /**
     * Retrieves the resource contained within this scope.
     * <p>
     * This method provides access to the underlying resource. It is the responsibility of the caller
     * to ensure that the resource is used only within the intended scope and is properly returned
     * by calling {@link #close()}.
     * </p>
     *
     * @return The resource contained within this scope.
     */
    T get();

    /**
     * Ends the scope of this resource, returning it to the pool for reuse.
     * <p>
     * This method should be called when the resource is no longer needed, allowing it to be returned
     * to the pool and made available for other acquirers. Once {@code close()} is called, the resource
     * should not be used again, and any further attempts to use it may result in undefined behavior.
     * </p>
     */
    @Override
    void close();
}
