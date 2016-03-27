/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import java.util.concurrent.atomic.AtomicLong;

public class ReferenceCounter {
    private final AtomicLong value = new AtomicLong(1);
    private final Runnable onRelease;

    private ReferenceCounter(Runnable onRelease) {
        this.onRelease = onRelease;
    }

    public static ReferenceCounter onReleased(Runnable onRelease) {
        return new ReferenceCounter(onRelease);
    }

    public void reserve() throws IllegalStateException {
        for (; ; ) {
            long v = value.get();
            if (v <= 0)
                throw new IllegalStateException("Released");
            if (value.compareAndSet(v, v + 1))
                break;
        }
    }

    public void release() throws IllegalStateException {
        for (; ; ) {
            long v = value.get();
            if (v <= 0)
                throw new IllegalStateException("Released");
            if (value.compareAndSet(v, v - 1)) {
                if (v == 1)
                    onRelease.run();
                break;
            }
        }
    }

    public long get() {
        return value.get();
    }

    public String toString() {
        return Long.toString(value.get());
    }

    public void releaseAll() {
        if (value.get() > 0)
            onRelease.run();
    }
}
