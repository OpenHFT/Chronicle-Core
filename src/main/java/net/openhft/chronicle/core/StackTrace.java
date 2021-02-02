/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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
 * Throwable created purely for the purposes of reporting a stack trace.
 * <p>
 * This is not an Error or an Exception and is not expected to be thrown or caught.
 * </p>
 * https://github.com/OpenHFT/Chronicle-Core/issues/75
 */
public class StackTrace extends Throwable {
    public StackTrace() {
        this("stack trace");
    }

    public StackTrace(String message) {
        this(message, null);
    }

    public StackTrace(String message, Throwable cause) {
        super(message + " on " + Thread.currentThread().getName(), cause);
    }

    @Nullable
    public static StackTrace forThread(Thread t) {
        if (t == null) return null;
        StackTrace st = new StackTrace(t.toString());
        StackTraceElement[] stackTrace = t.getStackTrace();
        int start = 0;
        if (stackTrace.length > 2) {
            if (stackTrace[0].isNativeMethod()) {
                start++;
            }
        }
        if (start > 0) {
            StackTraceElement[] stackTrace2 = new StackTraceElement[stackTrace.length - start];
            System.arraycopy(stackTrace, start, stackTrace2, 0, stackTrace2.length);
            stackTrace = stackTrace2;
        }
        st.setStackTrace(stackTrace);
        return st;
    }
}
