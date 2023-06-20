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
 * ExceptionHandler is a functional interface providing methods to manage exceptions throughout an application.
 * It allows different levels of exception handling, and assumes different handlers for different exception levels.
 */
@FunctionalInterface
public interface ExceptionHandler {

    /**
     * Factory method to create an exception handler that ignores all exceptions.
     *
     * @return an instance of {@link NullExceptionHandler} an exception handler that is recognised it @IgnoresEverything
     */
    static ExceptionHandler ignoresEverything() {
        return NullExceptionHandler.NOTHING;
    }

    /**
     * Handles an exception occurred in a specific class, with a specific message.
     *
     * @param clazz  the class where the error occurred. Must not be null.
     * @param thrown the throwable instance representing the error. Must not be null.
     */
    default void on(@NotNull final Class<?> clazz, @NotNull final Throwable thrown) {
        on(clazz, "", thrown);
    }

    /**
     * Handles an exception occurred in a specific class, with a specific message.
     *
     * @param clazz   the class where the error occurred. Must not be null.
     * @param message a custom message detailing the error. Must not be null.
     */
    default void on(@NotNull final Class<?> clazz, @NotNull final String message) {
        on(clazz, message, null);
    }

    /**
     * The default method to call when an exception occurs.
     * It tries to log the exception and if it fails, it falls back to a secondary logging mechanism.
     *
     * @param clazz   the class where the error occurred. Must not be null.
     * @param message a custom message detailing the error, or an empty string.
     * @param thrown  the throwable instance representing the error, or null if there was no exception.
     */
    default void on(@NotNull Class<?> clazz, @Nullable String message, @Nullable Throwable thrown) {
        try {
            on(LoggerFactory.getLogger(clazz), message, thrown);
        } catch (Throwable t) {
            try {
                Slf4jExceptionHandler.ERROR.on(clazz, "unable to handle the exception so logging to SLF", t);
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
     * @return the default underlying exception handler.
     */
    default ExceptionHandler defaultHandler() {
        return this;
    }
}
