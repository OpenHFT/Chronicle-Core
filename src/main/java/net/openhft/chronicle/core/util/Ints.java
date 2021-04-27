package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.ints.IntBiCondition;
import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import net.openhft.chronicle.core.internal.invariant.ints.IntTriCondition;

import java.util.function.Function;
import java.util.function.IntPredicate;

import static java.util.Objects.requireNonNull;

/**
 * A collection of functional compositions to check and assert various requirements
 * related to {@code int} values.
 */
public final class Ints {

    // Suppresses default constructor, ensuring non-instantiability.
    private Ints() {
    }

    /**
     * Returns the provided {@code value} after checking that it satisfies the provided {@code requirement} throwing
     * an {@link IllegalArgumentException} if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = require(nonNegative(), bar);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value}
     * @param value       the value to check
     * @return the provided {@code value} if the check passes
     * @throws NullPointerException     if the provided {@code requirement} is {@code null}.
     * @throws IllegalArgumentException if the check fails
     */
    public static int require(final IntPredicate requirement,
                              final int value) {
        return require(requirement, value, IllegalArgumentException::new);
    }

    /**
     * Returns the provided {@code value} after checking that it satisfies the provided {@code requirement} throwing
     * a custom exception if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = require(byteConvertible(), bar, ArithmeticException::new);
     * }
     * </pre></blockquote>
     *
     * @param <X>             Exception typ to throw if the check fails
     * @param requirement     to impose on the provided {@code value}
     * @param value           the value to check
     * @param exceptionMapper to apply should the check fail
     * @return the provided {@code value} if the check passes
     * @throws NullPointerException if the provided {@code requirement} is {@code null} or if the provided
     *                              {@code exceptionMapper} is {@code null}.
     * @throws RuntimeException     of the specified type of the provided {@code exceptionMapper}
     */
    public static <X extends RuntimeException> int require(final IntPredicate requirement,
                                                           final int value,
                                                           final Function<String, X> exceptionMapper) {
        requireNonNull(exceptionMapper);
        if (!requirement.test(value))
            throw exceptionMapper.apply(failDescription(requirement, value));
        return value;
    }

    /**
     * Returns the provided {@code value} after checking that it and the provided {@code otherValue} satisfies the
     * provided {@code requirement} throwing an {@link IllegalArgumentException} if the check fails.
     * <p>
     * Checks that the provided {@code value} and provided {@code otherValue} satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = require(betweenZeroAnd(), bar, 32);
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value} and {@code otherValue}
     * @param value       the value to check
     * @param otherValue  the other value to compare against the provided {@code value}
     * @return the provided {@code value} if the check passes
     * @throws NullPointerException     if the provided {@code requirement} is {@code null}.
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
     * Returns the provided {@code value} after checking that it and the provided {@code otherValue} satisfies the
     * provided {@code requirement} throwing an {@link IllegalArgumentException} if the check fails.
     * <p>
     * Checks that the provided {@code value} and provided {@code otherValue} satisfies the provided {@code requirement}.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = require(betweenZeroAnd(), bar, 16, 32, ArithmeticException::new);
     * }
     * </pre></blockquote>
     *
     * @param <X>             Exception typ to throw if the check fails
     * @param requirement     to impose on the provided {@code value} and {@code otherValue}
     * @param value           the value to check
     * @param otherValue      the other value to compare against the provided {@code value}
     * @param exceptionMapper to apply should the check fail
     * @return the provided {@code value} if the check passes
     * @throws NullPointerException     if the provided {@code requirement} is {@code null}.
     * @throws IllegalArgumentException if the check fails
     */
    public static <X extends RuntimeException> int require(final IntBiPredicate requirement,
                                                           final int value,
                                                           final int otherValue,
                                                           final Function<String, X> exceptionMapper) {
        requireNonNull(exceptionMapper);
        if (!requirement.test(value, otherValue))
            throw exceptionMapper.apply(failDescription(requirement, value, otherValue));
        return value;
    }

    /**
     * Returns the provided {@code value} after checking that it and the provided {@code otherValue}
     * and the provided {@code otherSecondValue} satisfies the provided {@code requirement}
     * throwing an {@link IllegalArgumentException} if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = require(between(), bar, 16, 32);
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
        return require(requirement, value, otherFirstValue, otherSecondValue, IllegalArgumentException::new);
    }

    /**
     * Returns the provided {@code value} after checking that it and the provided {@code otherValue}
     * and the provided {@code otherSecondValue} satisfies the provided {@code requirement}
     * throwing an {@link IllegalArgumentException} if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = require(between(), bar, 16, 32, ArithmeticException::new);
     * }
     * </pre></blockquote>
     *
     * @param <X>              Exception typ to throw if the check fails
     * @param requirement      to impose on the provided values
     * @param value            the value to check
     * @param otherFirstValue  the other first value to compare against the provided {@code value}
     * @param otherSecondValue the other first value to compare against the provided {@code value}
     * @param exceptionMapper  to apply should the check fail
     * @return the provided {@code value} if the check passes
     * @throws NullPointerException     if the provided {@code requirement} is {@code null}.
     * @throws IllegalArgumentException if the check fails
     */
    public static <X extends RuntimeException> int require(final IntTriPredicate requirement,
                                                           final int value,
                                                           final int otherFirstValue,
                                                           final int otherSecondValue,
                                                           final Function<String, X> exceptionMapper) {
        requireNonNull(exceptionMapper);
        if (!requirement.test(value, otherFirstValue, otherSecondValue))
            throw exceptionMapper.apply(failDescription(requirement, value, otherFirstValue, otherSecondValue));
        return value;
    }

