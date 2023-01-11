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

import net.openhft.chronicle.core.ClassLocal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Slf4jExceptionHandler implements ExceptionHandler {
    ERROR {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            logger.error(message, thrown);
        }
    },
    WARN {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            logger.warn(message, thrown);
        }
    },
    PERF {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            logger.info(message, thrown);
        }
    },
    DEBUG {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            logger.debug(message, thrown);
        }

        @Override
        public boolean isEnabled(@NotNull Class<?> clazz) {
            return getLogger(clazz).isDebugEnabled();
        }
    };

    static Logger getLogger(Class<?> clazz) {
        return CLASS_LOGGER.get(clazz);
    }

    static final ClassLocal<Logger> CLASS_LOGGER = ClassLocal.withInitial(LoggerFactory::getLogger);

    public static Slf4jExceptionHandler valueOf(LogLevel logLevel) {
        if (logLevel == LogLevel.ERROR)
            return ERROR;
        if (logLevel == LogLevel.WARN)
            return WARN;
        return DEBUG;
    }

    private static boolean isJUnitTest() {

        for (StackTraceElement[] stackTrace : Thread.getAllStackTraces().values()) {
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().contains(".junit")) {
                    return true;
                }
            }
        }
        return false;
    }
}
