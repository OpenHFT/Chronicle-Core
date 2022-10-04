/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.onoes.LogLevel;
import net.openhft.chronicle.testframework.internal.ExceptionTracker;

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
     * Create a JvmExceptionTracker
     *
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create() {
        return create(Jvm.recordExceptions());
    }

    /**
     * Create a JvmExceptionTracker
     *
     * @param debug Whether to track debug messages
     * @return the exception tracker
     */
    public static ExceptionTracker<ExceptionKey> create(final boolean debug) {
        return create(Jvm.recordExceptions(debug));
    }

    /**
     * Create a JvmExceptionTracker
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
     * Create a JvmExceptionTracker
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
     * Create a JvmExceptionTracker
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
