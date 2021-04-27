package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.longs.LongBiCondition;
import net.openhft.chronicle.core.internal.invariant.longs.LongCondition;
import net.openhft.chronicle.core.internal.invariant.longs.LongTriCondition;

import java.util.function.Function;
import java.util.function.LongPredicate;

import static java.util.Objects.requireNonNull;

/**
 * A collection of functional compositions to check and assert various requirements
 * related to {@code long} values.
 */
public final class Longs {

    // Suppresses default constructor, ensuring non-instantiability.
    private Longs() {
    }

    /**
     * Returns the provided {@code value} after checking that it satisfies the provided {@code requirement} throwing
     * an {@link IllegalArgumentException} if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
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
    public static long require(final LongPredicate requirement,
                               final long value) {
        return require(requirement, value, IllegalArgumentException::new);
    }

    /**
     * Returns the provided {@code value} after checking that it satisfies the provided {@code requirement} throwing
     * a custom exception if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
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
    public static <X extends RuntimeException> long require(final LongPredicate requirement,
                                                            final long value,
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
     * public Foo(long bar) {
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
    public static long require(final LongBiPredicate requirement,
                               final long value,
                               final long otherValue) {
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
     * public Foo(long bar) {
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
    public static <X extends RuntimeException> long require(final LongBiPredicate requirement,
                                                            final long value,
                                                            final long otherValue,
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
     * public Foo(long bar) {
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
    public static long require(final LongTriPredicate requirement,
                               final long value,
                               final long otherFirstValue,
                               final long otherSecondValue) {
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
     * public Foo(long bar) {
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
    public static <X extends RuntimeException> long require(final LongTriPredicate requirement,
                                                            final long value,
                                                            final long otherFirstValue,
                                                            final long otherSecondValue,
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
     * private Foo(long bar) {
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
    public static void assertIfEnabled(final LongPredicate requirement,
                                       final long value) {
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
     * private Foo(long bar) {
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
    public static void assertIfEnabled(final LongBiPredicate requirement,
                                       final long value,
                                       final long otherValue) {
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
     * public private(long bar) {
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
    public static void assertIfEnabled(final LongTriPredicate requirement,
                                       final long value,
                                       final long otherFirstValue,
                                       final long otherSecondValue) {
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
                                         final long value) {
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
                                         final long otherValue) {
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
                                         final long otherSecondValue) {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s (%d, %d)", value, value, requirement, otherFirstValue, otherSecondValue);
    }

    /**
     * Returns a predicate that can test if a value is <em>positive</em> (i.e. value > 0).
     *
     * @return a predicate that can test if a value is <em>positive</em> (i.e. value > 0)
     */
    public static LongPredicate positive() {
        return LongCondition.POSITIVE;
    }

    /**
     * Returns a predicate that can test if a value is <em>negative</em> (i.e. value < 0).
     *
     * @return a predicate that can test if a value is <em>negative</em> (i.e. value < 0)
     */
    public static LongPredicate negative() {
        return LongCondition.NEGATIVE;
    }

    /**
     * Returns a predicate that can test if a value is <em>zero</em> (i.e. value == 0).
     *
     * @return a predicate that can test if a value is <em>zero</em> (i.e. value == 0)
     */
    public static LongPredicate zero() {
        return LongCondition.ZERO;
    }

    /**
     * Returns a predicate that can test if a value can <em>fit in a {@code byte}</em> (i.e. value ∈ [-128, 127]").
     *
     * @return a predicate that can test if a value can <em>fit in a {@code byte}</em> (i.e. value ∈ [-128, 127]")
     */
    public static LongPredicate byteConvertible() {
        return LongCondition.BYTE_CONVERTIBLE;
    }

    /**
     * Returns a predicate that can test if a value can <em>fit in a {@code short}</em> (i.e. value ∈ [-32768, 32767]").
     *
     * @return a predicate that can test if a value can <em>fit in a {@code short}</em> (i.e. value ∈ [-32768, 32767]")
     */
    public static LongPredicate shortConvertible() {
        return LongCondition.SHORT_CONVERTIBLE;
    }

    /**
     * Returns a predicate that can test if a value is an <em>even power of two</em>
     * (i.e. log2(value) is an integer).
     *
     * @return a predicate that can test if a value is an <em>even power of two</em>
     * (i.e. log2(value) is an integer)
     */
    public static LongPredicate evenPowerOfTwo() {
        return LongCondition.EVEN_POWER_OF_TWO;
    }

