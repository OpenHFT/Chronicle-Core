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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;

/**
 * The {@code RecordingExceptionHandler} class is a concrete implementation of the {@link ExceptionHandler} interface.
 * It records each exception by incrementing a count in a provided map, using an {@link ExceptionKey} as the key.
 *
 * <p>Each {@code ExceptionKey} is constructed using a {@link LogLevel}, the {@code Class} where the exception happened,
 * a custom message, and the {@link Throwable} object representing the exception. The {@code LogLevel} is provided at instantiation,
 * while the rest are provided when an exception occurs.
 *
 * <p>If the {@code exceptionsOnly} flag is set, the handler will ignore errors that are not associated with a {@link Throwable}.
 */
public class RecordingExceptionHandler implements ExceptionHandler {

    /**
     * The log level associated with this exception handler.
     */
    private final LogLevel level;

    /**
     * A map that keeps track of the count of each exception by using an {@link ExceptionKey} as the key.
     */
    private final Map<ExceptionKey, Integer> exceptionKeyCountMap;

    /**
     * A flag indicating if the handler should ignore errors that are not associated with a {@link Throwable}.
     */
    private final boolean exceptionsOnly;

    /**
     * Constructs an instance of {@code RecordingExceptionHandler} with the specified {@link LogLevel},
     * map for exception counts, and {@code exceptionsOnly} flag.
     *
     * @param level                the {@link LogLevel} for the {@link ExceptionKey}s.
     * @param exceptionKeyCountMap the map where the count of each {@link ExceptionKey} will be stored.
     * @param exceptionsOnly       a flag indicating if the handler should ignore errors that are not associated with a {@link Throwable}.
     */
    public RecordingExceptionHandler(LogLevel level, Map<ExceptionKey, Integer> exceptionKeyCountMap, boolean exceptionsOnly) {
        this.level = level;
        this.exceptionKeyCountMap = exceptionKeyCountMap;
        this.exceptionsOnly = exceptionsOnly;
    }

    /**
     * Records an exception by incrementing the count of its corresponding {@link ExceptionKey} in the map.
     *
     * <p>If the {@code exceptionsOnly} flag is set and the {@code thrown} parameter is {@code null},
     * the exception will not be recorded.
     *
     * @param clazz   the class where the exception occurred. Must not be {@code null}.
     * @param message a custom message detailing the error, or {@code null}.
     * @param thrown  the throwable instance representing the error, or {@code null}.
     */
    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, Throwable thrown) {
        if (exceptionsOnly && thrown == null)
            return;

        // Synchronize to ensure thread-safe access to the exceptionKeyCountMap.
        synchronized (exceptionKeyCountMap) {
            @NotNull ExceptionKey key = new ExceptionKey(level, clazz, message, thrown);
            // Increment the count for the exception key, or add it with a count of 1 if it's not already present.
            exceptionKeyCountMap.merge(key, 1, Integer::sum);
        }
    }

    /**
     * Records an exception by incrementing the count of its corresponding {@link ExceptionKey} in the map.
     *
     * <p>If the {@code exceptionsOnly} flag is set and the {@code thrown} parameter is {@code null},
     * the exception will not be recorded.
     *
     * @param logger  the {@link Logger} instance. Must not be {@code null}.
     * @param message a custom message detailing the error, or {@code null}.
     * @param thrown  the throwable instance representing the error, or {@code null}.
     */
    @Override
    public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
        if (exceptionsOnly && thrown == null)
            return;

        // Synchronize to ensure thread-safe access to the exceptionKeyCountMap.
        synchronized (exceptionKeyCountMap) {
            @NotNull ExceptionKey key = new ExceptionKey(level, Logger.class, logger.getName() + ": " + message, thrown);
            // Increment the count for the exception key, or add it with a count of 1 if it's not already present.
            exceptionKeyCountMap.merge(key, 1, Integer::sum);
        }
    }
}
