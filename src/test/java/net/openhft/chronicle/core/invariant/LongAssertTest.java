package net.openhft.chronicle.core.invariant;

import org.junit.Test;

import static org.junit.Assert.fail;

public class LongAssertTest {

    @Test
    public void assertPositive() {
        LongAssertions.assertPositive(1);
    }

    @Test
    public void assertPositiveButWasZero() {
        assertThrowsIfEnabled(() -> LongAssertions.assertPositive(0));
    }

    @Test
    public void assertPositiveButWasNegative() {
        assertThrowsIfEnabled(() -> LongAssertions.assertPositive(-1));
    }

    @Test
    public void assertNegative() {
        LongAssertions.assertNegative(-1);
    }

    @Test
    public void assertNegativeButWasZero() {
        assertThrowsIfEnabled(() -> LongAssertions.assertNegative(0));
    }

    @Test
    public void assertNegativeButWasPositive() {
        assertThrowsIfEnabled(() -> LongAssertions.assertNegative(1));
    }

    @Test
    public void assertZero() {
        LongAssertions.assertZero(0);
    }

    @Test
    public void assertZeroButWasOne() {
        assertThrowsIfEnabled(() -> LongAssertions.assertZero(1));
    }

    @Test
    public void assertZeroButWasMinusOne() {
        assertThrowsIfEnabled(() -> LongAssertions.assertZero(-1));
    }

    @Test
    public void assertNonPositive() {
        LongAssertions.assertNonPositive(-1);
        LongAssertions.assertNonPositive(0);
    }

    @Test
    public void assertNonPositiveButWasPositive() {
        assertThrowsIfEnabled(() -> LongAssertions.assertNegative(1));
    }

    @Test
    public void assertNonNegative() {
        LongAssertions.assertNonNegative(1);
        LongAssertions.assertNonNegative(0);

    }

    @Test
    public void assertNonNegativeButWasNegative() {
        assertThrowsIfEnabled(() -> LongAssertions.assertNonNegative(-1));
    }

    @Test
    public void assertNonZero() {
        LongAssertions.assertNonZero(-1);
        LongAssertions.assertNonZero(1);
    }

    @Test
    public void assertNonZeroButWasZero() {
        assertThrowsIfEnabled(() -> LongAssertions.assertNonZero(0));
    }

    @Test
    public void assertEquals() {
        LongAssertions.assertEquals(0, 0);
        LongAssertions.assertEquals(1, 1);
        LongAssertions.assertEquals(-1, -1);
    }

    @Test
    public void assertEqualsButWasNotEqual() {
        assertThrowsIfEnabled(() -> LongAssertions.assertEquals(42, 13));
    }

    @Test
    public void assertNotEquals() {
        LongAssertions.assertNotEquals(42, 13);
    }

    @Test
    public void assertNotEqualsButWasEqual() {
        assertThrowsIfEnabled(() -> LongAssertions.assertNotEquals(42, 42));
    }

    @Test
    public void assertInRange() {
        LongAssertions.assertInRange(42, -1, 100);
        LongAssertions.assertInRange(-10, -20, -2);
    }

    @Test
    public void assertInRangeButWasAbove() {
        assertThrowsIfEnabled(() -> LongAssertions.assertInRange(42, -1, 42));
    }

    @Test
    public void assertInRangeButWasBelow() {
        assertThrowsIfEnabled(() -> LongAssertions.assertInRange(-1, 0, 42));
    }

    @Test
    public void assertInRangeClosed() {
        LongAssertions.assertInRangeClosed(42, -1, 42);
        LongAssertions.assertInRangeClosed(42, -1, 100);
    }

    @Test
    public void assertInRangeClosedAbove() {
        assertThrowsIfEnabled(() -> LongAssertions.assertInRangeClosed(43, -1, 42));
    }

    @Test
    public void assertInRangeClosedBelow() {
        assertThrowsIfEnabled(() -> LongAssertions.assertInRangeClosed(-1 , 0, 42));
    }

    @Test
    public void assertInRangeZero() {
        LongAssertions.assertInRangeZero(42, 100);
    }

    @Test
    public void assertInRangeZeroAbove() {
        assertThrowsIfEnabled(() -> LongAssertions.assertInRangeZero(43, 42));
    }

    @Test
    public void assertInRangeZeroBelow() {
        assertThrowsIfEnabled(() -> LongAssertions.assertInRangeZero(43, -1));
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