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

package net.openhft.chronicle.core.threads;

/**
 * A runtime exception representing interruption of a thread.
 * <p>
 * This unchecked exception serves as an alternative to the checked {@link InterruptedException}.
 * Generally, it is recommended to use {@link InterruptedException} to handle interruptions.
 * However, in scenarios where handling checked exceptions is not feasible or desirable, this
 * class can be used to represent thread interruption without being subject to the checked
 * exception requirements.
 * <p>
 * When converting from {@link InterruptedException} to {@code InterruptedRuntimeException}, it is
 * important to preserve the interrupt status by calling {@link Thread#interrupt()} before
 * throwing this exception.
 * <p>
 * Example usage:
 * <pre>
 * try {
 *     // Code that may throw InterruptedException
 * } catch (InterruptedException e) {
 *     Thread.currentThread().interrupt(); // Preserve interrupt status
 *     throw new InterruptedRuntimeException("Thread was interrupted", e);
 * }
 * </pre>
 */
public class InterruptedRuntimeException extends IllegalStateException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs an {@code InterruptedRuntimeException} with no detail message or cause.
     */
    public InterruptedRuntimeException() {
    }

    /**
     * Constructs an {@code InterruptedRuntimeException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public InterruptedRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code InterruptedRuntimeException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically
     * incorporated into this exception's detail message.
     *
     * @param message the detail message.
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public InterruptedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code InterruptedRuntimeException} with the specified cause and a detail
     * message of {@code (cause == null ? null : cause.toString())} (which typically contains
     * the class and detail message of {@code cause}).
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *              A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public InterruptedRuntimeException(Throwable cause) {
        super(cause);
    }
}
