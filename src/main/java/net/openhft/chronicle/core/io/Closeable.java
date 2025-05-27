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

import net.openhft.chronicle.core.internal.CloseableUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of {@link java.io.Closeable} that participates in the Chronicle
 * resource lifecycle.
 * <p>
 * Typical usage is via <em>try-with-resources</em> where {@link #close()} is
 * invoked automatically. Implementations normally extend
 * {@link AbstractCloseable} to delegate their cleanup to
 * {@link AbstractCloseable#performClose()}.
 * <p>
 * Thread safety is implementation specific; callers should assume instances are
 * not thread-safe unless stated otherwise.
 */
public interface Closeable extends java.io.Closeable, QueryCloseable {

    /**
     * Close the supplied resources without propagating any exception.
     * Elements of arrays or collections are closed recursively.
     * <pre>
     * Closeable.closeQuietly(in, socket);
     * </pre>
     *
     * @param closeables resources to close
     * @see AbstractCloseable#performClose()
     */
    static void closeQuietly(@Nullable Object... closeables) {
        CloseableUtils.closeQuietly(closeables);
    }

    /**
     * Variant for a single resource.
     * Arrays, collections and {@link java.nio.channels.ServerSocketChannel}
     * are handled transparently.
     *
     * @param o resource to close
     * @see AbstractCloseable#performClose()
     */
    static void closeQuietly(@Nullable Object o) {
        CloseableUtils.closeQuietly(o);
    }

    /**
     * Release any underlying resources. The call is idempotent and may be made
     * from a try-with-resources block.
     *
     * @throws IllegalStateException if the resource refuses to close
     * @see AbstractCloseable#performClose()
     */
    @Override
    void close();
}
