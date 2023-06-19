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
 * This class provides a thread-localized ExceptionHandler. The actual ExceptionHandler used for handling
 * exceptions can vary per thread and can be configured using the threadLocalHandler method.
 * If no thread-local ExceptionHandler has been set, it will fall back to a default ExceptionHandler.
 * <p>
 * The default ExceptionHandler can be set using the defaultHandler method, and is initially passed to
 * the constructor when creating a new instance of this class.
 */
public class ThreadLocalisedExceptionHandler implements ExceptionHandler {
    private ExceptionHandler eh;
    private ThreadLocal<ExceptionHandler> handlerTL;

    public ThreadLocalisedExceptionHandler(ExceptionHandler handler) {
        eh = handler;
        resetThreadLocalHandler();
    }

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

    public ExceptionHandler defaultHandler() {
        return eh;
    }

    public static ExceptionHandler unwrap(ExceptionHandler eh) {
        if (eh instanceof ThreadLocalisedExceptionHandler)
            return ((ThreadLocalisedExceptionHandler) eh).exceptionHandler();
        return eh;
    }

    public ThreadLocalisedExceptionHandler defaultHandler(ExceptionHandler defaultHandler) {
        defaultHandler = unwrap(defaultHandler);
        if (defaultHandler instanceof ChainedExceptionHandler) {
            ChainedExceptionHandler ceh = (ChainedExceptionHandler) defaultHandler;
            for (ExceptionHandler handler : ceh.chain()) {
                if (handler instanceof ThreadLocalisedExceptionHandler)
                    throw new AssertionError("Recursive used of "+getClass());
            }
        }
        this.eh = defaultHandler == null ? NullExceptionHandler.NOTHING : defaultHandler;
        return this;
    }

    public ExceptionHandler threadLocalHandler() {
        return handlerTL.get();
    }

    public ThreadLocalisedExceptionHandler threadLocalHandler(ExceptionHandler handler) {
        handlerTL.set(handler);
        return this;
    }

    public void resetThreadLocalHandler() {
        handlerTL = new InheritableThreadLocal<>();
    }

    @Override
    public boolean isEnabled(@NotNull Class<?> aClass) {
        ExceptionHandler exceptionHandler = exceptionHandler();
        if (exceptionHandler == null)
            return true;
        return exceptionHandler.isEnabled(aClass);
    }
}
