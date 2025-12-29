/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.onoes.LogLevel;
import net.openhft.chronicle.testframework.exception.ExceptionTracker;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static net.openhft.chronicle.core.onoes.LogLevel.DEBUG;
import static net.openhft.chronicle.core.onoes.LogLevel.PERF;

/**
 * A Factory for creating ExceptionTrackers that use {@link Jvm} to track exceptions represented
 * by {@link ExceptionKey}s.
 */
public enum JvmExceptionTracker {
    ;

    private static final Set<LogLevel> IGNORED_LOG_LEVELS = EnumSet.of(DEBUG, PERF);

    /**
     * Builds an ExceptionTracker using the current {@link Jvm} exception recording configuration.
     *
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create() {
        return create(Jvm.recordExceptions());
    }

    /**
     * Builds an ExceptionTracker with optional debug exception tracking.
     *
     * @param debug Whether to track debug messages
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create(final boolean debug) {
        return create(Jvm.recordExceptions(debug));
    }

    /**
     * Builds an ExceptionTracker with debug and exceptions-only options.
     *
     * @param debug          Whether to track debug messages
     * @param exceptionsOnly Whether to track only messages with exceptions
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create(final boolean debug,
                                                        final boolean exceptionsOnly) {
        return create(Jvm.recordExceptions(debug, exceptionsOnly));
    }

    /**
     * Builds an ExceptionTracker with debug, exceptions-only, and SLF4J logging options.
     *
     * @param debug          Whether to track debug messages
     * @param exceptionsOnly Whether to track only messages with exceptions
     * @param logToSlf4j     Whether to also log messages to slf4j
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create(final boolean debug,
                                                        final boolean exceptionsOnly,
                                                        final boolean logToSlf4j) {
        return create(Jvm.recordExceptions(debug, exceptionsOnly, logToSlf4j));
    }

    /**
     * Builds an ExceptionTracker from an existing recorded exceptions map.
     *
     * @return the exception tracker
     */
    private static ExceptionTracker<ExceptionKey> create(Map<ExceptionKey, Integer> recordedExceptions) {
        return ExceptionTracker.create(
                ExceptionKey::message,
                ExceptionKey::throwable,
                Jvm::resetExceptionHandlers,
                recordedExceptions,
                key -> IGNORED_LOG_LEVELS.contains(key.level()),
                key -> key.level() + " " + key.clazz().getSimpleName() + " " + key.message()
        );
    }
}
