/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThreadLocalisedExceptionHandler implements ExceptionHandler {
    private ExceptionHandler defaultHandler;
    private ThreadLocal<ExceptionHandler> handlerTL;

    public ThreadLocalisedExceptionHandler(Slf4jExceptionHandler handler) {
        defaultHandler = handler;
        resetThreadLocalHandler();
    }

    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, Throwable thrown) {
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

    private ExceptionHandler exceptionHandler() {
        ExceptionHandler exceptionHandler = handlerTL.get();
        if (exceptionHandler == null)
            exceptionHandler = defaultHandler;
        return exceptionHandler;
    }

    public ExceptionHandler defaultHandler() {
        return defaultHandler;
    }

    public ThreadLocalisedExceptionHandler defaultHandler(ExceptionHandler defaultHandler) {
        if (defaultHandler instanceof ThreadLocalisedExceptionHandler)
            defaultHandler = ((ThreadLocalisedExceptionHandler) defaultHandler).exceptionHandler();
        this.defaultHandler = defaultHandler == null ? NullExceptionHandler.NOTHING : defaultHandler;
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
