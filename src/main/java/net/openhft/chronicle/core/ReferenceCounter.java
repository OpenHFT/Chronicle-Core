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

import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ReferenceCounter {
    private final AtomicLong value = new AtomicLong(1);
    private final Runnable onRelease;

    // records where reference was created, reserved and released,
    // only used for debugging and only active when assertions are turned on
    private Queue<Throwable> referenceCountHistory;

    private ReferenceCounter(Runnable onRelease) {
        this.onRelease = onRelease;
//        assert newRefCountHistory();
    }

    @NotNull
    public static ReferenceCounter onReleased(Runnable onRelease) {
        return new ReferenceCounter(onRelease);
    }

    private boolean newRefCountHistory() {
        referenceCountHistory = new ConcurrentLinkedQueue<>();
        referenceCountHistory.add(new Throwable(Integer.toHexString(onRelease.hashCode()) + '-' + Thread.currentThread().getName() + " creation ref-count=" + 1));
        return true;
    }

    public void reserve() throws IllegalStateException {
        for (; ; ) {

            long v = value.get();
            if (v <= 0) {
//                assert recordResevation(v);
//                assert logReferenceCountHistory();
                throw new IllegalStateException("Released");
            }
            if (value.compareAndSet(v, v + 1)) {
//                assert recordResevation(v + 1);
                break;
            }
        }
    }

    private boolean recordResevation(long v) {
        referenceCountHistory.add(new Throwable(Integer.toHexString(onRelease.hashCode()) + '-' + Thread.currentThread().getName() + " Reserve ref-count=" + v));
        return true;
    }

    public void release() throws IllegalStateException {
        for (; ; ) {
            long v = value.get();
            if (v <= 0) {
//                assert recordRelease(v);
//                assert logReferenceCountHistory();
                throw new IllegalStateException("Released");
            }
            if (value.compareAndSet(v, v - 1)) {
//                assert recordRelease(v - 1);
                if (v == 1)
                    onRelease.run();
                break;
            }
        }
    }

    private boolean recordRelease(long v) {
        referenceCountHistory.add(new Throwable(Integer.toHexString(onRelease.hashCode()) + '-' + Thread.currentThread().getName() + " Release ref-count=" + v));
        return true;
    }

    private boolean logReferenceCountHistory() {
        referenceCountHistory.forEach(Throwable::printStackTrace);
        return true;
    }

    public long get() {
        return value.get();
    }

    @NotNull
    public String toString() {
        return Long.toString(value.get());
    }

}
