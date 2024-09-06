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

/**
 * A pool of resources, whereby a lease is taken on an instance
 * by calling {@link #get()} and that lease is relinquished when
 * {@link ScopedResource#close()} is called.
 * <p>
 * Example of use:
 * <pre>{@code
 *   try (ScopedResource<Wire> sharedWire = sharedWireScopedThreadLocal.get()) {
 *     Wire wire = sharedWire.get();
 *     // ... do something with the wire
 *   } // it is returned for use by inner scopes here
 * }</pre>
 *
 * @param <T> The type of object contained in the pool
 */
public interface ScopedResourcePool<T> {

    /**
     * Get a scoped instance of the shared resource
     *
     * @return the {@link ScopedResource}, to be closed once it is finished being used
     */
    ScopedResource<T> get();
}
