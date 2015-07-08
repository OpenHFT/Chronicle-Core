package net.openhft.chronicle.core.util;

/**
 * A timer for timeouts which is resilient to pauses in the JVM.
 */
public enum UnpausedTime {
    ;

    static long lastTime = System.currentTimeMillis();
    static long timeMillis = 0;

    public static long now() {
        long now = System.currentTimeMillis();
        if (now == lastTime)
            return timeMillis;
        lastTime = now;
        return ++timeMillis;
    }

    public static void wait(Object o, long waitTimeMS) throws InterruptedException {
        if ((int) waitTimeMS != waitTimeMS)
            throw new IllegalArgumentException("waitTimeMS: " + waitTimeMS);
        long end = now() + waitTimeMS;
        for (long remaining; (remaining = end - now()) > 0; )
            o.wait(remaining);
    }
}
