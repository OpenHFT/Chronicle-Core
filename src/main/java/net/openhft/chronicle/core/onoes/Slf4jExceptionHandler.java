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
 * <p>Unless {@code -Dchronicle.logging.disableStderrFallback=true}, WARN and ERROR also
 * write to stderr when both the supplied logger and the active SLF4J factory
 * are NOP implementations. This explicitly overrides an installed NOP provider,
 * but does not bypass filtering by a real provider. Direct SLF4J calls are not covered.
 * <p>Set the disable property on the JVM command line for early logging. The fallback
 * reads system properties directly; it does not initialise Jvm or load system.properties.
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

    @SuppressWarnings({"CallToPrintStackTrace", "java:S1181"}) // Catching Throwable ensures logging failures never mask the original error.
    @Override
    public void on(@NotNull Logger logger, @Nullable String message, @Nullable Throwable thrown) {
        try {
            if ((this == WARN || this == ERROR) && NopLoggerFallback.enabled(logger))
                NopLoggerFallback.write(name(), logger.getName(), message, thrown);
            else
                logMethod.log(logger, message, thrown);
        } catch (Throwable t) {
            System.err.println("Failed to write to logger: " + logger.getName() + ", message: " + message);
            if (thrown != null) {
                System.err.println("Original exception: " + thrown.getMessage());
            }
            t.printStackTrace();
        }
    }

    @SuppressWarnings({"CallToPrintStackTrace", "java:S1181"})
    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, @Nullable Throwable thrown) {
        try {
            Logger logger = getLogger(clazz);
            if ((this == WARN || this == ERROR) && NopLoggerFallback.enabled(logger))
                NopLoggerFallback.write(name(), clazz.getName(), message, thrown);
            else
                on(logger, message, thrown);
        } catch (Throwable t) {
            System.err.println("Failed to write to logger: " + clazz + ", message: " + message);
            if (thrown != null) {
                System.err.println("Original exception: " + thrown.getMessage());
            }
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
