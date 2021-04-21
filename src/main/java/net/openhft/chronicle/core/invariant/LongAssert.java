package net.openhft.chronicle.core.invariant;

import net.openhft.chronicle.core.annotation.*;
import net.openhft.chronicle.core.internal.util.RangeUtil;

import static net.openhft.chronicle.core.internal.util.RangeUtil.IS_NOT_IN_THE_RANGE;
import static net.openhft.chronicle.core.invariant.AssertUtil.USE_ASSERTIONS;

public final class LongAssert {

    // Suppresses default constructor, ensuring non-instantiability.
    private LongAssert() {
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is positive (i.e. unless {@code val > 0}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertPositive(bar);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertPositive(@Positive final long val) {
        if (USE_ASSERTIONS && val <= 0)
            throw new AssertionError(val + RangeUtil.IS_NOT_POSITIVE);
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is negative (i.e. unless {@code val < 0}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertNegative(bar);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertNegative(@Negative final long val) {
        if (USE_ASSERTIONS && val >= 0)
            throw new AssertionError(val + RangeUtil.IS_NOT_NEGATIVE);
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is zero (i.e. unless {@code val == 0}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertZero(bar);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertZero(final long val) {
        if (USE_ASSERTIONS && val != 0)
            throw new AssertionError(val + RangeUtil.IS_NOT_ZERO);
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is non-positive (i.e. unless {@code val <= 0}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertNonPositive(bar);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertNonPositive(@NonPositive final long val) {
        if (USE_ASSERTIONS && val > 0)
            throw new AssertionError(val + RangeUtil.IS_POSITIVE);
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is non-positive (i.e. unless {@code val >= 0}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertNonNegative(bar);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertNonNegative(@NonNegative final long val) {
        if (USE_ASSERTIONS && val < 0)
            throw new AssertionError(val + RangeUtil.IS_NEGATIVE);
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is non-zero (i.e. unless {@code val != 0}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertNonZero(bar);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertNonZero(final long val) {
        if (USE_ASSERTIONS && val == 0)
            throw new AssertionError(val + RangeUtil.IS_ZERO);
    }

    /**
     * Throws an AssertionError unless the provided {@code val} equals the provided {@code otherValue}
     * (i.e. unless {@code val == otherValue}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertEquals(bar, 42);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val        the value to check
     * @param otherValue to check against
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertEquals(final long val, final long otherValue) {
        if (USE_ASSERTIONS && val != otherValue)
            throw new AssertionError(val + RangeUtil.IS_NOT_EQUAL_TO + otherValue);
    }

    /**
     * Throws an AssertionError unless the provided {@code val} equals the provided {@code otherValue}
     * (i.e. unless {@code val != otherValue}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertNotEquals(bar, 13);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val        the value to check
     * @param otherValue to check against
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertNotEquals(final long val, final long otherValue) {
        if (USE_ASSERTIONS && val == otherValue)
            throw new AssertionError(val + RangeUtil.IS_EQUAL_TO + otherValue);
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is between the provided {@code from} (inclusive)
     * and the provided {@code toExclusive} (exclusive) (i.e. unless {@code val ∈ [from, toExclusive)}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertInRange(bar, 0, 1024);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val         the value to check
     * @param from        lower bounds (inclusive)
     * @param toExclusive upper bounds (exclusive)
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertInRange(@Range final long val,
                                     final long from,
                                     final long toExclusive) {
        if (USE_ASSERTIONS && (val < from || val >= toExclusive))
            throw new AssertionError(val + IS_NOT_IN_THE_RANGE + from + ", " + toExclusive + ")");
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is between the provided {@code from} (inclusive)
     * and the provided {@code toInclusive} (inclusive) (i.e. {@code val ∈ [from, lastExclusive]}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertInRangeClosed(bar, 0, 1023);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val         the value to check
     * @param from        lower bounds (inclusive)
     * @param toInclusive upper bounds (exclusive)
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertInRangeClosed(@Range final long val,
                                           final long from,
                                           final long toInclusive) {
        if (USE_ASSERTIONS && (val < from || val > toInclusive))
            throw new AssertionError(val + IS_NOT_IN_THE_RANGE + from + ", " + toInclusive + "]");
    }

    /**
     * Throws an AssertionError unless the provided {@code val} is between zero (inclusive)
     * and the provided {@code toExclusive} (exclusive) (i.e. unless {@code val ∈ [0, toExclusive)}).
     * <p>
     * This method is designed primarily for doing parameter assertions in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(long bar) {
     *     LongAssert.assertInRangeZero(bar, 1024);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param val         the value to check
     * @param toExclusive upper bounds (exclusive)
     * @throws AssertionError if the provided value does not meet the requirements.
     */
    public static void assertInRangeZero(@Range(from = 0) final long val,
                                         final long toExclusive) {
        if (USE_ASSERTIONS && (val < 0 || val >= toExclusive))
            throw new AssertionError(val + IS_NOT_IN_THE_RANGE + 0 + ", " + toExclusive + ")");
    }

}