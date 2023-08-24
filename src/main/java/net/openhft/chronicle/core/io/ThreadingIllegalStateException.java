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
 * A custom exception indicating an illegal access or operation due to threading constraints.
 * <p>
 * This exception is typically thrown when a component, which is not designed to be thread-safe,
 * is accessed or modified by multiple threads concurrently. It provides additional context
 * by encapsulating the offending sequence of operations, usually through a stack trace.
 */
public class ThreadingIllegalStateException extends IllegalStateException {

    /**
     * Constructs a new {@code ThreadingIllegalStateException} with the specified detail
     * message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *                {@link Throwable#getMessage()} method).
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method). A {@code null} value is
     *                permitted and indicates that the cause is nonexistent or unknown.
     */
    public ThreadingIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
