/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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
    ERR {
        @Override
        public void on(@NotNull Class clazz, String message, Throwable thrown) {
            printLog(clazz, message, thrown, System.err);
        }
    },
    OUT {
        @Override
        public void on(@NotNull Class clazz, String message, Throwable thrown) {
            printLog(clazz, message, thrown, System.out);
        }
    };

    public static final PrintExceptionHandler WARN = ERR;
    public static final PrintExceptionHandler DEBUG = OUT;

    private static void printLog(@NotNull Class clazz, String message, @Nullable Throwable thrown, PrintStream stream) {
        boolean interrupted = Thread.interrupted();
        try {
        synchronized (stream) {
            stream.print(LocalDateTime.now() + " " + Thread.currentThread().getName() + " " + clazz.getSimpleName() + " " + message);
            if (thrown != null)
                thrown.printStackTrace(stream);
            else
                stream.println();
        }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }
}
