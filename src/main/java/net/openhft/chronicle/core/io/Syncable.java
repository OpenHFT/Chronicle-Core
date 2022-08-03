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

package net.openhft.chronicle.core.io;

/**
 * Pass a hint that now would be a good time to sync to underlying media if supported.
 */
public interface Syncable {
    /**
     * Perform a sync up to the point that this handle has read or written. There might be data beyond this point that isn't sync-ed.
     * It might not do anything depending on whether this is supported or turned off through configuration.
     */
    void sync();

    static void syncIfAvailable(Object o) {
        if (o instanceof Syncable)
            ((Syncable) o).sync();
    }
}
