package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LongsTest {

    @Test
    public void require1Arg() {
        test(1, 0, v -> Longs.require(Longs.positive(), v));
        test(1, -1, v -> Longs.require(Longs.positive(), v));
    }

    @Test
    public void require2Arg() {
        test(1, 0, v -> Longs.require(Longs.equalTo(), v, 1));
    }

    @Test
    public void require3Ard() {
        test(0, 16, v -> Longs.require(Longs.between(), v, 0, 16));
        test(15, -1, v -> Longs.require(Longs.between(), v, 0, 16));
    }

    private void test(final int happy,
                      final int sad,
                      @NotNull final LongUnaryOperator mapper) {
        try {
            final long result = mapper.applyAsLong(happy);
            assertEquals(happy, result);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
        try {
            final long result2 = mapper.applyAsLong(sad);
            fail(result2 + " is not valid!");
        } catch (IllegalArgumentException ignored) {
            // Happy path
        }
    }

    interface LongUnaryOperator {
        long applyAsLong(int happy) throws IllegalArgumentException;
    }

}