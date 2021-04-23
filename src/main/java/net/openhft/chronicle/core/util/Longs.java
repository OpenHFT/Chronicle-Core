package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.*;

import java.util.function.LongPredicate;

import static net.openhft.chronicle.core.internal.util.RangeUtil.*;

public final class Longs {

    // Suppresses default constructor, ensuring non-instantiability.
    private Longs() {
    }

    /**
     * Checks that the provided {@code value} satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.require(LongCondition.NON_NEGATIVE, bar);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value}
     * @param value       the value to check
     * @return the provided {@code value} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long require(final LongPredicate requirement,
                               final long value) throws IllegalArgumentException, NullPointerException {
        if (!requirement.test(value))
            throw new IllegalArgumentException(failDescription(requirement, value));
        return value;
    }

    /**
     * Checks that the provided {@code value} and provided {@code otherValue} satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.require(LongBiCondition.GREATER_THAN, bar, 42);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value} and {@code otherValue}
     * @param value       the value to check
     * @param otherValue  the other value to compare against the provided {@code value}
     * @return the provided {@code value} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long require(final LongBiPredicate requirement,
                               final long value,
                               final long otherValue) throws IllegalArgumentException, NullPointerException {
        if (!requirement.test(value, otherValue))
            throw new IllegalArgumentException(failDescription(requirement, value, otherValue));
        return value;
    }

    /**
     * Checks that the provided {@code value}, provided {@code otherFirstValue} and provided {@code otherSecondValue}
     * satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.require(LongTriCondition.BETWEEN, bar, 13, 42);
     * }
     * </pre></blockquote>
     *
     * @param requirement      to impose on the provided values
     * @param value            the value to check
     * @param otherFirstValue  the other first value to compare against the provided {@code value}
     * @param otherSecondValue the other first value to compare against the provided {@code value}
     * @return the provided {@code value} if the check passes
     * @throws NullPointerException     if the provided {@code requirement} is {@code null}.
     * @throws IllegalArgumentException if the check fails
     */
    public static long require(final LongTriPredicate requirement,
                               final long value,
                               final long otherFirstValue,
                               final long otherSecondValue) throws IllegalArgumentException, NullPointerException {
        if (!requirement.test(value, otherFirstValue, otherSecondValue))
            throw new IllegalArgumentException(failDescription(requirement, value, otherFirstValue, otherSecondValue));
        return value;
    }

    /**
     * Asserts that the provided {@code value} satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.require(LongCondition.NON_NEGATIVE, bar);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value}
     * @param value       the value to check
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static void assertIfEnabled(final LongPredicate requirement,
                                       final long value) throws AssertionError, NullPointerException {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value) : failDescription(requirement, value);
    }

    /**
     * Checks that the provided {@code value} and provided {@code otherValue} satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.require(LongBiCondition.GREATER_THAN, bar, 42);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value} and {@code otherValue}
     * @param value       the value to check
     * @param otherValue  the other value to compare against the provided {@code value}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static void assertIfEnabled(final LongBiPredicate requirement,
                                       final long value,
                                       final long otherValue) throws AssertionError, NullPointerException {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value, otherValue) : failDescription(requirement, value, otherValue);
    }

    /**
     * Checks that the provided {@code value}, provided {@code otherFirstValue} and provided {@code otherSecondValue}
     * satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.require(LongTriCondition.BETWEEN, bar, 13, 42);
     * }
     * </pre></blockquote>
     *
     * @param requirement      to impose on the provided values
     * @param value            the value to check
     * @param otherFirstValue  the other first value to compare against the provided {@code value}
     * @param otherSecondValue the other first value to compare against the provided {@code value}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static void assertIfEnabled(final LongTriPredicate requirement,
                                       final long value,
                                       final long otherFirstValue,
                                       final long otherSecondValue) throws AssertionError, NullPointerException {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value, otherFirstValue, otherSecondValue)
                : failDescription(requirement, value, otherFirstValue, otherSecondValue);
    }

    /**
     * Returns a human readable form of a failure message provided that the provided {@code value} <em>did not</em>
     * satisfy the provided {@code requirement}.
     *
     * @param requirement to imposed on the provided values
     * @param value       the value to check
     * @return a human readable form of a failure message provided that the provided {@code value} <em>did not</em>
     * satisfy the provided {@code requirement}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     */
    public static String failDescription(final LongPredicate requirement,
                                         final long value) throws NullPointerException {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s", value, value, requirement);
    }

    /**
     * Returns a human readable form of a failure message provided that the provided {@code value} and
     * provided {@code otherValue} <em>did not</em> satisfy the provided {@code requirement}.
     *
     * @param requirement to imposed on the provided values
     * @param value       the value to check
     * @param otherValue  the other value to compare against the provided {@code value}
     * @return a human readable form of a failure message provided that the provided {@code value} and
     * provided {@code otherValue} <em>did not</em> satisfy the provided {@code requirement}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     */
    public static String failDescription(final LongBiPredicate requirement,
                                         final long value,
                                         final long otherValue) throws NullPointerException {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s %d", value, value, requirement, otherValue);
    }

    /**
     * Returns a human readable form of a failure message provided that the provided {@code value},
     * provided {@code otherFirstValue} and provided {@code otherFirstValue} <em>did not</em> satisfy the
     * provided {@code requirement}.
     *
     * @param requirement to imposed on the provided values
     * @param value       the value to check
     * @return a human readable form of a failure message provided that the provided {@code value} and
     * provided {@code otherValue} <em>did not</em> satisfy the provided {@code requirement}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     */
    public static String failDescription(final LongTriPredicate requirement,
                                         final long value,
                                         final long otherFirstValue,
                                         final long otherSecondValue) throws NullPointerException {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s (%d, %d)", value, value, requirement, otherFirstValue, otherSecondValue);
    }

    /**
     * Checks that the provided {@code val} is positive (i.e. {@code val > 0})
     * <p>
     * This method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = Longs.requirePositive(bar);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requirePositive(@Positive final long val) throws IllegalArgumentException {
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
    public static long requireNegative(@Negative final long val) throws IllegalArgumentException {
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
    public static long requireZero(final long val) throws IllegalArgumentException {
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
    public static long requireNonPositive(@NonPositive final long val) throws IllegalArgumentException {
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
    public static long requireNonNegative(@NonNegative final long val) throws IllegalArgumentException {
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
    public static long requireNonZero(final long val) throws IllegalArgumentException {
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
    public static long requireEquals(final long val, final long otherVal) throws IllegalArgumentException {
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
    public static long requireNotEquals(final long val, final long otherVal) throws IllegalArgumentException {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireInRange(bar, 8, 16);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireInRange(@Range final long val,
                                      final long from,
                                      final long toExclusive) throws IllegalArgumentException {
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
     * public Foo(long bar) {
     *     this.bar = Longs.requireInRangeClosed(bar, 8, 15);
     * }
     * </pre></blockquote>
     *
     * @param val the value to check
     * @return the provided {@code val} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static long requireInRangeClosed(@Range final long val,
                                            final long from,
                                            final long toInclusive) throws IllegalArgumentException {
        if (val < from || val > toInclusive)
            throw new IllegalArgumentException(val + IS_NOT_IN_THE_RANGE + from + ", " + toInclusive + "]");
        return val;
    }
}