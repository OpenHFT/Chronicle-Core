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

/**
 * Slf4jExceptionHandler is an enum implementation of the ExceptionHandler interface.
 * It uses SLF4J (Simple Logging Facade for Java) to handle exceptions based on the level of logging severity.
 * It supports four levels of logging severity: ERROR, WARN, PERF and DEBUG.
 * <p>
 * Each instance of Slf4jExceptionHandler logs at a specific level and corresponds to a LogLevel enum.
 * This is used to map LogLevel enums to their corresponding Slf4jExceptionHandler instances via the valueOf(LogLevel logLevel) method.
 * <p>
 * The DEBUG instance also overrides the {@code isEnabled(Class clazz)} method, using the isDebugEnabled() method from SLF4J's Logger class.
 * <p>
 * There's also a utility method isJUnitTest() which is used to detect if the current execution context is a JUnit test.
 */
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

    /**
     * Returns the appropriate Slf4jExceptionHandler value based on the given LogLevel.
     *
     * @param logLevel the LogLevel enum to convert.
     * @return the corresponding Slf4jExceptionHandler value.
     */
    public static Slf4jExceptionHandler valueOf(LogLevel logLevel) {
        if (logLevel == LogLevel.ERROR)
            return ERROR;
        if (logLevel == LogLevel.WARN)
            return WARN;
        if (logLevel == LogLevel.PERF)
            return PERF;
        return DEBUG;
    }
}
