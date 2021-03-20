package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.*;

import static net.openhft.chronicle.core.internal.util.RangeUtil.*;

public final class Ints {

    private Ints() {}

    /**
     * Checks that the provided {@code val} is positive (i.e. {@code val > 0})
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requirePositive(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requirePositive(@Positive final int val) throws IllegalArgumentException {
        if (val < 1)
            throw new IllegalArgumentException(val + IS_NOT_POSITIVE);
        return val;
    }

    /**
     * Checks that the provided {@code val} is negative (i.e. {@code val < 0})
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireNegative(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireNegative(@Negative final int val) throws IllegalArgumentException {
        if (val > -1)
            throw new IllegalArgumentException(val + IS_NOT_NEGATIVE);
        return val;
    }

    /**
     * Checks that the provided {@code val} is zero (i.e. {@code val == 0})
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireZero(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireZero(final int val) throws IllegalArgumentException {
        if (val != 0)
            throw new IllegalArgumentException(val + IS_NOT_ZERO);
        return val;
    }

    /**
     * Checks that the provided {@code val} is non-positive (i.e. {@code val <= 0}).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireNonPositive(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireNonPositive(@NonPositive final int val) throws IllegalArgumentException {
        if (val > 0)
            throw new IllegalArgumentException(val + IS_POSITIVE);
        return val;
    }

    /**
     * Checks that the provided {@code val} is non-negative (i.e. {@code val >= 0}).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireNonNegative(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireNonNegative(@NonNegative final int val) throws IllegalArgumentException {
        if (val < 0)
            throw new IllegalArgumentException(val + IS_NEGATIVE);
        return val;
    }

    /**
     * Checks that the provided {@code val} is non-zero (i.e. {@code val != 0}).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireNonZero(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireNonZero(final int val) throws IllegalArgumentException {
        if (val == 0)
            throw new IllegalArgumentException(val + IS_ZERO);
        return val;
    }

    /**
     * Checks that the provided {@code val} equals the provided {@code otherVal} (i.e. {@code val == otherVal}).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireEquals(bar, 42);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireEquals(final int val, final int otherVal) throws IllegalArgumentException {
        if (val != otherVal)
            throw new IllegalArgumentException(val + IS_NOT_EQUAL_TO + otherVal);
        return val;
    }

    /**
     * Checks that the provided {@code val} does not equal the provided {@code otherVal} (i.e. {@code val != otherVal}).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireNotEquals(bar, 13);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireNotEquals(final int val, final int otherVal) throws IllegalArgumentException {
        if (val == otherVal)
            throw new IllegalArgumentException(val + IS_EQUAL_TO + otherVal);
        return val;
    }

    /**
     * Checks that the provided {@code val} is between the provided {@code from} (inclusive)
     * and the provided {@code toExclusive} (exclusive) (i.e. {@code val ∈ [from, toExclusive)}).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireInRange(bar, 8, 16);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireInRange(@Range final int val,
                                     final int from,
                                     final int toExclusive) throws IllegalArgumentException {
        if (val < from || val >= toExclusive)
            throw new IllegalArgumentException(val + IS_NOT_IN_THE_RANGE + from + ", " + toExclusive + ")");
        return val;
    }

    /**
     * Checks that the provided {@code val} is between the provided {@code from} (inclusive)
     * and the provided {@code toInclusive} (inclusive) (i.e. {@code val ∈ [from, lastExclusive]}).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.requireInRangeClosed(bar, 8, 15);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireInRangeClosed(@Range final int val,
                                           final int from,
                                           final int toInclusive) throws IllegalArgumentException {
        if (val < from || val > toInclusive)
            throw new IllegalArgumentException(val + IS_NOT_IN_THE_RANGE + from + ", " + toInclusive + "]");
        return val;
    }
}