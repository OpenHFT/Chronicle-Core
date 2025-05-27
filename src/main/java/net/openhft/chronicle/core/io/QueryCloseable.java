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
package net.openhft.chronicle.core.io;
/**
 * Minimal contract to query the closing state of a resource.
 * It is used by {@link Closeable} and {@link ManagedCloseable}.
 */
public interface QueryCloseable {

    /**
     * Indicates whether the resource is in the process of closing.
     * This method should return {@code true} once {@link #isClosed()} does.
     *
     * @return {@code true} if {@code close()} has been invoked
     */
    default boolean isClosing() {
        return isClosed();
    }

    /**
     * Reports whether the resource has finished closing.
     *
     * @return {@code true} once the resource is closed
     */
    boolean isClosed();
}
