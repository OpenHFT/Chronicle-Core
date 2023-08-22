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

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.internal.CloseableUtils;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code Closeable} is a source or destination of data that can be closed. The close method
 * is invoked to release resources that the object is holding (such as open files).
 * This interface is an extension of {@link java.io.Closeable} and {@link QueryCloseable},
 * adding functionality for handling more resource types and closing them quietly without
 * throwing exceptions.
 * <p>
 * It is encouraged to use this interface in a try-with-resources statement.
 * 
 * <p>
 * Implementations of this interface should also consider extending {@link AbstractCloseable}
 * which provides common functionalities for closeable resources.
 * 
 */
public interface Closeable extends java.io.Closeable, QueryCloseable {

    /**
     * Closes multiple closeable objects quietly, without throwing exceptions.
     * If a closeable object is a collection or an array, all the elements within it are closed.
     * If a closeable object is a ServerSocketChannel, it is closed quietly.
     * <p>
     * Example:
     * <pre>
     * Closeable.closeQuietly(fileInputStream, socketChannel, listOfStreams);
     * </pre>
     * 
     *
     * @param closeables the array of objects to be closed
     * @see AbstractCloseable#performClose()
     */
    static void closeQuietly(@Nullable Object... closeables) {
        CloseableUtils.closeQuietly(closeables);
    }

    /**
     * Closes a single closeable object quietly, without throwing exceptions.
     * If the closeable object is a collection or an array, all the elements within it are closed.
     * If the closeable object is a ServerSocketChannel, it is closed quietly.
     * <p>
     * Example:
     * <pre>
     * Closeable.closeQuietly(fileInputStream);
     * </pre>
     * 
     *
     * @param o the object to be closed
     * @see AbstractCloseable#performClose()
     */
    static void closeQuietly(@Nullable Object o) {
        CloseableUtils.closeQuietly(o);
    }

    /**
     * Closes this resource, releasing any system resources associated with it.
     * If the resource is already closed, then invoking this method has no effect.
     * This method should be idempotent.
     * <p>
     * Subclasses should override {@link AbstractCloseable#performClose()} to provide
     * the actual close logic.
     * 
     *
     * @throws IllegalStateException If the resource cannot be closed.
     * @see AbstractCloseable#performClose()
     */
    @Override
    void close();
}
