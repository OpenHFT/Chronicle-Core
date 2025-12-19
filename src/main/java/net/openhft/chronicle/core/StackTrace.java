/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Represents a throwable stack trace, of the current thread or another thread, purely for reporting purposes.
 * <p>
 * This class is not designed as an Error or an Exception and is not intended to be thrown or caught.
 * StackTrace extends Throwable as a "blank slate" that still retains the stack trace machinery for monitoring
 * and tracing purposes, but doesn't carry the semantic baggage of being an error state or a normal exception.
 * <p>
 * To log this StackTrace, treat it as a Throwable and call {@link Throwable#printStackTrace()} or
 * use a standard logger. E.g. when montioring a thread:
 * <pre>{@code
 * LOGGER.warn("Thread is stalled here", StackTrace.forThread(monitoredThread));
 * }</pre>
 * <p>
 * To highlight why a resource can't be used anymore, use the following pattern:
 * <pre>{@code
 * // in close()
 * closedHere = new StackTrace("Resource was closed here");
 * // in use()
 * if (closed)
 *    throw new IllegalStateException("Resource is closed", closedHere));
 * }</pre>
 * <p>
 * To highlight where a resource was created, use the following pattern:
 * <pre>{@code
 * private final StackTrace createdHere = new StackTrace("Resource was created here");
 * // in warnIfNotClosed()
 * if (!closed)
 *   LOGGER.warn("Resource was not closed", createdHere);
 * }</pre>
 * To combine with StackWalker, use the following pattern:
 * <pre>{@code
 * StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
 * // Skip the first element, which is the current method
 * StackTraceElement[] stackTrace = walker.walk(s -> s.skip(1).toArray(StackTraceElement[]::new));
 * StackTrace st = new StackTrace.Less("Resource was created here", stackTrace);
 * }</pre>
 * <p>
 * StackTrace can be used to diagnose resource leaks, single-threaded resource enforcement,
 * diagnosing when a resource is used after closing and monitoring long-running threads on demand.
 * Taking a StackTrace isn't free; however, if used judiciously, it can be utilized in production
 * to provide on-demand profiling.
 * <p>
 * For a deep dive into the StackTrace class see <a href="https://github.com/OpenHFT/Chronicle-Core/tree/ea/src/main/docs/StackTrace-user-guide.adoc">StackTrace User Guide.adoc</a>
 */
public class StackTrace extends Throwable {
    private static final long serialVersionUID = 1L;
    private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
    private static final int NANOS_PER_MILLI = 1_000_000;

    /**
     * Creates a stack trace for the current thread using the message "stack trace".
     */
    public StackTrace() {
        this("stack trace", false);
    }

    /**
     * Creates a stack trace for the current thread using the message "stack trace" and optionally adds a timestamp.
     *
     * @param addTimestamp whether to add a timestamp to the stack trace message
     */
    public StackTrace(boolean addTimestamp) {
        this("stack trace", null, addTimestamp);
    }

    /**
     * Creates a stack trace for the current thread with the specified message.
     *
     * @param message the detail message for this stack trace
     */
    public StackTrace(String message) {
        this(message, null);
    }

    /**
     * Creates a stack trace with the specified message and an optional timestamp.
     *
     * @param message      the detail message for this stack trace
     * @param addTimestamp whether to add a timestamp to the stack trace message
     */
    public StackTrace(String message, boolean addTimestamp) {
        this(message, null, addTimestamp);
    }

    /**
     * Creates a stack trace with the specified message and cause.
     *
     * @param message the detail message for this stack trace
     * @param cause   the underlying cause, or {@code null} if unknown
     */
    public StackTrace(String message, Throwable cause) {
        this(message, cause, false);
    }

    /**
     * Creates a stack trace with the specified message and cause and can add a timestamp.
     *
     * @param message      the detail message for this stack trace
     * @param cause        the underlying cause, or {@code null} if unknown
     * @param addTimestamp whether to add a timestamp to the stack trace message
     */
    public StackTrace(String message, Throwable cause, boolean addTimestamp) {
        super(message + " on " + Thread.currentThread().getName() + (addTimestamp ? " at " + nanosAsZonedDateTime() : ""), cause);
    }

    /**
     * Creates a {@code StackTrace} object for the specified thread.
     * <p>
     * If the thread is {@code null}, this method returns {@code null}.
     * If the thread is the current thread, a simple {@code StackTrace} with its name is returned.
     * Otherwise, a {@link Less} instance is returned with the thread's stack trace adjusted to
     * exclude native methods from the top of the stack.
     * </p>
     *
     * @param t the thread for which to obtain the stack trace, or {@code null}.
     * @return a {@code StackTrace} object representing the thread's stack trace, or {@code null} if the thread is {@code null}.
     */
    @Nullable
    public static StackTrace forThread(Thread t) {
        if (t == null) return null;
        StackTraceElement[] stackTrace = t.getStackTrace();
        // Prune the top native method if present
        if (stackTrace.length > 2 && stackTrace[0].isNativeMethod()) {
            stackTrace = Arrays.copyOfRange(stackTrace, 1, stackTrace.length);
        }

        return new Less(t.toString(), stackTrace);
    }

    /**
     * Converts the current (nano-precision) time from the system CLOCK to a {@link ZonedDateTime}.
     *
     * @return the current time as a {@link ZonedDateTime}, with nanosecond precision
     */
    @NotNull
    private static ZonedDateTime nanosAsZonedDateTime() {
        // temporary change to confirm effect - only creates time at millisecond precision
        long nowNanos = System.currentTimeMillis() * NANOS_PER_MILLI;
        return ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(nowNanos / NANOS_PER_SECOND, nowNanos % NANOS_PER_SECOND),
                ZoneOffset.UTC);
    }

    /**
     * A specialized {@link StackTrace} that doesn't automatically fill in its own
     * call frames, so we can later call {@code setStackTrace(...)} with
     * another thread's frames.
     */
    public static class Less extends StackTrace {
        private static final long serialVersionUID = 1L;

        /**
         * Creates an empty stack trace placeholder with a message.
         *
         * @param message the detail message for this stack trace.
         */
        public Less(String message) {
            super(message, true);
        }

        /**
         * Creates a placeholder using the supplied stack trace elements.
         *
         * @param message the detail message for this stack trace.
         * @param stackTrace the stack trace elements for this stack trace.
         */
        @SuppressWarnings("this-escape")
        public Less(String message, StackTraceElement[] stackTrace) {
            this(message);
            setStackTrace(stackTrace);
        }

        /**
         * Overrides the default behavior of {@code Throwable} to avoid filling in the stack trace.
         *
         * @return this instance, without filling in the stack trace.
         */
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
