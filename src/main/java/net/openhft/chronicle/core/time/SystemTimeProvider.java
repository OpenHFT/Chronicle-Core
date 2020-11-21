/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;

public enum SystemTimeProvider implements TimeProvider {
    INSTANCE;

    private static final long LAST_NANOS;
    // Can be overridden for testing purposes.
    public static TimeProvider CLOCK = INSTANCE;

    static {
        // warmUp()
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 3) {
            INSTANCE.currentTimeNanos1();
            Jvm.nanoPause();
        }
        LAST_NANOS = UnsafeMemory.unsafeObjectFieldOffset(Jvm.getField(SystemTimeProvider.class, "lastNanos5"));
    }

    private long delta = 0;
    @UsedViaReflection
    private volatile long lastNanos5;

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1000;
    }

    /**
     * @return a nano second time stamp which is monotonically increasing and the top 59 bits are unique. i.e. 32 ns resolution.
     */
    @Override
    public long currentTimeNanos() {
        long timeNanos = currentTimeNanos1();
        while (true) {
            long last5 = this.lastNanos5;
            long timeNanos5 = timeNanos >>> 5;
            if (timeNanos5 <= last5) {
                timeNanos5 = last5 + 1;
                timeNanos = timeNanos5 << 5;
            }
            if (UnsafeMemory.INSTANCE.compareAndSwapLong(this, LAST_NANOS, last5, timeNanos5))
                return timeNanos;
        }
    }

    protected long currentTimeNanos1() {
        long nowNS = System.nanoTime();
        long nowMS = currentTimeMillis() * 1000000;
        long estimate = nowNS + delta;

        if (estimate < nowMS) {
            delta = nowMS - nowNS;
            return nowMS;

        } else if (estimate > nowMS + 1000000) {
            nowMS += 1000000;
            delta = nowMS - nowNS;
            return nowMS;
        }
        return estimate;
    }
}
