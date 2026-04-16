/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.ClassLocal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler that logs using the SLF4J API.
 *
 * <p>Each enum constant represents a logging level and calls the matching
 * method on the SLF4J {@link Logger}. When SLF4J fails to initialise, or
 * the logger throws at runtime, the implementation writes to
 * {@code System.err} instead.
 */
public enum Slf4jExceptionHandler implements ExceptionHandler {
    ERROR(Logger::error),
    WARN(Logger::warn),
    PERF(Logger::info),
    DEBUG(Logger::debug) {
        @Override
        public boolean isEnabled(@NotNull Class<?> aClass) {
            return getLogger(aClass).isDebugEnabled();
        }
    };

    private final LogMethod logMethod;

    Slf4jExceptionHandler(LogMethod logMethod) {
        this.logMethod = logMethod;
    }

    @SuppressWarnings({"CallToPrintStackTrace", "java:S1181", "CQJvmLogOverSystemErr"}) // Catching Throwable ensures logging failures never mask the original error.
    @Override
    public void on(@NotNull Logger logger, @Nullable String message, @Nullable Throwable thrown) {
        try {
            logMethod.log(logger, message, thrown);
            // CSCatchThrowable catch Throwable so that we can attempt to still log the message
        } catch (Throwable t) {
            System.err.println("Failed to write to logger: " + logger.getName() + ", message: " + message);
            if (thrown != null) {
                System.err.println("Original exception: " + thrown.getMessage());
            }
            // CSPrintStackTrace keep printStackTrace because this is the last-resort fallback after structured logging has already failed.
            t.printStackTrace();
        }
    }

    @SuppressWarnings({"CallToPrintStackTrace", "java:S1181", "CQJvmLogOverSystemErr"})
    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, @Nullable Throwable thrown) {
        try {
            on(getLogger(clazz), message, thrown);
            // CSCatchThrowable catch Throwable so that we can attempt to still log the message
        } catch (Throwable t) {
            System.err.println("Failed to write to logger: " + clazz + ", message: " + message);
            if (thrown != null) {
                System.err.println("Original exception: " + thrown.getMessage());
            }
            // CSPrintStackTrace keep printStackTrace because this is the last-resort fallback after structured logging has already failed.
            t.printStackTrace();
        }
    }

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

    @FunctionalInterface
    interface LogMethod {
        void log(Logger logger, String message, Throwable thrown);
    }
}
