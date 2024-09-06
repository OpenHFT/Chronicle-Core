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

package net.openhft.chronicle.core.internal.invariant.ints;

import net.openhft.chronicle.core.util.IntTriPredicate;

import static java.util.Objects.requireNonNull;

/**
 * Enum representing various conditions or predicates on three integer values, implementing {@link IntTriPredicate}.
 * <p>
 * This enum provides a set of predefined conditions for testing relationships between three integer values,
 * such as checking whether a value is between two other values. Each condition is defined by a symbolic operation
 * and an associated {@link IntTriPredicate} that performs the test.
 * </p>
 */
public enum IntTriCondition implements IntTriPredicate {

    /**
     * Condition representing that a value is between two other values (inclusive of the lower bound, exclusive of the upper bound).
     * <p>
     * Example: The condition checks whether {@code value} is in the range [fromInclusive, toExclusive).
     */
    BETWEEN("∈ [fromInclusive, toExclusive), where (fromInclusive, toExclusive) = ",
            (value, otherFirst, otherSecond) -> value >= otherFirst && value < otherSecond),

    /**
     * Condition representing that a value is between two other values (inclusive of both bounds).
     * <p>
     * Example: The condition checks whether {@code value} is in the range [fromInclusive, toInclusive].
     */
    BETWEEN_CLOSED("∈ [fromInclusive, toInclusive], where (fromInclusive, toInclusive) = ",
            (value, otherFirst, otherSecond) -> value >= otherFirst && value <= otherSecond),

    /**
     * Condition representing that a value is between 0 and the result of subtracting one value from another.
     * <p>
     * Example: The condition checks whether {@code value} is in the range [0, index - size].
     */
    BETWEEN_ZERO_AND_ENSURING("∈ [0, index - size], where (index, size) = ",
            (value, otherFirst, otherSecond) -> value >= 0 && value <= (otherFirst - otherSecond));

    // Symbolic representation of the condition
    private final String operation;

    // Predicate that defines the condition
    private final IntTriPredicate predicate;

    /**
     * Constructor for creating an {@link IntTriCondition} based on an operation string and predicate.
     *
     * @param operation The symbolic representation of the condition.
     * @param predicate The predicate defining the condition.
     */
    IntTriCondition(final String operation,
                    final IntTriPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    /**
     * Tests the condition on the provided three integer values.
     *
     * @param value       The integer value to test.
     * @param otherFirst  The first comparison value.
     * @param otherSecond The second comparison value.
     * @return {@code true} if the condition holds, {@code false} otherwise.
     */
    @Override
    public boolean test(final int value,
                        final int otherFirst,
                        final int otherSecond) {
        return predicate.test(value, otherFirst, otherSecond);
    }

    /**
     * Returns the symbolic representation of this condition.
     *
     * @return The string representation of the condition.
     */
    @Override
    public String toString() {
        return operation;
    }
}