    /**
     * Returns a predicate that can test if a value is <em>equal to</em> another value.
     *
     * @return a predicate that can test if a value is <em>equal to</em> another value
     */
    public static LongBiPredicate equalTo() {
        return LongBiCondition.EQUAL_TO;
    }

    /**
     * Returns a predicate that can test if a value is <em>greater than</em> to another value.
     *
     * @return a predicate that can test if a value is <em>greater than</em> to another value
     */
    public static LongBiPredicate greaterThan() {
        return LongBiCondition.GREATER_THAN;
    }

    /**
     * Returns a predicate that can test if a value is <em>greater or equal</em> to another value.
     *
     * @return a predicate that can test if a value is <em>greater or equal</em> to another value
     */
    public static LongBiPredicate greaterOrEqual() {
        return LongBiCondition.GREATER_OR_EQUAL;
    }

    /**
     * Returns a predicate that can test if a value is <em>less than</em> to another value.
     *
     * @return a predicate that can test if a value is <em>less than</em> to another value
     */
    public static LongBiPredicate lessThan() {
        return LongBiCondition.LESS_THAN;
    }

    /**
     * Returns a predicate that can test if a value is <em>less or equal</em> to another value.
     *
     * @return a predicate that can test if a value is <em>less or equal</em> to another value
     */
    public static LongBiPredicate lessOrEqual() {
        return LongBiCondition.LESS_OR_EQUAL;
    }

    /**
     * Returns a predicate that can test if a value is <em>between zero and</em> another value (exclusive)
     * (i.e value ∈ [0, other value) ).
     *
     * @return a predicate that can test if a value is <em>between zero and</em> another value (exclusive)
     * (i.e value ∈ [0, other value) )
     */
    public static LongBiPredicate betweenZeroAnd() {
        return LongBiCondition.BETWEEN_ZERO_AND;
    }

    /**
     * Returns a predicate that can test if a value is <em>between zero and</em> another value (inclusive)
     * (i.e value ∈ [0, other value] ).
     *
     * @return a predicate that can test if a value is <em>between zero and</em> another value (inclusive)
     * (i.e value ∈ [0, other value] )
     */
    public static LongBiPredicate betweenZeroAndClosed() {
        return LongBiCondition.BETWEEN_ZERO_AND_CLOSED;
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
    public static LongBiPredicate powerOfTwo() {
        return LongBiCondition.POWER_OF_TWO;
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
    public static LongBiPredicate log2() {
        return LongBiCondition.LOG2;
    }

    /**
     * Returns a predicate that can test if a value is <em>between</em> another first value (inclusive)
     * and another second value (exclusive) (i.e value ∈ [other first value , other second value) ).
     *
     * @return a predicate that can test if a value is <em>between</em> another first value (inclusive)
     * and another second value (exclusive) (i.e value ∈ [other first value , other second value) )
     */
    public static LongTriPredicate between() {
        return LongTriCondition.BETWEEN;
    }

    /**
     * Returns a predicate that can test if a value is <em>between (closed)</em> another first value (inclusive)
     * and another second value (inclusive) (i.e value ∈ [other first value , other second value] ).
     *
     * @return a predicate that can test if a value is <em>between (closed)</em> another first value (inclusive)
     * and another second value (inclusive) (i.e value ∈ [other first value , other second value] ).
     */
    public static LongTriPredicate betweenClosed() {
        return LongTriCondition.BETWEEN_CLOSED;
    }

    /**
     * Returns a predicate that can test if a value is <em>between zero and</em> another first value (inclusive)
     * whilst ensuring a value of size defined by another second value can fit.
     * (i.e value ∈ [0, other first value - other second value] ).
     * <p>
     * This predicate is useful when ensuring that a memory structure can be updated without exceeding its upper
     * bounds. For example:
     * <blockquote><pre>
     * public static void putLong(byte[] bytes, long offset, long value) {
     *         assert nonNull(bytes);
     *         assert betweenZeroAndReserving.test(offset, bytes.length, Integer.BYTES);
     *         ...
     * </pre></blockquote>
     *
     * @return Returns a predicate that can test if a value is <em>between zero and</em> another first value (inclusive)
     * whilst ensuring a value of size defined by another second value can fit.
     * (i.e value ∈ [0, other first value - other second value] ).
     */
    public static LongTriPredicate betweenZeroAndReserving() {
        return LongTriCondition.BETWEEN_ZERO_AND_ENSURING;
    }

}