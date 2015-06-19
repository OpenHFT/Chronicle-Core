/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core;

import java.util.concurrent.atomic.AtomicLong;

public class ReferenceCounter {
    private final AtomicLong value = new AtomicLong(1);
    private final Runnable onRelease;

    ReferenceCounter(Runnable onRelease) {
        this.onRelease = onRelease;
    }

    public static ReferenceCounter onReleased(Runnable onRelease) {
        return new ReferenceCounter(onRelease);
    }

    public void reserve() {
        for (; ; ) {
            long v = value.get();
            if (v <= 0)
                throw new IllegalStateException("Released");
            if (value.compareAndSet(v, v + 1))
                break;
        }
    }

    public void release() {
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
