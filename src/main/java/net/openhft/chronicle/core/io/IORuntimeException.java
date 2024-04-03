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

/**
 * {@code IORuntimeException} is a runtime exception that is thrown when an operation
 * involving an underlying IO resource fails or is unable to complete.
 * <p>
 * This exception is often used to wrap checked exceptions related to IO operations,
 * such as {@code IOException}, into an unchecked exception. This is useful in contexts
 * where it is inconvenient to handle or propagate the checked exceptions.
 * 
 * <p>
 * The class also provides a utility method to convert general exceptions into {@code IORuntimeException},
 * specializing the exception as {@code ClosedIORuntimeException} if the underlying IO resource is closed.
 * 
 * <p>
 * Example usage:
 * <pre>
 * try {
 *     // Perform some IO operation
 * } catch (IOException e) {
 *     throw new IORuntimeException("Failed to perform the IO operation", e);
 * }
 * </pre>
 * 
 */
public class IORuntimeException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an {@code IORuntimeException} with the specified detail message.
     * The detail message is meant to provide more information on why the exception was thrown.
     *
     * @param message The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public IORuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code IORuntimeException} with the specified cause.
     * This constructor is typically used to wrap a lower-level exception.
     *
     * @param thrown The cause of the exception, which is saved for later retrieval by the {@link #getCause()} method.
     */
    public IORuntimeException(Throwable thrown) {
        super(thrown);
    }

    /**
     * Constructs an {@code IORuntimeException} with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param thrown  The cause of the exception.
     */
    public IORuntimeException(String message, Throwable thrown) {
        super(message, thrown);
    }

    /**
     * Creates a new {@code IORuntimeException} based on the provided exception.
     * If the exception indicates a closed IO resource, a {@code ClosedIORuntimeException} is created.
     * Otherwise, a regular {@code IORuntimeException} is created.
     *
     * @param e The exception to create the {@code IORuntimeException} from.
     * @return The created {@code IORuntimeException}, which could be a specialized {@code ClosedIORuntimeException} if the underlying resource is closed.
     */
    public static IORuntimeException newIORuntimeException(Exception e) {
        return IOTools.isClosedException(e)
                ? new ClosedIORuntimeException("Closed", e)
                : new IORuntimeException(e);
    }
}
