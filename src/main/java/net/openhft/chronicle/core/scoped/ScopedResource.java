/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 * A short-lived handle to a shared resource.
 * <p>
 * Instances are obtained from a {@link ScopedResourcePool} such as
 * {@link ScopedThreadLocal} and are expected to be used with the
 * try-with-resources idiom. The underlying resource is returned to the pool
 * when {@link #close()} completes. Clients must not retain a reference to the
 * resource after the scope ends.
 *
 * @param <T> the type of the resource contained
 * @see ScopedResourcePool
 * @see ScopedThreadLocal
 */
public interface ScopedResource<T> extends Closeable {

    /**
     * Get the contained resource
     *
     * @return The resource
     */
    T get();

    /**
     * Signifies the end of the scope and returns the resource to the pool for
     * use by other acquirers.
     * <p>
     * This method is idempotent; additional invocations have no effect.
     */
    @Override
    void close();
}
