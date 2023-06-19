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
 * Exception thrown when an operation is attempted on a closed resource.
 * This exception is a subclass of IllegalStateException.
 */
public class ClosedIllegalStateException extends IllegalStateException {

    /**
     * Constructs a ClosedIllegalStateException with the specified detail message.
     *
     * @param s The detail message.
     */
    public ClosedIllegalStateException(String s) {
        super(s);
    }

    /**
     * Constructs a ClosedIllegalStateException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause of the exception.
     */
    public ClosedIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
