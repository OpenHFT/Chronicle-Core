package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IntsTest {

    @Test
    public void requirePositive() {
        test(1, 0, Ints::requirePositive);
        test(1, -1, Ints::requirePositive);
    }

    @Test
    public void requireNegative() {
        test(-1, 0, Ints::requireNegative);
        test(-1, 1, Ints::requireNegative);
    }

    @Test
    public void requireZero() {
        test(0, 1, Ints::requireZero);
    }

    @Test
    public void requireNonPositive() {
        test(-1, 1, Ints::requireNonPositive);
        test(0, 1, Ints::requireNonPositive);
    }

    @Test
    public void requireNonNegative() {
        test(0, -1, Ints::requireNonNegative);
        test(1, -1, Ints::requireNonNegative);
    }

    @Test
    public void requireNonZero() {
        test(1, 0, Ints::requireNonZero);
    }

    @Test
    public void requireEquals() {
        test(1, 0, v -> Ints.requireEquals(v, 1));
    }

    @Test
    public void requireNotEquals() {
        test(1, 0, v -> Ints.requireNotEquals(v, 0));
    }

    @Test
    public void requireInRange() {
        test(0, 16, v -> Ints.requireInRange(v, 0, 16));
        test(15, -1, v -> Ints.requireInRange(v, 0, 16));
    }

    @Test
    public void requireInRangeClosed() {
        test(0, 17, v -> Ints.requireInRangeClosed(v, 0, 16));
        test(16, -1, v -> Ints.requireInRangeClosed(v, 0, 16));
    }

    private void test(final int happy,
                      final int sad,
                      @NotNull final IntUnaryOperator mapper) {
        final long result = mapper.applyAsInt(happy);
        assertEquals(happy, result);
        try {
            final long result2 = mapper.applyAsInt(sad);
            fail(result2 + " is not valid!");
        } catch (IllegalArgumentException ignored) {
            // Happy path
        }
    }

}