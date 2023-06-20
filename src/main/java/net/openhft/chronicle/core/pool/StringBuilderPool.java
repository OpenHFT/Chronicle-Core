/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

package net.openhft.chronicle.core.pool;

import static java.lang.ThreadLocal.withInitial;

/**
 * This class provides a pool of StringBuilder objects for efficient string building operations.
 * Each thread gets its own StringBuilder instance via a ThreadLocal,
 * ensuring thread-safety while avoiding synchronization overhead.
 */
public class StringBuilderPool {
    /**
     * Thread-local variable that holds a StringBuilder for each thread.
     * The initial capacity for each StringBuilder is 128.
     */
    private final ThreadLocal<StringBuilder> sbtl = withInitial(
            () -> new StringBuilder(128));

    /**
     * Returns a StringBuilder instance from the pool.
     * If the current thread does not yet have a StringBuilder, it is created and added to the pool.
     * The length of the StringBuilder is reset to 0 before being returned.
     *
     * @return a StringBuilder instance with length 0.
     */
    public StringBuilder acquireStringBuilder() {
        StringBuilder sb = sbtl.get();
        sb.setLength(0);
        return sb;
    }
}
