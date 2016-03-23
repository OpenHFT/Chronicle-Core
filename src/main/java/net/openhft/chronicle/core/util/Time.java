/*
 *
 *  *     Copyright (C) ${YEAR}  higherfrequencytrading.com
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU Lesser General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU Lesser General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU Lesser General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
