package net.openhft.chronicle.core.time;

/**
 * Created by peter on 10/03/16.
 */
public enum SystemTimeProvider implements TimeProvider {
    INSTANCE;

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
