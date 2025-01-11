/*
 * Copyright 2016-2024 chronicle.software
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
package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static net.openhft.chronicle.core.time.SystemTimeProvider.CLOCK;

/**
 * Represents a throwable stack trace which is created purely for reporting purposes.
 * <p>
 * This class is not designed as an Error or an Exception and is not intended to be thrown or caught.
 * StackTrace extends Throwable as a “blank slate” that still retains the stack trace machinery for monitoring
 * and tracing purposes, but doesn’t carry the semantic baggage of being an error state or a normal exception.
 * <p>
 * To log this StackTrace, treat it as a Throwable and call {@link Throwable#printStackTrace()} or
 * use a standard logger:
 * <pre>{@code
 * LOGGER.warn("Thread is stalled here", StackTrace.forThread(monitoredThread));
 * }</pre>
 * StackTrace can be used to diagnose resource leaks, single-threaded resource enforcement,
 * diagnosing when a resource is used after closing and monitoring long-running threads on demand.
 * Taking a StackTrace isn't free; however, if used judiciously, it can be utilized in production
 * to provide on-demand profiling.
 */
public class StackTrace extends Throwable {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code StackTrace} for the current thread with the
     * default message "stack trace".
     */
    public StackTrace() {
        this("stack trace");
    }

    /**
     * Constructs a new {@code StackTrace} for the current thread with the specified message.
     *
     * @param message the detail message for this stack trace.
     */
    public StackTrace(String message) {
        this(message, null);
    }

    /**
     * Constructs a new {@code StackTrace} with the specified message and cause.
     *
     * @param message the detail message for this stack trace.
     * @param cause   the cause of this stack trace, or {@code null} if the cause is unknown or nonexistent.
     */
    public StackTrace(String message, Throwable cause) {
        super(message + " on " + Thread.currentThread().getName() + " at " + nanosAsZonedDateTime(), cause);
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
        // Create a specialized instance that doesn't fill in this constructor's own frames
        StackTrace st = new Less(t.toString());
        StackTraceElement[] stackTrace = t.getStackTrace();
        // Prune the top native method if present
        if (stackTrace.length > 2 && stackTrace[0].isNativeMethod()) {
            stackTrace = Arrays.copyOfRange(stackTrace, 1, stackTrace.length);
        }

        st.setStackTrace(stackTrace);
        return st;
    }

    /**
     * Converts the current (nano-precision) time from the system CLOCK to a {@link ZonedDateTime}.
     *
     * @return the current time as a {@link ZonedDateTime}, with nanosecond precision
     */
    @NotNull
    private static ZonedDateTime nanosAsZonedDateTime() {
        long nowNanos = CLOCK.currentTimeNanos();
        int nanosPerSecond = 1_000_000_000;
        return ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(nowNanos / nanosPerSecond, nowNanos % nanosPerSecond),
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
         * @param message the detail message for this stack trace.
         */
        public Less(String message) {
            super(message);
        }

        /**
         * Overrides the default behavior of {@code Throwable} to avoid filling in the stack trace.
         *
         * @return this instance, without filling in the stack trace.
         */
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
