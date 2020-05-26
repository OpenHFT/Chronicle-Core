/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

package net.openhft.chronicle.core.threads;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.LockSupport;

public class StackSampler {
    @NotNull
    private final Thread sampler;
    @Nullable
    private volatile Thread thread = null;
    @Nullable
    private volatile StackTraceElement[] stack = null;

    public StackSampler() {
        sampler = new Thread(this::sampling, "Thread sampler");
        sampler.setDaemon(true);
        sampler.start();
    }

    void sampling() {
        while (!Thread.currentThread().isInterrupted()) {
            Thread t = thread;
            if (t != null) {
                StackTraceElement[] stack0 = t.getStackTrace();
                if (thread == t)
                    stack = stack0;
            }
            LockSupport.parkNanos(10_000);
        }
    }

    public void stop() {
        sampler.interrupt();
    }

    public void thread(Thread thread) {
        this.thread = thread;
    }

    @Nullable
    public StackTraceElement[] getAndReset() {
        StackTraceElement[] stack = this.stack;
        thread = null;
        this.stack = null;
        return stack;
    }
}
