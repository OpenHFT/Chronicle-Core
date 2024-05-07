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
     * @return {@code true}
     * @throws NullPointerException If the provided {@code requirement} is {@code null}. There is no guarantee that this
     *                              exception is thrown. For example, if assertions are not enabled, then the exception
     *                              might not be thrown.
     * @throws AssertionError       if the check fails and assertions are enabled both via the {@code -ea} JVM command
     *                              line option and by setting {@link AssertUtil#SKIP_ASSERTIONS} to {@code false}.
     */
    public static boolean assertIfEnabled(final LongBiPredicate requirement,
                                          final long value,
                                          final long otherValue) {
        assert AssertUtil.SKIP_ASSERTIONS || requirement.test(value, otherValue)
                : failDescription(requirement, value, otherValue);
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
     * @throws NullPointerException If the provided {@code requirement} is {@code null}.
     */
    private static String failDescription(final LongPredicate requirement,
                                         final long value) {
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
     * @throws NullPointerException If the provided {@code requirement} is {@code null}.
     */
    private static String failDescription(final LongBiPredicate requirement,
                                         final long value,
                                         final long otherValue) {
        return String.format("The provided value (%d) is illegal because it does not satisfy the provided requirement: %d %s %d", value, value, requirement, otherValue);
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