    /**
     * Asserts that the provided {@code value} satisfies the provided {@code requirement} if
     * assertions is enabled (i.e. {@code -ea}) and {@link AssertUtil#SKIP_ASSERTIONS} is {@code false}.
     * <p>
     * This method is designed primarily for doing parameter validation in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(int bar) {
     *     assertIfEnabled(nonNegative, bar);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value}
     * @param value       the value to check
     * @throws NullPointerException if the provided {@code requirement} is {@code null}. There is no guarantee that this
     *                              exception is thrown. For example, if assertions are not enabled, then the exception
     *                              might not be thrown.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static void assertIfEnabled(final IntPredicate requirement,
                                       final int value) {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value)
                : failDescription(requirement, value);
    }

    /**
     * Asserts that the provided {@code value} and provided {@code otherValue} satisfies the provided {@code requirement}
     * if assertions is enabled (i.e. {@code -ea}) and {@link AssertUtil#SKIP_ASSERTIONS} is {@code false}.
     * <p>
     * This method is designed primarily for doing parameter validation in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * private Foo(int bar) {
     *     assertIfEnabled(betweenZeroAnd(), bar, 32);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param requirement to impose on the provided {@code value} and {@code otherValue}
     * @param value       the value to check
     * @param otherValue  the other value to compare against the provided {@code value}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}. There is no guarantee that this
     *                              exception is thrown. For example, if assertions are not enabled, then the exception
     *                              might not be thrown.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static void assertIfEnabled(final IntBiPredicate requirement,
                                       final int value,
                                       final int otherValue) {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value, otherValue)
                : failDescription(requirement, value, otherValue);
    }

    /**
     * Asserts that the provided {@code value}, provided {@code otherFirstValue} and provided {@code otherSecondValue}
     * satisfies the provided {@code requirement} if assertions is enabled (i.e. {@code -ea}) and
     * {@link AssertUtil#SKIP_ASSERTIONS} is {@code false}.
     * <p>
     * This method is designed primarily for doing parameter validation in private methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public private(int bar) {
     *     assertIfEnabled(between(), bar, 13, 42);
     *     this.bar = bar;
     * }
     * </pre></blockquote>
     *
     * @param requirement      to impose on the provided values
     * @param value            the value to check
     * @param otherFirstValue  the other first value to compare against the provided {@code value}
     * @param otherSecondValue the other first value to compare against the provided {@code value}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}. There is no guarantee that this
     *                              exception is thrown. For example, if assertions are not enabled, then the exception
     *                              might not be thrown.
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
     * Returns a predicate that can test if a value is <em>positive</em> (i.e. value > 0).
     *
     * @return a predicate that can test if a value is <em>positive</em> (i.e. value > 0)
     */
    public static IntPredicate positive() {
        return IntCondition.POSITIVE;
    }

    /**
     * Returns a predicate that can test if a value is <em>negative</em> (i.e. value < 0).
     *
     * @return a predicate that can test if a value is <em>negative</em> (i.e. value < 0)
     */
    public static IntPredicate negative() {
        return IntCondition.NEGATIVE;
    }

    /**
     * Returns a predicate that can test if a value is <em>zero</em> (i.e. value == 0).
     *
     * @return a predicate that can test if a value is <em>zero</em> (i.e. value == 0)
     */
    public static IntPredicate zero() {
        return IntCondition.ZERO;
    }

    /**
     * Returns a predicate that can test if a value can <em>fit in a {@code byte}</em> (i.e. value ∈ [-128, 127]").
     *
     * @return a predicate that can test if a value can <em>fit in a {@code byte}</em> (i.e. value ∈ [-128, 127]")
     */
    public static IntPredicate byteConvertible() {
        return IntCondition.BYTE_CONVERTIBLE;
    }

    /**
     * Returns a predicate that can test if a value can <em>fit in a {@code short}</em> (i.e. value ∈ [-32768, 32767]").
     *
     * @return a predicate that can test if a value can <em>fit in a {@code short}</em> (i.e. value ∈ [-32768, 32767]")
     */
    public static IntPredicate shortConvertible() {
        return IntCondition.SHORT_CONVERTIBLE;
    }

    /**
     * Returns a predicate that can test if a value is an <em>even power of two</em>
     * (i.e. log2(value) is an integer).
     *
     * @return a predicate that can test if a value is an <em>even power of two</em>
     * (i.e. log2(value) is an integer)
     */
    public static IntPredicate evenPowerOfTwo() {
        return IntCondition.EVEN_POWER_OF_TWO;
    }

