package net.openhft.chronicle.core.time;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UniqueMicroTimeProviderTest {

    @Test
    public void currentTimeMicros() {
        UniqueMicroTimeProvider tp = new UniqueMicroTimeProvider();
        SetTimeProvider stp = new SetTimeProvider(SystemTimeProvider.INSTANCE.currentTimeNanos());
        tp.provider(stp);
        long last = 0;
        for (int i = 0; i < 4_000; i++) {
            stp.advanceNanos(i);
            long time = tp.currentTimeMicros();
            assertEquals(LongTime.toMicros(time), time);
            assertTrue(time > last);
            last = time;
        }
    }

    @Test
    public void currentTimeNanos() {
        UniqueMicroTimeProvider tp = new UniqueMicroTimeProvider();
        SetTimeProvider stp = new SetTimeProvider(SystemTimeProvider.INSTANCE.currentTimeNanos());
        tp.provider(stp);
        long last = 0;
        for (int i = 0; i < 4_000; i++) {
            stp.advanceNanos(i);
            long time = tp.currentTimeNanos();
            assertEquals(LongTime.toNanos(time), time);
            assertTrue(time / 1000 > last);
            last = time / 1000;
        }
    }
}