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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

import static net.openhft.chronicle.core.io.AbstractCloseable.DISABLE_DISCARD_WARNING;

/**
 * The {@code ManagedCloseable} interface extends the {@link Closeable} interface and provides additional methods
 * that are primarily intended for expert use cases involving resource lifecycle management.
 * <p>
 * This interface is designed for scenarios where more fine-grained control over the closing process of a resource
 * is needed, or where it is necessary to perform advanced operations based on the state of the resource.
 */
public interface ManagedCloseable extends Closeable {

    /**
     * Closes the resource if it is not already closed, and logs a warning if the resource was discarded without being closed.
     * This method is intended for advanced use cases in resource lifecycle management.
     * <p>
     * When resource tracing is enabled and discard warnings are not disabled, a warning message is logged indicating that
     * the resource was discarded without being properly closed. The resource is then closed quietly, meaning that no
     * exception is thrown if an error occurs during the closing process.
     * 
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
     * @throws ThreadingIllegalStateException       If the thread safety check fails.
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
     *
     * @return The stack trace of the location where the resource was created, or {@code null} if the information is not available.
     */
    // TODO move implementation to sub-classes in x.24
    default StackTrace createdHere() {
        return null;
    }
}
