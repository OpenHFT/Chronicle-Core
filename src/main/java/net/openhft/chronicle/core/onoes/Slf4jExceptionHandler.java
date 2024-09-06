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
 */

package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.ClassLocal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code Slf4jExceptionHandler} is an enum implementation of the {@link ExceptionHandler} interface that uses SLF4J
 * (Simple Logging Facade for Java) to handle exceptions based on the level of logging severity.
 *
 * <p>It supports four levels of logging severity: ERROR, WARN, PERF, and DEBUG. Each instance of {@code Slf4jExceptionHandler}
 * logs at a specific level and corresponds to a {@link LogLevel} enum. This mapping allows exceptions to be logged
 * appropriately based on their severity.
 *
 * <p>The {@code DEBUG} instance also overrides the {@link #isEnabled(Class)} method, using the {@code isDebugEnabled()}
 * method from SLF4J's {@link Logger} class to determine if debug-level logging is enabled for the given class.
 *
 * <p>Additionally, there is a utility method {@code isJUnitTest()} (not shown in the provided code) which is used to
 * detect if the current execution context is a JUnit test.
 */
public enum Slf4jExceptionHandler implements ExceptionHandler {
    /**
     * Logs exceptions at the ERROR level.
     */
    ERROR {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            logger.error(message, thrown);
        }
    },

    /**
     * Logs exceptions at the WARN level.
     */
    WARN {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            logger.warn(message, thrown);
        }
    },

    /**
     * Logs exceptions at the INFO level, typically used for performance-related messages.
     */
    PERF {
        @Override
        public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
            logger.info(message, thrown);
        }
    },

    /**
     * Logs exceptions at the DEBUG level and checks if debug logging is enabled for a given class.
     */
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

    /**
     * Retrieves the {@link Logger} associated with the specified class.
     *
     * @param clazz the class for which to retrieve the logger.
     * @return the logger associated with the specified class.
     */
    static Logger getLogger(Class<?> clazz) {
        return CLASS_LOGGER.get(clazz);
    }

    /**
     * A {@link ClassLocal} cache for SLF4J {@link Logger} instances, initialized per class.
     */
    static final ClassLocal<Logger> CLASS_LOGGER = ClassLocal.withInitial(LoggerFactory::getLogger);

    /**
     * Returns the appropriate {@code Slf4jExceptionHandler} value based on the given {@link LogLevel}.
     *
     * @param logLevel the {@code LogLevel} to convert.
     * @return the corresponding {@code Slf4jExceptionHandler} value.
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
