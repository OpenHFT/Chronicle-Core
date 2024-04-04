/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.assertions.AssertUtil;
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
     * @return {@code true}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}. There is no guarantee that this
     *                              exception is thrown. For example, if assertions are not enabled, then the exception
     *                              might not be thrown.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static boolean assertIfEnabled(final IntTriPredicate requirement,
                                          final int value,
                                          final int otherFirstValue,
                                          final int otherSecondValue) {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value, otherFirstValue, otherSecondValue)
                : failDescription(requirement, value, otherFirstValue, otherSecondValue);
        return true;
    }

    /**
     * Returns a human-readable form of a failure message provided that the provided {@code value} <em>did not</em>
     * satisfy the provided {@code requirement}.
     *
     * @param requirement to imposed on the provided values
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
     * Returns a human-readable form of a failure message provided that the provided {@code value} and
     * provided {@code otherValue} <em>did not</em> satisfy the provided {@code requirement}.
     *
     * @param requirement to imposed on the provided values
     * @param value       the value to check
     * @param otherValue  the other value to compare against the provided {@code value}
     * @return a human-readable form of a failure message provided that the provided {@code value} and
     * provided {@code otherValue} <em>did not</em> satisfy the provided {@code requirement}
     * @throws NullPointerException if the provided {@code requirement} is {@code null}.
     */
    public static String failDescription(final IntBiPredicate requirement,
                                         final int value,
                                         final int otherValue) {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s %d", value, value, requirement, otherValue);
    }

    /**
     * Returns a human-readable form of a failure message provided that the provided {@code value},
     * provided {@code otherFirstValue} and provided {@code otherFirstValue} <em>did not</em> satisfy the
     * provided {@code requirement}.
     *
     * @param requirement to imposed on the provided values
     * @param value       the value to check
     * @return a human-readable form of a failure message provided that the provided {@code value} and
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
     * Returns a predicate that can test if a value is <em>non-negative</em> (i.e. value >= 0).
     * <p>
     * This is equivalent to: {@code negative().negate()}
     *
     * @return a predicate that can test if a value is <em>non-negative</em> (i.e. value >= 0)
     */
    public static IntPredicate nonNegative() {
        return IntCondition.NON_NEGATIVE;
    }
}
