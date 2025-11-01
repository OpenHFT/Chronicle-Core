/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */

package net.openhft.chronicle.core.onoes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
/**
 * Records each exception by incrementing a count in a provided map keyed by
 * {@link ExceptionKey}.
 * <p>
 * The supplied {@code exceptionKeyCountMap} <em>must</em> be thread-safe - for
 * example a {@link java.util.concurrent.ConcurrentHashMap}. The handler itself
 * is thread-safe.
 * <p>
 * The map may grow without bound if unique keys keep being added.
 * Periodically evict infrequently used entries to control memory usage.
 */
public class RecordingExceptionHandler implements ExceptionHandler {
    private final LogLevel level;
    private final Map<ExceptionKey, Integer> exceptionKeyCountMap;
    private final boolean exceptionsOnly;

    /**
     * Creates a handler that records exceptions in the supplied map.
     *
     * @param level                the {@link LogLevel} used in the {@link ExceptionKey}.
     * @param exceptionKeyCountMap thread-safe map where counts are stored, for example a
     *                             {@link java.util.concurrent.ConcurrentHashMap}.
     * @param exceptionsOnly       when {@code true}, only errors with a {@link Throwable}
     *                             are recorded.
     */
    public RecordingExceptionHandler(LogLevel level, Map<ExceptionKey, Integer> exceptionKeyCountMap, boolean exceptionsOnly) {
        this.level = level;
        this.exceptionKeyCountMap = exceptionKeyCountMap;
        this.exceptionsOnly = exceptionsOnly;
    }

    /**
     * Records an exception by incrementing the count of its corresponding ExceptionKey in the map.
     *
     * @param clazz   the class where the exception occurred. Must not be null.
     * @param message a custom message detailing the error, or null.
     * @param thrown  the throwable instance representing the error, or null.
     */
    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, Throwable thrown) {
        if (exceptionsOnly && thrown == null)
            return;
        synchronized (exceptionKeyCountMap) {
            @NotNull ExceptionKey key = new ExceptionKey(level, clazz, message, thrown);
            exceptionKeyCountMap.merge(key, 1, Integer::sum);
        }
    }

    /**
     * Records an exception by incrementing the count of its corresponding ExceptionKey in the map.
     *
     * @param logger  the logger instance. Must not be null.
     * @param message a custom message detailing the error, or null.
     * @param thrown  the throwable instance representing the error, or null.
     */
    @Override
    public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
        if (exceptionsOnly && thrown == null)
            return;
        synchronized (exceptionKeyCountMap) {
            @NotNull ExceptionKey key = new ExceptionKey(level, Logger.class, logger.getName() + ": " + message, thrown);
            exceptionKeyCountMap.merge(key, 1, Integer::sum);
        }
    }
}
