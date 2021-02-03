package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.function.LongUnaryOperator;

import static org.junit.Assert.*;

public class LongsTest {

    @Test
    public void requirePositive() {
        test(1, 0, Longs::requirePositive);
        test(1, -1, Longs::requirePositive);
    }

    @Test
    public void requireNegative() {
        test(-1, 0, Longs::requireNegative);
        test(-1, 1, Longs::requireNegative);
    }

    @Test
    public void requireZero() {
        test(0, 1, Longs::requireZero);
    }

    @Test
    public void requireNonPositive() {
        test(-1, 1, Longs::requireNonPositive);
        test(0, 1, Longs::requireNonPositive);
    }

    @Test
    public void requireNonNegative() {
        test(0, -1, Longs::requireNonNegative);
        test(1, -1, Longs::requireNonNegative);
    }

    @Test
    public void requireNonZero() {
        test(1, 0, Longs::requireNonZero);
    }

    @Test
    public void requireEquals() {
        test(1, 0, v -> Longs.requireEquals(v, 1));
    }

    @Test
    public void requireNotEquals() {
        test(1, 0, v -> Longs.requireNotEquals(v, 0));
    }

    @Test
    public void requireInRange() {
        test(0, 16, v -> Longs.requireInRange(v, 0, 16));
        test(15, -1, v -> Longs.requireInRange(v, 0, 16));
    }

    @Test
    public void requireInRangeClosed() {
        test(0, 17, v -> Longs.requireInRangeClosed(v, 0, 16));
        test(16, -1, v -> Longs.requireInRangeClosed(v, 0, 16));
    }

    private void test(final long happy,
                      final long sad,
                      @NotNull final LongUnaryOperator mapper) {
        final long result = mapper.applyAsLong(happy);
        assertEquals(happy, result);
        try {
            final long result2 = mapper.applyAsLong(sad);
            fail(result2 + " is not valid!");
        } catch (IllegalArgumentException ignored) {
            // Happy path
        }
    }

}