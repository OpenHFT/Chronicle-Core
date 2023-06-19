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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
/**
 * RecordingExceptionHandler is a concrete implementation of the ExceptionHandler interface.
 * It records each exception by incrementing a count in a provided map, using an ExceptionKey as the key.
 *
 * Each ExceptionKey is constructed using a LogLevel, the Class where the exception happened, a custom message,
 * and the Throwable object representing the exception. The LogLevel is provided at instantiation, while
 * the rest are provided when an exception occurs.
 *
 * If the exceptionsOnly flag is set, the handler will ignore errors that are not associated with a Throwable.
 */
public class RecordingExceptionHandler implements ExceptionHandler {
    private final LogLevel level;
    private final Map<ExceptionKey, Integer> exceptionKeyCountMap;
    private final boolean exceptionsOnly;

    /**
     * Constructs an instance of RecordingExceptionHandler with the specified LogLevel, map for exception counts, and exceptionsOnly flag.
     *
     * @param level the LogLevel for the ExceptionKeys.
     * @param exceptionKeyCountMap the map where the count of each ExceptionKey will be stored.
     * @param exceptionsOnly a flag indicating if the handler should ignore errors that are not associated with a Throwable.
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
