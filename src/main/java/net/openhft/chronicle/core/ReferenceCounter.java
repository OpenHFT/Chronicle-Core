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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ReferenceCounter {
    private final AtomicLong value = new AtomicLong(1);
    private final Runnable onRelease;

    // records where is reference was created and released, only used for debuging and
    // only active when assertions are turned on
    private List<Exception> referenceCountHistory;

    private ReferenceCounter(Runnable onRelease) {
        this.onRelease = onRelease;
        assert newRefCountHistory();
    }


    private boolean newRefCountHistory() {
        referenceCountHistory = new ArrayList<>();
        referenceCountHistory.add(new RuntimeException("creation ref-count=" + 0));
        return true;
    }

    public static ReferenceCounter onReleased(Runnable onRelease) {

        return new ReferenceCounter(onRelease);
    }

    public void reserve() throws IllegalStateException {
        for (; ; ) {

            long v = value.get();
            assert recordResevation(v);
            if (v <= 0) {
                assert logReferenceCountHistory();
                throw new IllegalStateException("Released");
            }
            if (value.compareAndSet(v, v + 1))
                break;
        }
    }

    private boolean recordResevation(long v) {
        referenceCountHistory.add(new RuntimeException("Reserve ref-count=" + v));
        return true;
    }

    public void release() throws IllegalStateException {
        for (; ; ) {
            long v = value.get();
            assert recordRelease(v);
            if (v <= 0) {
                assert logReferenceCountHistory();
                throw new IllegalStateException("Released");
            }
            if (value.compareAndSet(v, v - 1)) {
                if (v == 1)
                    onRelease.run();
                break;
            }
        }
    }

    private boolean logReferenceCountHistory() {
        referenceCountHistory.forEach(Throwable::printStackTrace);
        return true;
    }

    private boolean recordRelease(long v) {
        referenceCountHistory.add(new RuntimeException("Release ref-count=" + v));
        return true;
    }

    public long get() {
        return value.get();
    }

    public String toString() {
        return Long.toString(value.get());
    }

}
