package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.*;

import java.util.function.IntPredicate;

import static net.openhft.chronicle.core.internal.util.RangeUtil.*;

public final class Ints {

    // Suppresses default constructor, ensuring non-instantiability.
    private Ints() {
    }

    /**
     * Checks that the provided {@code value} satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.require(IntCondition.NON_NEGATIVE, bar);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value}
     * @param value       the value to check
     * @return the provided {@code value} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int require(final IntPredicate requirement,
                              final int value) {
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
     * public Foo(int bar) {
     *     this.bar = Ints.require(IntBiCondition.GREATER_THAN, bar, 42);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value} and {@code otherValue}
     * @param value       the value to check
     * @param otherValue  the other value to compare against the provided {@code value}
     * @return the provided {@code value} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int require(final IntBiPredicate requirement,
                              final int value,
                              final int otherValue) {
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
     * public Foo(int bar) {
     *     this.bar = Ints.require(IntTriCondition.BETWEEN, bar, 13, 42);
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
    public static int require(final IntTriPredicate requirement,
                              final int value,
                              final int otherFirstValue,
                              final int otherSecondValue) {
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
     * public Foo(int bar) {
     *     this.bar = Ints.require(IntCondition.NON_NEGATIVE, bar);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value}
     * @param value       the value to check
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static void assertIfEnabled(final IntPredicate requirement,
                                       final int value) {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value) : failDescription(requirement, value);
    }

    /**
     * Checks that the provided {@code value} and provided {@code otherValue} satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.require(IntBiCondition.GREATER_THAN, bar, 42);
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
    public static void assertIfEnabled(final IntBiPredicate requirement,
                                       final int value,
                                       final int otherValue) {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value, otherValue) : failDescription(requirement, value, otherValue);
    }

    /**
     * Checks that the provided {@code value}, provided {@code otherFirstValue} and provided {@code otherSecondValue}
     * satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = Ints.require(IntTriCondition.BETWEEN, bar, 13, 42);
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
    public static void assertIfEnabled(final IntTriPredicate requirement,
                                       final int value,
                                       final int otherFirstValue,
                                       final int otherSecondValue) {
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
    public static String failDescription(final IntPredicate requirement,
                                         final int value) {
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
    public static String failDescription(final IntBiPredicate requirement,
                                         final int value,
                                         final int otherValue) {
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
    public static String failDescription(final IntTriPredicate requirement,
                                         final int value,
                                         final int otherFirstValue,
                                         final int otherSecondValue) {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s (%d, %d)", value, value, requirement, otherFirstValue, otherSecondValue);
    }


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