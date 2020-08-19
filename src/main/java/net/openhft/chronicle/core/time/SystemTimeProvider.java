/*
 * Copyright 2016-2020 Chronicle Software
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

public enum SystemTimeProvider implements TimeProvider {
    INSTANCE;

    private static final long LAST_NANOS;
    // Can be overridden for testing purposes.
    public static TimeProvider CLOCK = INSTANCE;

    static {
        // warmUp()
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 3)
            INSTANCE.currentTimeNanos2(System.nanoTime());
        LAST_NANOS = UnsafeMemory.UNSAFE.objectFieldOffset(Jvm.getField(SystemTimeProvider.class, "lastNanos"));
    }

    private long delta = 0;
    private long calibrateNanos;
    private volatile long lastNanos;

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1000;
    }

    @Override
    public long currentTimeNanos() {
        long timeNanos2 = currentTimeNanos1();
        while (true) {
            long last = this.lastNanos;
            if (timeNanos2 <= last)
                timeNanos2 = last + 1;
            if (UnsafeMemory.UNSAFE.compareAndSwapLong(this, LAST_NANOS, last, timeNanos2))
                return timeNanos2;
        }
    }

    protected long currentTimeNanos1() {
        long nowNS = System.nanoTime();
        if (nowNS - calibrateNanos < 128_000_000_000L)
            return nowNS + delta;
        return currentTimeNanos2(nowNS);
    }

    protected long currentTimeNanos2(long nowNS) {
        calibrateNanos = nowNS;
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
