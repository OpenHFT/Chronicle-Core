package net.openhft.chronicle.core.time;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Timestamps are unique across threads for a single process.
 */
public enum UniqueMicroTimeProvider implements TimeProvider {
    INSTANCE;

    private final AtomicLong lastTime = new AtomicLong();
    private TimeProvider provider = SystemTimeProvider.INSTANCE;

    public UniqueMicroTimeProvider provider(TimeProvider provider) {
        this.provider = provider;
        lastTime.set(provider.currentTimeMicros());
        return this;
    }

    @Override
    public long currentTimeMillis() {
        return provider.currentTimeMillis();
    }

    @Override
    public long currentTimeMicros() {
        long time = provider.currentTimeMicros();
        while (true) {
            long time0 = lastTime.get();
            if (time0 >= time)
                time = time0 + 1;
            if (lastTime.compareAndSet(time0, time))
                return time;
        }
    }
}
