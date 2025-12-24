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
     * Create an exception tracker that records Jvm exception events.
     *
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create() {
        return create(Jvm.recordExceptions());
    }

    /**
     * Create an exception tracker configured to include or exclude debug-level events.
     *
     * @param debug whether to track debug messages
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create(final boolean debug) {
        return create(Jvm.recordExceptions(debug));
    }

    /**
     * Create an exception tracker that can restrict output to exception-bearing events.
     *
     * @param debug          whether to track debug messages
     * @param exceptionsOnly whether to track only messages with exceptions
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create(final boolean debug,
                                                        final boolean exceptionsOnly) {
        return create(Jvm.recordExceptions(debug, exceptionsOnly));
    }

    /**
     * Create an exception tracker that optionally forwards events to SLF4J.
     *
     * @param debug          whether to track debug messages
     * @param exceptionsOnly whether to track only messages with exceptions
     * @param logToSlf4j     whether to also log messages to SLF4J
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create(final boolean debug,
                                                        final boolean exceptionsOnly,
                                                        final boolean logToSlf4j) {
        return create(Jvm.recordExceptions(debug, exceptionsOnly, logToSlf4j));
    }

    /**
     * Create an exception tracker from the recorded exception map.
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
