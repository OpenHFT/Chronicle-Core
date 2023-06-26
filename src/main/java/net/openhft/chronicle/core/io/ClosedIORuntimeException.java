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
 * {@code ClosedIORuntimeException} is thrown to indicate that an I/O operation has been
 * attempted on a closed I/O resource, such as a file or network connection. This exception
 * is used when the state of the resource renders it unsuitable for the requested operation.
 * <p>
 * This exception is a specialized subclass of {@link IORuntimeException}, which is an unchecked
 * wrapper for the standard checked {@link java.io.IOException}. {@code ClosedIORuntimeException}
 * specifically targets scenarios where the illegal operation is due to the underlying resource
 * being closed.
 * </p>
 * <p>
 * {@code ClosedIORuntimeException} is used to report a runtime I/O exception that arises
 * from the closed state of the resource, without requiring the method to declare it in its
 * {@code throws} clause. This is useful in situations where reporting the exception is necessary
 * but forcing the calling code to catch it is not desired.
 * </p>
 * <p>
 * Here's an example of how {@code ClosedIORuntimeException} might be used:
 * <pre>
 * public void writeToStream(OutputStream stream, byte[] data) {
 *   try {
 *     stream.write(data);
 *   } catch (IOException e) {
 *     if (stream.isClosed())
 *       throw new ClosedIORuntimeException("Stream is closed", e);
 *     throw new IORuntimeException("Failed to write to stream", e);
 *   }
 * }
 * </pre>
 * </p>
 */
public class ClosedIORuntimeException extends IORuntimeException {

    /**
     * Constructs a {@code ClosedIORuntimeException} with the specified detail message.
     * The detail message is intended to provide more information on why the exception was thrown.
     *
     * @param message The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public ClosedIORuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code ClosedIORuntimeException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically
     * incorporated into this exception's detail message.
     * </p>
     *
     * @param message The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     * @param thrown  The cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                A {@code null} value is permitted and indicates that the cause is nonexistent or unknown.
     */
    public ClosedIORuntimeException(String message, Throwable thrown) {
        super(message, thrown);
    }

}
