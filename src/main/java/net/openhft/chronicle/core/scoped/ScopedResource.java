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

import java.io.Closeable;

/**
 * A scoped resource, it is drawn from a "pool" of sorts, and will be returned
 * to that pool when {@link #close()} is called.
 * <p>
 * Do not keep a reference to the contained resource beyond the scope.
 *
 * @param <T> The type of the resource contained
 */
public interface ScopedResource<T> extends Closeable {

    /**
     * Get the contained resource
     *
     * @return The resource
     */
    T get();

    /**
     * Signifies the end of the scope, will return the resource to the "pool" for use by other acquirers
     */
    @Override
    void close();
}
