package net.openhft.chronicle.core.time;

/**
 * Created by peter on 10/03/16.
 */
public class SetTimeProvider implements TimeProvider {
    private long currentTimeMillis;

    public void currentTimeMillis(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    @Override
    public long currentTimeMillis() {
        return currentTimeMillis;
    }
}
