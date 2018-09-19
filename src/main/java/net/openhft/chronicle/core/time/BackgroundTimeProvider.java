package net.openhft.chronicle.core.time;

/**
 * EXPERIMENTAL!!!
 */
public enum BackgroundTimeProvider implements TimeProvider {
    INSTANCE;

    static volatile long delta;
    static int count = 0;

    static {
        delta = SystemTimeProvider.INSTANCE.delta;
        Thread t = new Thread(INSTANCE::run, "bg-time-provider");
        t.setDaemon(true);
        t.start();
    }

    void run() {
        try {
            for (int i = 0; i < 1000; i++) {
                delta = SystemTimeProvider.INSTANCE.delta;
                Thread.yield();
            }
            for (; ; ) {
                long delta2 = SystemTimeProvider.INSTANCE.delta;
                delta += (delta2 - delta) / 8;
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            // dying.
        }
    }

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
        if (count++ > 20) {
            count = 0;
            return SystemTimeProvider.INSTANCE.currentTimeNanos();
        }
        return System.nanoTime() + delta;
    }
}
