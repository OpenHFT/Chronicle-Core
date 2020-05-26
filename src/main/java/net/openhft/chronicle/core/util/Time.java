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

package net.openhft.chronicle.core.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * A timer for timeouts which is resilient to pauses in the JVM, tickTime can only increase if the JVM hasn't been paused.
 * <p>
 * For this to work, currentTimeMillis (or one of the methods that calls it) must be called more frequently than
 * every millisecond; the EventLoop implementations in chronicle-threads do this.
 * </p>
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

    /**
     * Sleeps in such a way that any long pause of the JVM, e.g. GC, will make it wait much longer to prevent timeouts being busted.
     *
     * @param time to pause for at least.
     * @param timeUnit the time unit
     */
    public static void sleep(long time, TimeUnit timeUnit) {
        int millis = (int) timeUnit.convert(time, TimeUnit.MILLISECONDS);
        for (int i = 0; i < millis; i++) {
            currentTimeMillis();
            LockSupport.parkNanos(1_000_000);
        }
    }
}
