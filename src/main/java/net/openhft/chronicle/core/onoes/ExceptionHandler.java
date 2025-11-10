//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Strategy interface for pluggable exception handling, suitable for use with lambdas.
 *
 * <p>The overloads delegate to {@link #on(Class, String, Throwable)}. Implementations must
 * be non-blocking and re-entrant.</p>
 *
 * @see NullExceptionHandler
 * @see Slf4jExceptionHandler
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
     * Convenience overload delegating to {@link #on(Class, String, Throwable)} with an empty message.
     *
     * @param clazz  the class where the error occurred
     * @param thrown the throwable instance representing the error, may be {@code null}
     * @throws NullPointerException if {@code clazz} is {@code null}
     */
    default void on(@NotNull final Class<?> clazz, final Throwable thrown) {
        on(clazz, "", thrown);
    }

    /**
     * Convenience overload delegating to {@link #on(Class, String, Throwable)} with a {@code null} throwable.
     *
     * @param clazz   the class where the error occurred
     * @param message a custom message detailing the error, may be {@code null}
     * @throws NullPointerException if {@code clazz} is {@code null}
     */
    default void on(@NotNull final Class<?> clazz, final String message) {
        on(clazz, message, null);
    }

    /**
     * Handles an exception for the given class, message and throwable.
     * If logging fails this method attempts to log again at {@link Slf4jExceptionHandler#ERROR} level.
     *
     * @param clazz   the class where the exception occurred
     * @param message a custom message providing additional information or {@code null}
     * @param thrown  the exception that needs to be handled, may be {@code null}
     * @throws NullPointerException if {@code clazz} is {@code null}
     */
    @SuppressWarnings("java:S1181") // Catching Throwable is intentional to prevent user log handlers from crashing the reporting path.
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
     * Handles an exception with the given logger, message and throwable.
     *
     * @param logger  the logger used to record the error
     * @param message a custom message detailing the error, may be {@code null}
     * @param thrown  the throwable instance representing the error, may be {@code null}
     * @throws NullPointerException if {@code logger} is {@code null}
     */
    void on(@NotNull Logger logger, @Nullable String message, @Nullable Throwable thrown);

    /**
     * Convenience overload delegating to {@link #on(Logger, String, Throwable)} with a {@code null} throwable.
     *
     * @param logger  the logger used to record the error
     * @param message a custom message detailing the error, may be {@code null}
     * @throws NullPointerException if {@code logger} is {@code null}
     */
    default void on(@NotNull Logger logger, @Nullable String message) {
        on(logger, message, null);
    }

    /**
     * Checks if the exception handler is enabled for the given class.
     *
     * @param aClass the class to test
     * @return {@code true} if this handler should be invoked for the class
     * @throws NullPointerException if {@code aClass} is {@code null}
     */
    default boolean isEnabled(@NotNull Class<?> aClass) {
        requireNonNull(aClass);
        return true;
    }

    /**
     * Retrieves the default underlying exception handler.
     *
     * @return the default exception handler, usually {@code this}
     */
    default ExceptionHandler defaultHandler() {
        return this;
    }
}
