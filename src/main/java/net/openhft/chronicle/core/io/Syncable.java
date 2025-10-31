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
package net.openhft.chronicle.core.io;

/**
 * Implement for resources that are able to flush data to the underlying
 * medium.  Memory-mapped files and some {@code Bytes} implementations provide
 * this facility.
 */
public interface Syncable {

    /**
     * Performs a sync operation if the supplied object implements
     * {@code Syncable}.  Use when the concrete type may or may not support
     * syncing.
     *
     * @param o object to sync if possible
     */
    static void syncIfAvailable(Object o) {
        if (o instanceof Syncable)
            ((Syncable) o).sync();
    }

    /**
     * Flush data to the backing store up to the point that this handle has read
     * or written. Some implementations may ignore this call.
     */
    void sync();
}
