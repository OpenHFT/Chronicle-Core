package net.openhft.chronicle.core.util;

import static net.openhft.chronicle.core.internal.util.RangeUtil.*;

public final class Longs {

    private Longs() {}

    /**
     * Checks that the provided {@code val} is positive (i.e. {@code val > 0})
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Longs.requirePositive(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requirePositive(final long val) {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireNegative(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireNegative(final long val) {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireZero(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireZero(final long val) {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireNonPositive(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireNonPositive(final long val) {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireNonNegative(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireNonNegative(final long val) {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireNonZero(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireNonZero(final long val) {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireEquals(bar, 42);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireEquals(final long val, final long otherVal) {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireNotEquals(bar, 13);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireNotEquals(final long val, final long otherVal) {
        if (val == otherVal)
            throw new IllegalArgumentException(val + IS_EQUAL_TO + otherVal);
        return val;
    }

    /**
     * Checks that the provided {@code val} is between the provided {@code first} (inclusive)
     * and the provided {@code lastExclusive} (exclusive) (i.e. {@code val ∈ [first, lastExclusive)}).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.requireInRange(bar, 8, 16);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireInRange(final long val,
                                     final long first,
                                     final long lastExclusive) {
        if (val < first || val >= lastExclusive)
            throw new IllegalArgumentException(val + IS_NOT_IN_THE_RANGE + first + ", " + lastExclusive + ")");
        return val;
    }

    /**
     * Checks that the provided {@code val} is between the provided {@code first} (inclusive)
     * and the provided {@code lastInclusive} (inclusive) (i.e. {@code val ∈ [first, lastExclusive]).
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.requireInRangeClosed(bar, 8, 15);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireInRangeClosed(final long val,
                                           final long first,
                                           final long lastInclusive) {
        if (val < first || val > lastInclusive)
            throw new IllegalArgumentException(val + IS_NOT_IN_THE_RANGE + first + ", " + lastInclusive + "]");
        return val;
    }

}