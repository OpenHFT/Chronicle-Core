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
 * A RuntimeException triggered when a underlying IO resource throws an exception.
 */
public class IORuntimeException extends RuntimeException {

    /**
     * Constructs an IORuntimeException with the specified detail message.
     *
     * @param message The detail message.
     */
    public IORuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs an IORuntimeException with the specified cause.
     *
     * @param thrown The cause of the exception.
     */
    public IORuntimeException(Throwable thrown) {
        super(thrown);
    }

    /**
     * Constructs an IORuntimeException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param thrown  The cause of the exception.
     */
    public IORuntimeException(String message, Throwable thrown) {
        super(message, thrown);
    }

    /**
     * Creates a new IORuntimeException based on the provided Exception.
     * If the Exception indicates a closed IO resource, a ClosedIORuntimeException is created.
     * Otherwise, a regular IORuntimeException is created.
     *
     * @param e The Exception to create the IORuntimeException from.
     * @return The created IORuntimeException.
     */
    public static IORuntimeException newIORuntimeException(Exception e) {
        return IOTools.isClosedException(e)
                ? new ClosedIORuntimeException("Closed", e)
                : new IORuntimeException(e);
    }
}