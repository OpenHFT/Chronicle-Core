/*
 * Copyright 2016-2020 chronicle.software
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

import org.jetbrains.annotations.Nullable;

/**
 * Represents a throwable stack trace which is created purely for reporting purposes.
 * <p>
 * This class is not designed as an {@link Exception} or an {@link Error}, nor is it intended to be thrown or caught.
 * Instead, it serves as a lightweight representation of a stack trace that can be created and inspected without
 * incurring the performance cost of typical exception handling mechanisms.
 */
public class StackTrace extends Throwable {

    /**
     * Constructs a new {@code StackTrace} with a default message "stack trace".
     */
    public StackTrace() {
        this("stack trace");
    }

    /**
     * Constructs a new {@code StackTrace} with the specified message.
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
        super(message + " on " + Thread.currentThread().getName(), cause);
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
        String threadString = t.toString();

        if (t == Thread.currentThread())
            return new StackTrace(threadString);

        StackTrace st = new Less(threadString);
        StackTraceElement[] stackTrace = t.getStackTrace();

        int start = 0;
        // Skip native method frames at the top of the stack trace, if any.
        if (stackTrace.length > 2 && stackTrace[0].isNativeMethod()) {
            start++;
        }
        if (start > 0) {
            StackTraceElement[] adjustedStackTrace = new StackTraceElement[stackTrace.length - start];
            System.arraycopy(stackTrace, start, adjustedStackTrace, 0, adjustedStackTrace.length);
            stackTrace = adjustedStackTrace;
        }
        st.setStackTrace(stackTrace);
        return st;
    }

    /**
     * A lightweight version of {@code StackTrace} that avoids filling in the stack trace.
     */
    public static class Less extends StackTrace {
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
