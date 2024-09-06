/*
 * Copyright 2016-2020 chronicle.software
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

package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.IgnoresEverything;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * The {@code ThreadLocalisedExceptionHandler} class provides a thread-localized implementation of the {@link ExceptionHandler} interface.
 * This class allows different threads to have different {@link ExceptionHandler} instances. If a thread-local {@link ExceptionHandler}
 * is not set, it falls back to a default handler.
 *
 * <p>The default handler can be set using the {@link #defaultHandler(ExceptionHandler)} method or initialized during construction.
 * This class also ensures that the thread's interruption status is preserved when handling exceptions.
 */
public class ThreadLocalisedExceptionHandler implements ExceptionHandler {

    /**
     * The default {@link ExceptionHandler} used when no thread-local handler is set.
     */
    private ExceptionHandler eh;

    /**
     * A thread-local variable to hold the {@link ExceptionHandler} for each thread.
     */
    private ThreadLocal<ExceptionHandler> handlerTL;

    /**
     * Constructs a new {@code ThreadLocalisedExceptionHandler} with the specified default handler.
     *
     * @param handler the default {@link ExceptionHandler} to use when no thread-local handler is set.
     */
    public ThreadLocalisedExceptionHandler(ExceptionHandler handler) {
        eh = handler;
        resetThreadLocalHandler();
    }

    /**
     * Handles an exception by delegating to the appropriate {@link ExceptionHandler}.
     * If a thread-local handler is set, it is used; otherwise, the default handler is used.
     *
     * @param clazz   the class where the exception occurred. Must not be {@code null}.
     * @param message a custom message detailing the error, or {@code null}.
     * @param thrown  the {@link Throwable} instance representing the error, or {@code null}.
     */
    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, @Nullable Throwable thrown) {
        ExceptionHandler exceptionHandler = exceptionHandler();
        if (exceptionHandler == null)
            return;

        // Preserve the interrupted status of the thread
        boolean interrupted = Thread.interrupted();
        try {
            exceptionHandler.on(clazz, message, thrown);
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    /**
     * Handles an exception by delegating to the appropriate {@link ExceptionHandler}.
     * If a thread-local handler is set, it is used; otherwise, the default handler is used.
     *
     * @param logger  the {@link Logger} instance. Must not be {@code null}.
     * @param message a custom message detailing the error, or {@code null}.
     * @param thrown  the {@link Throwable} instance representing the error, or {@code null}.
     */
    @Override
    public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
        ExceptionHandler exceptionHandler = exceptionHandler();
        if (exceptionHandler == null || exceptionHandler instanceof IgnoresEverything)
            return;
        // Preserve the interrupted status of the thread
        boolean interrupted = Thread.interrupted();
        try {
            exceptionHandler.on(logger, message, thrown);
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the current {@link ExceptionHandler} for the thread.
     * If no thread-local handler is set, the default handler is returned.
     *
     * @return the current thread's {@link ExceptionHandler}, or the default handler if none is set.
     */
    private ExceptionHandler exceptionHandler() {
        ExceptionHandler exceptionHandler = handlerTL.get();
        if (exceptionHandler == null)
            exceptionHandler = eh;
        return exceptionHandler;
    }

    /**
     * Returns the default {@link ExceptionHandler}.
     *
     * @return the default {@link ExceptionHandler}.
     */
    public ExceptionHandler defaultHandler() {
        return eh;
    }

    /**
     * Unwraps the provided {@link ExceptionHandler} to its underlying handler if it is an instance of
     * {@link ThreadLocalisedExceptionHandler}. Otherwise, returns the provided handler.
     *
     * @param eh the {@link ExceptionHandler} to unwrap.
     * @return the unwrapped {@link ExceptionHandler}.
     */
    public static ExceptionHandler unwrap(ExceptionHandler eh) {
        if (eh instanceof ThreadLocalisedExceptionHandler)
            return ((ThreadLocalisedExceptionHandler) eh).exceptionHandler();
        return eh;
    }

    /**
     * Sets the default {@link ExceptionHandler} to the specified handler.
     *
     * <p>If the provided handler is {@code null}, a {@link NullExceptionHandler#NOTHING} is used instead.
     * If the provided handler is a {@link ChainedExceptionHandler}, it checks for recursive use of {@code ThreadLocalisedExceptionHandler}.
     *
     * @param defaultHandler the new default {@link ExceptionHandler}.
     * @return this {@code ThreadLocalisedExceptionHandler} instance.
     * @throws AssertionError if recursive use of {@code ThreadLocalisedExceptionHandler} is detected.
     */
    public ThreadLocalisedExceptionHandler defaultHandler(ExceptionHandler defaultHandler) {
        defaultHandler = unwrap(defaultHandler);
        if (defaultHandler instanceof ChainedExceptionHandler) {
            ChainedExceptionHandler ceh = (ChainedExceptionHandler) defaultHandler;
            for (ExceptionHandler handler : ceh.chain()) {
                if (handler instanceof ThreadLocalisedExceptionHandler)
                    throw new AssertionError("Recursive use of " + getClass());
            }
        }
        this.eh = defaultHandler == null ? NullExceptionHandler.NOTHING : defaultHandler;
        return this;
    }

    /**
     * Returns the thread-local {@link ExceptionHandler} for the current thread.
     *
     * @return the thread-local {@link ExceptionHandler}, or {@code null} if none is set.
     */
    public ExceptionHandler threadLocalHandler() {
        return handlerTL.get();
    }

    /**
     * Sets the thread-local {@link ExceptionHandler} for the current thread to the specified handler.
     *
     * @param handler the new thread-local {@link ExceptionHandler}.
     * @return this {@code ThreadLocalisedExceptionHandler} instance.
     */
    public ThreadLocalisedExceptionHandler threadLocalHandler(ExceptionHandler handler) {
        handlerTL.set(handler);
        return this;
    }

    /**
     * Resets the thread-local handler to a new {@link InheritableThreadLocal} instance.
     */
    public void resetThreadLocalHandler() {
        handlerTL = new InheritableThreadLocal<>();
    }

    /**
     * Checks if the current exception handling is enabled for the specified class.
     * It delegates the check to the appropriate {@link ExceptionHandler}.
     *
     * @param aClass the class to check.
     * @return {@code true} if exception handling is enabled for the specified class; {@code false} otherwise.
     */
    @Override
    public boolean isEnabled(@NotNull Class<?> aClass) {
        ExceptionHandler exceptionHandler = exceptionHandler();
        if (exceptionHandler == null)
            return true;
        return exceptionHandler.isEnabled(aClass);
    }
}
