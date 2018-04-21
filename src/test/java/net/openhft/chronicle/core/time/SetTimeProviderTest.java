package net.openhft.chronicle.core.time;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class SetTimeProviderTest {

    @Test
    public void testNoOpConstructor() {
        final SetTimeProvider tp = new SetTimeProvider();
        assertEquals(0, tp.currentTimeNanos());
        tp.currentTimeNanos(99_123_456_789L);
        assertEquals(99_123_456_789L, tp.currentTimeNanos());
        assertEquals(99_123_456L, tp.currentTimeMicros());
        assertEquals(99_123L, tp.currentTimeMillis());
        assertEquals(99, tp.currentTime(TimeUnit.SECONDS));
        tp.advanceMillis(7).advanceMicros(5).advanceNanos(3);
        assertEquals(99_130_461_792L, tp.currentTimeNanos());
    }

    @Test
    public void testNanosConstructor() {
        final SetTimeProvider tp = new SetTimeProvider(99_999_999_999L);
        assertEquals(99_999_999_999L, tp.currentTimeNanos());
        assertEquals(99_999_999L, tp.currentTimeMicros());
        assertEquals(99_999L, tp.currentTimeMillis());
        assertEquals(99, tp.currentTime(TimeUnit.SECONDS));

        tp.currentTimeMicros(100_000_000L);
        assertEquals(100_000_000_000L, tp.currentTimeNanos());
        assertEquals(100_000_000L, tp.currentTimeMicros());
        assertEquals(100_000L, tp.currentTimeMillis());
        assertEquals(100, tp.currentTime(TimeUnit.SECONDS));

        tp.currentTimeMillis(101_987L);
        assertEquals(101_987_000_000L, tp.currentTimeNanos());
        assertEquals(101_987_000L, tp.currentTimeMicros());
        assertEquals(101_987L, tp.currentTimeMillis());
        assertEquals(101, tp.currentTime(TimeUnit.SECONDS));
        tp.advanceMillis(1_011).advanceMicros(1_211).advanceNanos(789_123);
        assertEquals(103_000_000_123L, tp.currentTimeNanos());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttemptToGoBackwardsNanos() {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        tp.currentTimeNanos(99_999_999_999L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttemptToGoBackwardsMicros() {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        tp.currentTimeMicros(99_999_999L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttemptToGoBackwardsMillis() {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        tp.currentTimeMillis(99_999L);
    }

}
