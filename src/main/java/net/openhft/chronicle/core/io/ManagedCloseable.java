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
 * This interface hides some methods which are more for expert lifecycle use but not strictly internal
 */
public interface ManagedCloseable extends Closeable {

    /**
     * Warns and closes the resource if it is not already closed.
     * This method is intended for expert lifecycle use but not strictly internal.
     * If resource tracing is enabled and the discard warning is not disabled,
     * a warning message is logged for the discarded resource.
     * The resource is then closed using {@link Closeable#closeQuietly(Object)}.
     */
    default void warnAndCloseIfNotClosed() {
        if (!isClosing()) {
            if (Jvm.isResourceTracing() && !DISABLE_DISCARD_WARNING) {
                ExceptionHandler warn =
                        Jvm.getBoolean("warnAndCloseIfNotClosed")
                                ? Jvm.warn()
                                : Slf4jExceptionHandler.WARN;
                warn.on(getClass(), "Discarded without closing " + this);
            }
            Closeable.closeQuietly(this);
        }
    }

    /**
     * Throws an exception if the resource is closed or closing.
     * This method is intended for expert lifecycle use but not strictly internal.
     * If the resource is closing, a {@link ClosedIllegalStateException} is thrown
     * with a message indicating whether the resource is already closed or closing.
     *
     * @throws IllegalStateException If the resource is closed or closing.
     */
    default void throwExceptionIfClosed() throws IllegalStateException {
        if (isClosing())
            throw new ClosedIllegalStateException(isClosed() ? "Closed" : "Closing");
    }

    /**
     * Returns the stack trace of where the resource was created.
     * This method is intended for expert lifecycle use but not strictly internal.
     * It returns null by default, indicating that the information is not available.
     *
     * @return The stack trace of where the resource was created, or null if not available.
     */
    default StackTrace createdHere() {
        return null;
    }
}
