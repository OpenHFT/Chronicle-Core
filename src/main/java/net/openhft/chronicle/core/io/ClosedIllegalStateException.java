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
 * {@code ClosedIllegalStateException} is thrown to indicate that a method has been invoked on a
 * resource that is in an inappropriate state because it has been closed. For example, attempting to
 * read from a file that has already been closed.
 * <p>
 * This exception is a specialized version of {@link IllegalStateException} specifically for cases
 * where the illegal state is due to the resource being closed. This makes the exception more
 * semantically meaningful when dealing with closeable resources.
 * </p>
 * <p>
 * Here's a typical example of how {@code ClosedIllegalStateException} might be used:
 * <pre>
 * public void readData() {
 *     if (isClosed()) {
 *         throw new ClosedIllegalStateException("Attempted to read from a closed resource.");
 *     }
 *     // ... read data ...
 * }
 * </pre>
 * </p>
 */
public class ClosedIllegalStateException extends IllegalStateException {

    /**
     * Constructs a {@code ClosedIllegalStateException} with the specified detail message.
     * The detail message is meant to provide more information on why the exception was thrown.
     *
     * @param s The detail message.
     */
    public ClosedIllegalStateException(String s) {
        super(s);
    }

    /**
     * Constructs a {@code ClosedIllegalStateException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically
     * incorporated into this exception's detail message.
     * </p>
     *
     * @param message The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ClosedIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
