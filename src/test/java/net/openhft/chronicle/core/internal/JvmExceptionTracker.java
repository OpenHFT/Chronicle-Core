package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;
import net.openhft.chronicle.testframework.internal.ExceptionTracker;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An implementation of ExceptionTracker that uses {@link Jvm} to track exceptions represented
 * by {@link ExceptionKey}s.
 */
public final class JvmExceptionTracker implements ExceptionTracker<ExceptionKey> {
    private final Map<Predicate<ExceptionKey>, String> ignoredExceptions = new LinkedHashMap<>();
    private final Map<Predicate<ExceptionKey>, String> expectedExceptions = new LinkedHashMap<>();
    private final Map<ExceptionKey, Integer> exceptions;

    private JvmExceptionTracker(Map<ExceptionKey, Integer> exceptions) {
        this.exceptions = exceptions;
    }

    /**
     * Create a JvmExceptionTracker
     *
     * @return the exception tracker
     */
    public static JvmExceptionTracker create() {
        return new JvmExceptionTracker(Jvm.recordExceptions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignoreException(String message) {
        ignoreException(k -> contains(k.message, message) || (k.throwable != null && contains(k.throwable.getMessage(), message)), message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void expectException(String message) {
        expectException(k -> contains(k.message, message) || (k.throwable != null && contains(k.throwable.getMessage(), message)), message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignoreException(Predicate<ExceptionKey> predicate, String description) {
        ignoredExceptions.put(predicate, description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void expectException(Predicate<ExceptionKey> predicate, String description) {
        expectedExceptions.put(predicate, description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkExceptions() {
        for (Map.Entry<Predicate<ExceptionKey>, String> expectedException : expectedExceptions.entrySet()) {
            if (!exceptions.keySet().removeIf(expectedException.getKey()))
                throw new AssertionError("No error for " + expectedException.getValue());
        }
        expectedExceptions.clear();
        for (Map.Entry<Predicate<ExceptionKey>, String> ignoredException : ignoredExceptions.entrySet()) {
            if (!exceptions.keySet().removeIf(ignoredException.getKey()))
                Slf4jExceptionHandler.DEBUG.on(getClass(), "Ignored " + ignoredException.getValue());
        }
        ignoredExceptions.clear();
        if (Jvm.hasException(exceptions)) {
            final String msg = exceptions.size() + " exceptions were detected: " + exceptions.keySet().stream().map(ek -> ek.message).collect(Collectors.joining(", "));
            Jvm.dumpException(exceptions);
            Jvm.resetExceptionHandlers();
            throw new AssertionError(msg);
        }
    }

    private static boolean contains(String text, String message) {
        return text != null && text.contains(message);
    }
}
