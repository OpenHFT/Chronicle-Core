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

import java.io.IOException;
import java.lang.ref.Reference;
import java.net.HttpURLConnection;
import java.nio.channels.ServerSocketChannel;
import java.util.Collection;
import java.util.*;

/**
 * A resource that must be closed when it is no longer needed.
 */
public interface Closeable extends java.io.Closeable, QueryCloseable {

    /**
     * Close a closeable quietly, i.e. without throwing an exception.
     * If the closeable is a collection, close all the elements.
     * If the closeable is an array, close all the elements.
     * If the closeable is a ServerSocketChannel, close it quietly.
     *
     * @param closeables the objects to close
     */
    static void closeQuietly(@Nullable Object... closeables) {
        CloseableUtils.closeQuietly(closeables);
    }

    /**
     * Close a closeable quietly, i.e. without throwing an exception.
     * If the closeable is a collection, close all the elements.
     * If the closeable is an array, close all the elements.
     * If the closeable is a ServerSocketChannel, close it quietly.
     *
     * @param o the object to close
     */
    static void closeQuietly(@Nullable Object o) {
        CloseableUtils.closeQuietly(o);
    }

    /**
     * Closes this resource, potentially preventing parts of it from being used again
     * and potentially relinquishing resources held.
     * <p>
     * This method is idem-potent.
     *
     * @throws IllegalStateException if the resource cannot be closed.
     */
    @Override
    void close();
}
