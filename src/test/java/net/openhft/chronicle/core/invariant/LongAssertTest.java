package net.openhft.chronicle.core.invariant;

import org.junit.Test;

import static org.junit.Assert.fail;

public class LongAssertTest {

    @Test
    public void assertPositive() {
        LongAssert.assertPositive(1);
    }

    @Test
    public void assertPositiveButWasZero() {
        assertThrowsIfEnabled(() -> LongAssert.assertPositive(0));
    }

    @Test
    public void assertPositiveButWasNegative() {
        assertThrowsIfEnabled(() -> LongAssert.assertPositive(-1));
    }

    @Test
    public void assertNegative() {
        LongAssert.assertNegative(-1);
    }

    @Test
    public void assertNegativeButWasZero() {
        assertThrowsIfEnabled(() -> LongAssert.assertNegative(0));
    }

    @Test
    public void assertNegativeButWasPositive() {
        assertThrowsIfEnabled(() -> LongAssert.assertNegative(1));
    }

    @Test
    public void assertZero() {
        LongAssert.assertZero(0);
    }

    @Test
    public void assertZeroButWasOne() {
        assertThrowsIfEnabled(() -> LongAssert.assertZero(1));
    }

    @Test
    public void assertZeroButWasMinusOne() {
        assertThrowsIfEnabled(() -> LongAssert.assertZero(-1));
    }

    @Test
    public void assertNonPositive() {
        LongAssert.assertNonPositive(-1);
        LongAssert.assertNonPositive(0);
    }

    @Test
    public void assertNonPositiveButWasPositive() {
        assertThrowsIfEnabled(() -> LongAssert.assertNegative(1));
    }

    @Test
    public void assertNonNegative() {
        LongAssert.assertNonNegative(1);
        LongAssert.assertNonNegative(0);

    }

    @Test
    public void assertNonNegativeButWasNegative() {
        assertThrowsIfEnabled(() -> LongAssert.assertNonNegative(-1));
    }

    @Test
    public void assertNonZero() {
        LongAssert.assertNonZero(-1);
        LongAssert.assertNonZero(1);
    }

    @Test
    public void assertNonZeroButWasZero() {
        assertThrowsIfEnabled(() -> LongAssert.assertNonZero(0));
    }

    @Test
    public void assertEquals() {
        LongAssert.assertEquals(0, 0);
        LongAssert.assertEquals(1, 1);
        LongAssert.assertEquals(-1, -1);
    }

    @Test
    public void assertEqualsButWasNotEqual() {
        assertThrowsIfEnabled(() -> LongAssert.assertEquals(42, 13));
    }

    @Test
    public void assertNotEquals() {
        LongAssert.assertNotEquals(42, 13);
    }

    @Test
    public void assertNotEqualsButWasEqual() {
        assertThrowsIfEnabled(() -> LongAssert.assertNotEquals(42, 42));
    }

    @Test
    public void assertInRange() {
        LongAssert.assertInRange(42, -1, 100);
        LongAssert.assertInRange(-10, -20, -2);
    }

    @Test
    public void assertInRangeButWasAbove() {
        assertThrowsIfEnabled(() -> LongAssert.assertInRange(42, -1, 42));
    }

    @Test
    public void assertInRangeButWasBelow() {
        assertThrowsIfEnabled(() -> LongAssert.assertInRange(-1, 0, 42));
    }

    @Test
    public void assertInRangeClosed() {
        LongAssert.assertInRangeClosed(42, -1, 42);
        LongAssert.assertInRangeClosed(42, -1, 100);
    }

    @Test
    public void assertInRangeClosedAbove() {
        assertThrowsIfEnabled(() -> LongAssert.assertInRangeClosed(43, -1, 42));
    }

    @Test
    public void assertInRangeClosedBelow() {
        assertThrowsIfEnabled(() -> LongAssert.assertInRangeClosed(-1 , 0, 42));
    }

    @Test
    public void assertInRangeZero() {
        LongAssert.assertInRangeZero(42, 100);
    }

    @Test
    public void assertInRangeZeroAbove() {
        assertThrowsIfEnabled(() -> LongAssert.assertInRangeZero(43, 42));
    }

    @Test
    public void assertInRangeZeroBelow() {
        assertThrowsIfEnabled(() -> LongAssert.assertInRangeZero(43, -1));
    }

    private void assertThrowsIfEnabled(Runnable runnable) {
        if (AssertUtil.USE_ASSERTIONS) {
            try {
                runnable.run();
                fail(runnable + " did not throw an AssertionError");
            } catch (AssertionError ae) {
                // ignore
            }
        }
    }

}