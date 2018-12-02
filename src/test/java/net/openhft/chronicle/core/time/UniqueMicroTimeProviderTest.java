package net.openhft.chronicle.core.time;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class UniqueMicroTimeProviderTest {

    @Test
    public void currentTimeMicros() {
        TimeProvider tp = UniqueMicroTimeProvider.INSTANCE;
        long last = 0;
        for (int i = 0; i < 100_000; i++) {
            long time = tp.currentTimeMicros();
            assertTrue(time > last);
            last = time;
        }
    }

}