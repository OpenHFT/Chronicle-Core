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

package net.openhft.chronicle.core.util;

/**
 * A runtime exception used to wrap a {@link ClassNotFoundException}.
 * <p>
 * This exception is typically used to convert a checked exception (ClassNotFoundException)
 * into an unchecked exception, allowing it to propagate up the call stack without the need
 * for explicit exception handling.
 *
 * @see RuntimeException
 * @see ClassNotFoundException
 */
public class ClassNotFoundRuntimeException extends RuntimeException {

    /**
     * Constructs a new runtime exception with the specified cause.
     *
     * @param cause The {@code ClassNotFoundException} to be wrapped by this runtime exception.
     */
    public ClassNotFoundRuntimeException(ClassNotFoundException cause) {
        super(cause);
    }

    /**
     * Returns the {@code ClassNotFoundException} that is the cause of this runtime exception.
     *
     * @return The {@code ClassNotFoundException} that is the cause of this runtime exception.
     */
    @Override
    public synchronized ClassNotFoundException getCause() {
        return (ClassNotFoundException) super.getCause();
    }
}
