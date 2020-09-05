package net.openhft.chronicle.core.time;

import org.junit.Test;

import java.time.Instant;
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
        final SetTimeProvider tp = new SetTimeProvider(99_999_999_999_000_000L);
        assertEquals(99_999_999_999_000_000L, tp.currentTimeNanos());
        assertEquals(99_999_999_999_000L, tp.currentTimeMicros());
        assertEquals(99_999_999_999L, tp.currentTimeMillis());
        assertEquals(99_999_999, tp.currentTime(TimeUnit.SECONDS));

        tp.currentTimeMicros(100_000_000_000_000L);
        assertEquals(100_000_000_000_000_000L, tp.currentTimeNanos());
        assertEquals(100_000_000_000_000L, tp.currentTimeMicros());
        assertEquals(100_000_000_000L, tp.currentTimeMillis());
        assertEquals(100_000_000, tp.currentTime(TimeUnit.SECONDS));

        tp.currentTimeMillis(101_987_000_000L);
        assertEquals(101_987_000_000_000_000L, tp.currentTimeNanos());
        assertEquals(101_987_000_000_000L, tp.currentTimeMicros());
        assertEquals(101_987_000_000L, tp.currentTimeMillis());
        assertEquals(101_987_000, tp.currentTime(TimeUnit.SECONDS));
        tp.advanceMillis(1_011).advanceMicros(1_211).advanceNanos(789_123);
        assertEquals(101_987_001_013_000_123L, tp.currentTimeNanos());
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

    @Test
    public void withTimestamp() {
        SetTimeProvider tp = new SetTimeProvider("2018-08-20T12:53:04.075");
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584075000L, tp.currentTimeMicros());
        SetTimeProvider tp2 = new SetTimeProvider("2018-08-20T12:53:04.075123");
        assertEquals(1534769584075L, tp2.currentTimeMillis());
        assertEquals(1534769584075123L, tp2.currentTimeMicros());
    }

    @Test
    public void withInstant() {
        SetTimeProvider tp = new SetTimeProvider(Instant.parse("2018-08-20T12:53:04.075Z"));
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584075000L, tp.currentTimeMicros());
        SetTimeProvider tp2 = new SetTimeProvider(Instant.parse("2018-08-20T12:53:04.075123Z"));
        assertEquals(1534769584075L, tp2.currentTimeMillis());
        assertEquals(1534769584075123L, tp2.currentTimeMicros());
    }

    @Test
    public void autoIncrement() {
        SetTimeProvider tp = new SetTimeProvider("2018-08-20T12:53:04.075")
                .autoIncrement(1, TimeUnit.MILLISECONDS);
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584076L, tp.currentTimeMillis());
        assertEquals(1534769584077L, tp.currentTimeMillis());

    }
}
