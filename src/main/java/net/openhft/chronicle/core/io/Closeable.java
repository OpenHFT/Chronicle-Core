/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
@SuppressWarnings("java:S2176")
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