    /**
     * Returns a predicate that can test if a value is <em>equal to</em> another value.
     *
     * @return a predicate that can test if a value is <em>equal to</em> another value
     */
    public static IntBiPredicate equalTo() {
        return IntBiCondition.EQUAL_TO;
    }

    /**
     * Returns a predicate that can test if a value is <em>greater than</em> to another value.
     *
     * @return a predicate that can test if a value is <em>greater than</em> to another value
     */
    public static IntBiPredicate greaterThan() {
        return IntBiCondition.GREATER_THAN;
    }

    /**
     * Returns a predicate that can test if a value is <em>greater or equal</em> to another value.
     *
     * @return a predicate that can test if a value is <em>greater or equal</em> to another value
     */
    public static IntBiPredicate greaterOrEqual() {
        return IntBiCondition.GREATER_OR_EQUAL;
    }

    /**
     * Returns a predicate that can test if a value is <em>less than</em> to another value.
     *
     * @return a predicate that can test if a value is <em>less than</em> to another value
     */
    public static IntBiPredicate lessThan() {
        return IntBiCondition.LESS_THAN;
    }

    /**
     * Returns a predicate that can test if a value is <em>less or equal</em> to another value.
     *
     * @return a predicate that can test if a value is <em>less or equal</em> to another value
     */
    public static IntBiPredicate lessOrEqual() {
        return IntBiCondition.LESS_OR_EQUAL;
    }

    /**
     * Returns a predicate that can test if a value is <em>between zero and</em> another value (exclusive)
     * (i.e value ∈ [0, other value) ).
     *
     * @return a predicate that can test if a value is <em>between zero and</em> another value (exclusive)
     * (i.e value ∈ [0, other value) )
     */
    public static IntBiPredicate betweenZeroAnd() {
        return IntBiCondition.BETWEEN_ZERO_AND;
    }

    /**
     * Returns a predicate that can test if a value is <em>between zero and</em> another value (inclusive)
     * (i.e value ∈ [0, other value] ).
     *
     * @return a predicate that can test if a value is <em>between zero and</em> another value (inclusive)
     * (i.e value ∈ [0, other value] )
     */
    public static IntBiPredicate betweenZeroAndClosed() {
        return IntBiCondition.BETWEEN_ZERO_AND_CLOSED;
    }

    /**
     * Returns a predicate that can test if a value is a <em>power of two</em> of another value
     * (i.e. value = 2 ^ other value).
     * For example,
     * {@code powerOfTwo().test(16, 4)} is {@code true} because 2^4 is 16
     *
     * @return a predicate that can test if a value is an <em>even power of two</em>
     * (i.e. an N exists such that value ^ N is an integer)
     */
    public static IntBiPredicate powerOfTwo() {
        return IntBiCondition.POWER_OF_TWO;
    }

    /**
     * Returns a predicate that can test if a value is <em>log2</em> of another value
     * (i.e. value = log2(other value) ).
     * <p>
     * For example,
     * {@code log2().test(4, 16)} is {@code true} because log2(16) is 4
     *
     * @return a predicate that can test if a value is <em>log2</em> of another value
     * (i.e. value = log2(other value) ).
     */
    public static IntBiPredicate log2() {
        return IntBiCondition.LOG2;
    }

    /**
     * Returns a predicate that can test if a value is <em>between</em> another first value (inclusive)
     * and another second value (exclusive) (i.e value ∈ [other first value , other second value) ).
     *
     * @return a predicate that can test if a value is <em>between</em> another first value (inclusive)
     * and another second value (exclusive) (i.e value ∈ [other first value , other second value) )
     */
    public static IntTriPredicate between() {
        return IntTriCondition.BETWEEN;
    }

    /**
     * Returns a predicate that can test if a value is <em>between (closed)</em> another first value (inclusive)
     * and another second value (inclusive) (i.e value ∈ [other first value , other second value] ).
     *
     * @return a predicate that can test if a value is <em>between (closed)</em> another first value (inclusive)
     * and another second value (inclusive) (i.e value ∈ [other first value , other second value] ).
     */
    public static IntTriPredicate betweenClosed() {
        return IntTriCondition.BETWEEN_CLOSED;
    }

    /**
     * Returns a predicate that can test if a value is <em>between zero and</em> another first value (inclusive)
     * whilst ensuring a value of size defined by another second value can fit.
     * (i.e value ∈ [0, other first value - other second value] ).
     * <p>
     * This predicate is useful when ensuring that a memory structure can be updated without exceeding its upper
     * bounds. For example:
     * <blockquote><pre>
     * public static void putInt(byte[] bytes, int offset, int value) {
     *         assert nonNull(bytes);
     *         assert betweenZeroAndReserving.test(offset, bytes.length, Integer.BYTES);
     *         ...
     * </pre></blockquote>
     *
     * @return Returns a predicate that can test if a value is <em>between zero and</em> another first value (inclusive)
     * whilst ensuring a value of size defined by another second value can fit.
     * (i.e value ∈ [0, other first value - other second value] ).
     */
    public static IntTriPredicate betweenZeroAndReserving() {
        return IntTriCondition.BETWEEN_ZERO_AND_ENSURING;
    }

}