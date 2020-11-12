package net.openhft.chronicle.core.time;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class UniqueMicroTimeProviderTest {

    @Test
    public void currentTimeMicros() {
        UniqueMicroTimeProvider tp = new UniqueMicroTimeProvider();
        SetTimeProvider stp = new SetTimeProvider();
        tp.provider(stp);
        long last = 0;
        for (int i = 0; i < 4_000; i++) {
            stp.advanceNanos(i);
            long time = tp.currentTimeMicros();
            assertTrue(time > last);
            last = time;
        }
    }

    @Test
    public void currentTimeNanos() {
        UniqueMicroTimeProvider tp = new UniqueMicroTimeProvider();
        SetTimeProvider stp = new SetTimeProvider();
        tp.provider(stp);
        long last = 0;
        for (int i = 0; i < 4_000; i++) {
            stp.advanceNanos(i);
            long time = tp.currentTimeNanos();
            assertTrue(time / 1000 > last);
            last = time / 1000;
        }
    }
}