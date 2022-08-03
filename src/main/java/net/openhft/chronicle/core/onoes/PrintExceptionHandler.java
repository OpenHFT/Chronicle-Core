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

import java.io.PrintStream;
import java.time.LocalDateTime;

public enum PrintExceptionHandler implements ExceptionHandler {
    ERR(System.err) {
        @Override
        public void on(@NotNull Class<?> clazz, @Nullable String message, Throwable thrown) {
            printLog(clazz, message, thrown, this);
        }
    },
    OUT(System.out) {
        @Override
        public void on(@NotNull Class<?> clazz, @Nullable String message, Throwable thrown) {
            printLog(clazz, message, thrown, this);
        }
    };

    public static final PrintExceptionHandler WARN = ERR;
    public static final PrintExceptionHandler DEBUG = OUT;

    PrintExceptionHandler(final PrintStream printStream) {
        this.printStream = printStream;
    }

    private final PrintStream printStream;

    private static void printLog(@NotNull final Class<?> clazz,
                                 final String message,
                                 @Nullable final Throwable thrown,
                                 final PrintExceptionHandler exceptionHandler) {
        final boolean interrupted = Thread.interrupted();
        try {
            synchronized (exceptionHandler.printStream) {
                exceptionHandler.printStream.print(LocalDateTime.now() + " " + Thread.currentThread().getName() + " " + clazz.getSimpleName() + " " + message);
                if (thrown != null)
                    thrown.printStackTrace(exceptionHandler.printStream);
                else
                    exceptionHandler.printStream.println();
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }
}
