//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

import static net.openhft.chronicle.core.io.AbstractCloseable.DISABLE_DISCARD_WARNING;

/**
 * Adds expert lifecycle hooks to {@link Closeable}.
 * Implementations may inspect their creation site and warn when discarded
 * without being closed. Users typically interact with such resources via
 * {@code try}-with-resources.
 * <p>
 * Thread safety is left to the concrete implementation.
 */
public interface ManagedCloseable extends Closeable {

    /**
     * Closes the resource if it is not already closed, and logs a warning if the resource was discarded without being closed.
     * This method is intended for advanced use cases in resource lifecycle management.
     * <p>
     * When resource tracing is enabled and discard warnings are not disabled, a warning message is logged indicating that
     * the resource was discarded without being properly closed. The resource is then closed quietly, meaning that no
     * exception is thrown if an error occurs during the closing process.
     */
    // TODO move implementation to sub-classes in x.24
    default void warnAndCloseIfNotClosed() {
        if (!isClosing()) {
            if (Jvm.isResourceTracing() && !DISABLE_DISCARD_WARNING) {
                ExceptionHandler warn = Jvm.getBoolean("warnAndCloseIfNotClosed") ? Jvm.warn() : Slf4jExceptionHandler.WARN;
                warn.on(getClass(), "Discarded without closing " + this);
            }
            Closeable.closeQuietly(this);
        }
    }

    /**
     * Throws an exception if the resource is closed or in the process of closing.
     * This method is intended for advanced use cases in resource lifecycle management.
     * <p>
     * If the resource is in the process of closing, a {@link ClosedIllegalStateException} is thrown.
     * The exception message indicates whether the resource is already closed or is currently in the process of closing.
     *
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the thread safety check fails.
     */
    default void throwExceptionIfClosed() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        if (isClosing())
            throw new ClosedIllegalStateException(isClosed() ? "Closed" : "Closing");
    }

    /**
     * Returns the stack trace of the location where the resource was created.
     * This method is intended for advanced use cases in resource lifecycle management.
     * <p>
     * By default, this method returns {@code null}, indicating that the information is not available.
     * Implementations may override this method to provide the actual stack trace where the resource was created.
     *
     * @return The stack trace of the location where the resource was created, or {@code null} if the information is not available.
     */
    // TODO move implementation to sub-classes in x.24
    default StackTrace createdHere() {
        return null;
    }
}
