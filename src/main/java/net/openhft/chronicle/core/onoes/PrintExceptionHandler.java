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

import java.io.PrintStream;
import java.time.LocalDateTime;

/**
 * PrintExceptionHandler is an enumeration implementing the ExceptionHandler interface.
 * It is used to log exceptions to either the standard output or standard error.
 *
 * <ul>
 *     <li>{@link #ERR} - Logs to the standard error stream.</li>
 *     <li>{@link #OUT} - Logs to the standard output stream.</li>
 * </ul>
 * <p>
 * Additionally, it provides two static instances, {@link #WARN} and {@link #DEBUG},
 * which correspond to {@link #ERR} and {@link #OUT} respectively.
 */
public enum PrintExceptionHandler implements ExceptionHandler {
    ERR(System.err) {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            printLog(logger, message, thrown, this);
        }
    },
    OUT(System.out) {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            printLog(logger, message, thrown, this);
        }
    };

    public static final PrintExceptionHandler WARN = ERR;
    public static final PrintExceptionHandler DEBUG = OUT;

    private final PrintStream printStream;

    /**
     * Constructs an instance of PrintExceptionHandler with the specified PrintStream.
     *
     * @param printStream the PrintStream to which the handler will log.
     */
    PrintExceptionHandler(final PrintStream printStream) {
        this.printStream = printStream;
    }

    /**
     * Prints a log message to the associated PrintStream.
     * If a throwable is provided, its stack trace is also printed.
     *
     * @param logger           the logger instance. Must not be null.
     * @param message          a custom message detailing the error, or null.
     * @param thrown           the throwable instance representing the error, or null.
     * @param exceptionHandler the instance of PrintExceptionHandler which will output the log message.
     */
    private static void printLog(@NotNull final Logger logger,
                                 final String message,
                                 @Nullable final Throwable thrown,
                                 final PrintExceptionHandler exceptionHandler) {
        final boolean interrupted = Thread.interrupted();
        try {
            synchronized (exceptionHandler.printStream) {
                exceptionHandler.printStream.print(LocalDateTime.now() + " " + Thread.currentThread().getName() + " " + logger.getName() + " " + message);
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
