/*
 * Copyright 2016-2025 chronicle.software
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
 * Provides a per-thread {@link ExceptionHandler}.
 *
 * <p>The handler supplied to the constructor becomes the default. Each thread may
 * override this via {@link #threadLocalHandler(ExceptionHandler)} and the override can
 * be removed with {@link #resetThreadLocalHandler()}.
 * Nested {@link ChainedExceptionHandler} instances are rejected by
 * {@link #defaultHandler(ExceptionHandler)}.
 *
 * <p>This class is <em>conditionally thread-safe</em>: the default handler is shared while
 * per-thread handlers are isolated using {@link ThreadLocal}.
 *
 * @see ChainedExceptionHandler
 * @since 3.25ea
 */
public class ThreadLocalisedExceptionHandler implements ExceptionHandler {
    private ExceptionHandler eh;
    private ThreadLocal<ExceptionHandler> handlerTL;

    /**
     * Creates a new instance using the supplied handler as the default.
     *
     * @param handler the handler used when no thread-local handler is present
     */
    @SuppressWarnings("this-escape")
    public ThreadLocalisedExceptionHandler(ExceptionHandler handler) {
        eh = handler;
        resetThreadLocalHandler();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec The interrupt status is cleared before delegation and restored afterwards.
     */
    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, @Nullable Throwable thrown) {
        ExceptionHandler exceptionHandler = exceptionHandler();
        if (exceptionHandler == null)
            return;
        boolean interrupted = Thread.interrupted();
        try {
            exceptionHandler.on(clazz, message, thrown);
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec The interrupt status is cleared before delegation and restored afterwards.
     */
    @Override
    public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
        ExceptionHandler exceptionHandler = exceptionHandler();
        if (exceptionHandler == null || exceptionHandler instanceof IgnoresEverything)
            return;
        boolean interrupted = Thread.interrupted();
        try {
            exceptionHandler.on(logger, message, thrown);
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    private ExceptionHandler exceptionHandler() {
        ExceptionHandler exceptionHandler = handlerTL.get();
        if (exceptionHandler == null)
            exceptionHandler = eh;
        return exceptionHandler;
    }

    /**
     * Returns the current default handler.
     *
     * @return the default handler in use
     */
    public ExceptionHandler defaultHandler() {
        return eh;
    }

    /**
     * Unwraps the supplied handler if it is an instance of this class.
     *
     * @param eh the handler to unwrap
     * @return the underlying handler or the supplied instance if it is not wrapped
     */
    public static ExceptionHandler unwrap(ExceptionHandler eh) {
        if (eh instanceof ThreadLocalisedExceptionHandler)
            return ((ThreadLocalisedExceptionHandler) eh).exceptionHandler();
        return eh;
    }

    /**
     * Sets the default handler to use when no thread-local handler is present.
     * Nested {@link ChainedExceptionHandler} instances are rejected.
     *
     * @param defaultHandler the new default handler, or {@code null} to use
     *                       {@link NullExceptionHandler#NOTHING}
     * @return {@code this} for chaining
     */
    public ThreadLocalisedExceptionHandler defaultHandler(ExceptionHandler defaultHandler) {
        defaultHandler = unwrap(defaultHandler);
        if (defaultHandler instanceof ChainedExceptionHandler) {
            ChainedExceptionHandler ceh = (ChainedExceptionHandler) defaultHandler;
            for (ExceptionHandler handler : ceh.chain()) {
                if (handler instanceof ThreadLocalisedExceptionHandler)
                    throw new AssertionError("Recursive used of " + getClass());
            }
        }
        this.eh = defaultHandler == null ? NullExceptionHandler.NOTHING : defaultHandler;
        return this;
    }

    /**
     * Returns the handler specific to the current thread or {@code null} if none is set.
     *
     * @return the thread-local handler or {@code null}
     */
    public ExceptionHandler threadLocalHandler() {
        return handlerTL.get();
    }

    /**
     * Overrides the handler for the current thread.
     *
     * @param handler the handler to install for this thread, may be {@code null}
     * @return {@code this} for chaining
     */
    public ThreadLocalisedExceptionHandler threadLocalHandler(ExceptionHandler handler) {
        handlerTL.set(handler);
        return this;
    }

    /**
     * Clears any thread specific handler so the default will be used.
     */
    public void resetThreadLocalHandler() {
        handlerTL = new InheritableThreadLocal<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(@NotNull Class<?> aClass) {
        ExceptionHandler exceptionHandler = exceptionHandler();
        if (exceptionHandler == null)
            return true;
        return exceptionHandler.isEnabled(aClass);
    }
}
