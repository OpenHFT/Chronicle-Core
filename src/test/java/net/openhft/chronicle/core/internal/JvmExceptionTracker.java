package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.onoes.LogLevel;
import net.openhft.chronicle.testframework.internal.ExceptionTracker;
import net.openhft.chronicle.testframework.internal.VanillaExceptionTracker;

import java.util.EnumSet;
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
        return new VanillaExceptionTracker<>(
                ExceptionKey::message,
                ExceptionKey::throwable,
                Jvm::resetExceptionHandlers,
                Jvm.recordExceptions(),
                key -> IGNORED_LOG_LEVELS.contains(key.level()),
                key -> key.level() + " " + key.clazz().getSimpleName() + " " + key.message()
        );
    }
}
