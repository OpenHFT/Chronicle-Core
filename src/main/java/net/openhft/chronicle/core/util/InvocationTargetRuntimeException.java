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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * The {@code InvocationTargetRuntimeException} class is a custom runtime exception
 * that is thrown when a method's invocation on the target object throws an exception.
 * This class helps in wrapping the exception thrown by the target method
 * into an unchecked exception.
 * <p>
 * For example, if a reflective method invocation through {@code java.lang.reflect.Method}
 * throws an exception, this class can be used to wrap and rethrow it as a
 * runtime exception.
 * <p>
 * This is useful in scenarios where the client code invoking the method
 * is not expecting or is not capable of handling checked exceptions thrown
 * by the method.
 *
 * @see InvocationTargetException
 */
public class InvocationTargetRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new {@code InvocationTargetRuntimeException} with the
     * specified cause. If the cause is an instance of
     * {@link InvocationTargetException}, the actual exception thrown by
     * the target method is extracted and set as the cause for this exception.
     * Otherwise, the supplied {@code cause} is set as the cause.
     *
     * @param cause The cause of this exception. This is usually the exception
     *              thrown by the method that was invoked.
     */
    public InvocationTargetRuntimeException(@NotNull Throwable cause) {
        super(cause instanceof InvocationTargetException ? cause.getCause() : cause);
    }
}
