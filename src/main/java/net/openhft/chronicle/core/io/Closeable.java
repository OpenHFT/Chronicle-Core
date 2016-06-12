/*
 * Copyright 2016 higherfrequencytrading.com
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

import org.slf4j.LoggerFactory;

import java.io.IOException;

@FunctionalInterface
public interface Closeable extends java.io.Closeable {
    static void closeQuietly(Object o) {
        if (o instanceof Object[]) {
            for (Object o2 : (Object[]) o) {
                closeQuietly(o2);
            }
        } else if (o instanceof java.io.Closeable) {
            try {
                ((java.io.Closeable) o).close();
            } catch (IOException e) {
                LoggerFactory.getLogger(Closeable.class).debug("", e);
            }
        }
    }

    /**
     * Doesn't throw a checked exception.
     */
    void close();

    default void notifyClosing() {
        // take an action before everything else closes.
    }

    default boolean isClosed() {
        throw new UnsupportedOperationException("todo");
    }
}
