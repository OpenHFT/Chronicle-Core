/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.assertions.AssertUtil;
import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;

import java.util.function.IntPredicate;

/**
 * A collection of functional compositions to check and assert various requirements
 * related to {@code int} values.
 */
public final class Ints {

    // Suppresses default constructor, ensuring non-instantiability.
    private Ints() {
    }

    /**
     * Returns the provided {@code value} after checking that it is <em>non-negative</em> throwing
     * an {@link IllegalArgumentException} if the check fails.
     * <p>
     * This method is designed primarily for doing parameter validation in public methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = requireNonNegative(bar);
     * }
     * </pre></blockquote>
     * <p>
     * This method is functionally equivalent to:
     * <blockquote><pre>
     *     require(negative().negate(), value);
     * </pre></blockquote>
     * but is potentially optimized for performance.
     *
     * @param value the value to check
     * @return the provided {@code value} if the check passes
     * @throws IllegalArgumentException if the check fails
     */
    public static int requireNonNegative(final int value) {
        if (value < 0)
            throw new IllegalArgumentException("The provided value (" + value + ") is negative");
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
     * @return {@code true}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}. There is no guarantee that this
     *                              exception is thrown. For example, if assertions are not enabled, then the exception
     *                              might not be thrown.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static boolean assertIfEnabled(final IntPredicate requirement,
                                          final int value) {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value)
                : failDescription(requirement, value);
        return true;
    }

    /**
     * Returns a human-readable form of a failure message provided that the provided {@code value} <em>did not</em>
     * satisfy the provided {@code requirement}.
     *
     * @param requirement to impose on the provided values
     * @param value       the value to check
     * @return a human-readable form of a failure message provided that the provided {@code value} <em>did not</em>
     * satisfy the provided {@code requirement}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     */
    public static String failDescription(final IntPredicate requirement,
                                         final int value) {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s", value, value, requirement);
    }

    /**
     * Returns a predicate that can test if a value is <em>non-negative</em> (i.e. value &gt;= 0).
     * <p>
     * This is equivalent to: {@code negative().negate()}
     *
     * @return a predicate that can test if a value is <em>non-negative</em> (i.e. value &gt;= 0)
     */
    public static IntPredicate nonNegative() {
        return IntCondition.NON_NEGATIVE;
    }
}
