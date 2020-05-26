/*
 * Copyright 2016-2020 Chronicle Software
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
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public enum Slf4jExceptionHandler implements ExceptionHandler {
    FATAL {
        @Override
        public void on(@NotNull Class clazz, String message, Throwable thrown) {
            LoggerFactory.getLogger(clazz).error("FATAL error " + message, thrown);
            if (!Slf4jExceptionHandler.isJUnitTest()) {
                System.exit(-1);
            }
        }
    },
    WARN {
        @Override
        public void on(@NotNull Class clazz, String message, Throwable thrown) {
            LoggerFactory.getLogger(clazz).warn(message, thrown);
        }
    },
    PERF {
        @Override
        public void on(@NotNull Class clazz, String message, Throwable thrown) {
            LoggerFactory.getLogger(clazz).warn(message, thrown);
        }
    },
    DEBUG {
        @Override
        public void on(@NotNull Class clazz, String message, Throwable thrown) {
            LoggerFactory.getLogger(clazz).debug(message, thrown);
        }

        @Override
        public boolean isEnabled(Class clazz) {
            return LoggerFactory.getLogger(clazz).isDebugEnabled();
        }
    };

    public static Slf4jExceptionHandler valueOf(LogLevel logLevel) {
        if (logLevel == LogLevel.FATAL)
            return FATAL;
        if (logLevel == LogLevel.WARN)
            return WARN;
        return DEBUG;
    }

    private static boolean isJUnitTest() {

        for (StackTraceElement[] stackTrace : Thread.getAllStackTraces().values()) {

            List<StackTraceElement> list = Arrays.asList(stackTrace);
            for (StackTraceElement element : list) {
                if (element.getClassName().contains(".junit")) {
                    return true;
                }
            }
        }
        return false;
    }
}
