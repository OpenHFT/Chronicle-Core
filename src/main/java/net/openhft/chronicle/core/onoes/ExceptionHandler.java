/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.onoes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * {@code ExceptionHandler} is a functional interface that provides a mechanism
 * for handling exceptions in a uniform way throughout an application.
 * It allows defining custom logic for exception handling depending on different levels and types
 * of exceptions. This can be particularly useful for logging or taking corrective action based on specific exceptions.
 *
 * <p>Implementations of this interface can provide custom strategies for handling exceptions,
 * such as logging them, ignoring them, or throwing them.
 *
 * <p>By default, exceptions are logged using SLF4J logging framework, however, this behavior can be overridden.
 */
@FunctionalInterface
public interface ExceptionHandler {

    /**
     * Creates an {@code ExceptionHandler} that ignores all exceptions.
     *
     * @return an instance of {@link NullExceptionHandler} which ignores all exceptions.
     */
    static ExceptionHandler ignoresEverything() {
        return NullExceptionHandler.NOTHING;
    }

    /**
     * Handles an exception occurred in a specific class, with a specific message.
     *
     * @param clazz  the class where the error occurred. Must not be null.
     * @param thrown the throwable instance representing the error.
     */
    default void on(@NotNull final Class<?> clazz, final Throwable thrown) {
        on(clazz, "", thrown);
    }

    /**
     * Handles an exception occurred in a specific class, with a specific message.
     *
     * @param clazz   the class where the error occurred. Must not be null.
     * @param message a custom message detailing the error.
     */
    default void on(@NotNull final Class<?> clazz, final String message) {
        on(clazz, message, null);
    }

    /**
     * The default method to call when an exception occurs.
     * It attempts to log the exception using SLF4J, and if this fails, it falls back to a secondary logging mechanism.
     *
     * @param clazz   the class where the exception occurred. Must not be null.
     * @param message a custom message providing additional information about the exception, or an empty string if not available.
     * @param thrown  the exception that needs to be handled, or null if there is no exception.
     */
    default void on(@NotNull Class<?> clazz, @Nullable String message, @Nullable Throwable thrown) {
        requireNonNull(clazz);
        try {
            on(LoggerFactory.getLogger(clazz), message, thrown);
        } catch (Throwable t) {
            try {
                Slf4jExceptionHandler.ERROR.on(clazz, "Unable to handle the exception, logging to SLF", t);
                Slf4jExceptionHandler.ERROR.on(clazz, message, thrown);
            } catch (Throwable t0) {
                t0.printStackTrace();
            }
        }
    }

    /**
     * Handles an exception occurred with a specific logger, message and throwable.
     *
     * @param logger  the logger instance to log the error. Must not be null.
     * @param message a custom message detailing the error, or null.
     * @param thrown  the throwable instance representing the error, or null.
     */
    void on(@NotNull Logger logger, @Nullable String message, @Nullable Throwable thrown);

    /**
     * Handles an exception occurred with a specific logger and message.
     *
     * @param logger  the logger instance to log the error. Must not be null.
     * @param message a custom message detailing the error, or null.
     */
    default void on(@NotNull Logger logger, @Nullable String message) {
        on(logger, message, null);
    }

    /**
     * Checks if the exception handler is enabled for the given class.
     *
     * @param aClass the class to check if the exception handler is enabled for. Must not be null.
     * @return true, as the exception handler is enabled by default for all classes.
     */
    default boolean isEnabled(@NotNull Class<?> aClass) {
        requireNonNull(aClass);
        return true;
    }

    /**
     * Retrieves the default underlying exception handler.
     *
     * @return the default exception handler, which is the current instance by default.
     */
    default ExceptionHandler defaultHandler() {
        return this;
    }
}
