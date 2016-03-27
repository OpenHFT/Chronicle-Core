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

package net.openhft.chronicle.core.util;

import java.util.concurrent.locks.LockSupport;

/**
 * A timer for timeouts which is resilient to pauses in the JVM.
 */
public enum Time {
    ;

    static volatile long lastTime = System.currentTimeMillis();
    static volatile long tickTime = 0;

    public static long currentTimeMillis() {
        long now = System.currentTimeMillis();
        if (now == lastTime)
            return now;
        tickTime++;
        lastTime = now;
        return lastTime;
    }

    public static long tickTime() {
        currentTimeMillis();
        return tickTime;
    }

    public static void wait(Object o, long waitTimeMS) throws InterruptedException, IllegalArgumentException {
        if ((int) waitTimeMS != waitTimeMS)
            throw new IllegalArgumentException("waitTimeMS: " + waitTimeMS);
        long end = tickTime() + waitTimeMS;
        for (long remaining; (remaining = end - tickTime()) > 0; )
            o.wait(remaining);
    }

    public static void parkNanos(long nanos) {
        long millis = nanos / 1000000;
        if (millis > 0) {
            long start = System.currentTimeMillis();
            long startTT = tickTime;
            LockSupport.parkNanos(nanos);
            long end = System.currentTimeMillis();
            long endTT = Math.min(end - start, millis) + startTT;
            if (endTT > tickTime)
                tickTime = endTT;
        } else {
            currentTimeMillis();
            LockSupport.parkNanos(nanos);
            currentTimeMillis();
        }
    }
}
