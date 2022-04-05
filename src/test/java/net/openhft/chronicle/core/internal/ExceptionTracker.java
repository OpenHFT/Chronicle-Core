package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A test utility class for recording and executing assertions about the presence (or absence) of exceptions
 */
public final class ExceptionTracker {
    private final Map<Predicate<ExceptionKey>, String> ignoredExceptions = new LinkedHashMap<>();
    private final Map<Predicate<ExceptionKey>, String> expectedExceptions = new LinkedHashMap<>();
    private Map<ExceptionKey, Integer> exceptions;

    /**
     * Call this in @Before to start accumulating exceptions
     */
    public void recordExceptions() {
        exceptions = Jvm.recordExceptions();
    }

    /**
     * Ignore exceptions containing the specified string
     *
     * @param message The string to ignore
     */
    public void ignoreException(String message) {
        ignoreException(k -> contains(k.message, message) || (k.throwable != null && contains(k.throwable.getMessage(), message)), message);
    }

    /**
     * Require than an exception containing the specified string is thrown during the test
     *
     * @param message The string to require
     */
    public void expectException(String message) {
        expectException(k -> contains(k.message, message) || (k.throwable != null && contains(k.throwable.getMessage(), message)), message);
    }

    /**
     * Ignore exceptions matching the specified predicate
     *
     * @param predicate   The predicate to match the exception
     * @param description The description of the exceptions being ignored
     */
    public void ignoreException(Predicate<ExceptionKey> predicate, String description) {
        ignoredExceptions.put(predicate, description);
    }

    /**
     * Require that an exception matching the specified predicate is thrown
     *
     * @param predicate   The predicate used to match exceptions
     * @param description The description of the exceptions being required
     */
    public void expectException(Predicate<ExceptionKey> predicate, String description) {
        expectedExceptions.put(predicate, description);
    }

    /**
     * Call this in @After to ensure
     * <ul>
     *     <li>No non-ignored exceptions were thrown</li>
     *     <li>There is an exception matching each of the expected predicates</li>
     * </ul>
     */
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
