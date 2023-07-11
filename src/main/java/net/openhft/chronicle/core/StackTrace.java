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
 * This class is not designed as an Error or an Exception and is not intended to be thrown or caught.
 * 
 * <a href="https://github.com/OpenHFT/Chronicle-Core/issues/75">...</a>
 */
public class StackTrace extends Throwable {

    /**
     * Constructs a new StackTrace with a default message "stack trace".
     */
    public StackTrace() {
        this("stack trace");
    }

    /**
     * Constructs a new StackTrace with the specified message.
     *
     * @param message the detail message.
     */
    public StackTrace(String message) {
        this(message, null);
    }

    /**
     * Constructs a new StackTrace with the specified message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of the stack trace.
     */
    public StackTrace(String message, Throwable cause) {
        super(message + " on " + Thread.currentThread().getName(), cause);
    }

    /**
     * Returns a StackTrace object for the specified thread.
     *
     * @param t the thread for which to obtain the stack trace.
     * @return a StackTrace object containing the stack trace of the specified thread,
     * or {@code null} if the thread is null.
     */
    @Nullable
    public static StackTrace forThread(Thread t) {
        if (t == null) return null;
        StackTrace st = new StackTrace(t.toString());
        StackTraceElement[] stackTrace = t.getStackTrace();
        int start = 0;
        if (stackTrace.length > 2 && stackTrace[0].isNativeMethod()) {
            start++;
        }
        if (start > 0) {
            StackTraceElement[] ste2 = new StackTraceElement[stackTrace.length - start];
            System.arraycopy(stackTrace, start, ste2, 0, ste2.length);
            stackTrace = ste2;
        }
        st.setStackTrace(stackTrace);
        return st;
    }
}
