//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.longs.LongCondition;

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
     * Returns the provided {@code value} after checking that it is <em>non-negative</em> throwing
     * an {@link IllegalArgumentException} if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = requireNonNegative(bar);
     * }
     * </pre></blockquote>
     *
     * @param value the value to check
     * @return the provided {@code value} if the check passes
     * @throws IllegalArgumentException If the check fails
     */
    public static long requireNonNegative(final long value) {
        if (value < 0)
            throw new IllegalArgumentException("The provided value (" + value + ") is negative");
        return value;
    }

    /**
     * Returns the provided {@code value} after checking that it is <em>positive</em> throwing
     * an {@link IllegalArgumentException} if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(long bar) {
     *     this.bar = requirePositive(bar);
     * }
     * </pre></blockquote>
     *
     * @param value the value to check
     * @return the provided {@code value} if the check passes
     * @throws IllegalArgumentException If the check fails
     */
    public static long requirePositive(final long value) {
        if (value <= 0)
            throw new IllegalArgumentException("The provided value (" + value + ") is not positive");
        return value;
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
     * @throws NullPointerException If the provided {@code requirement} is {@code null} or if the provided
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
     * Returns a human-readable form of a failure message provided that the provided {@code value} <em>did not</em>
     * satisfy the provided {@code requirement}.
     *
     * @param requirement to imposed on the provided values
     * @param value       the value to check
     * @return a human-readable form of a failure message provided that the provided {@code value} <em>did not</em>
     * satisfy the provided {@code requirement}
     * @throws NullPointerException If the provided {@code requirement} is {@code null}.
     */
    private static String failDescription(final LongPredicate requirement,
                                         final long value) {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s", value, value, requirement);
    }

    /**
     * Returns a predicate that can test if a value is <em>non-negative</em> (i.e. value &gt;= 0).
     * <p>
     * This is equivalent to: {@code negative().negate()}
     *
     * @return a predicate that can test if a value is <em>non-negative</em> (i.e. value &gt;= 0)
     */
    public static LongPredicate nonNegative() {
        return LongCondition.NON_NEGATIVE;
    }
}
