package net.openhft.chronicle.core.util;

import java.util.concurrent.locks.LockSupport;

/**
 * A timer for timeouts which is resilient to pauses in the JVM.
 */
public enum Time {
    ;

    static long lastTime = System.currentTimeMillis();
    static volatile long tickTime = 0;

    public static long currentTimeMillis() {
        long now = System.currentTimeMillis();
        if (now == lastTime)
            return now;
        tickTime++;
        return lastTime = now;
    }
    public static long now() {
        currentTimeMillis();
        return tickTime;
    }

    public static void wait(Object o, long waitTimeMS) throws InterruptedException {
        if ((int) waitTimeMS != waitTimeMS)
            throw new IllegalArgumentException("waitTimeMS: " + waitTimeMS);
        long end = now() + waitTimeMS;
        for (long remaining; (remaining = end - now()) > 0; )
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
