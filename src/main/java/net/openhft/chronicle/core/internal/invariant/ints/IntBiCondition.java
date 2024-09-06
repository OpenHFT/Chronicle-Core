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

import net.openhft.chronicle.core.util.IntBiPredicate;

import static java.util.Objects.requireNonNull;

/**
 * Enum representing various integer comparison conditions, implementing {@link IntBiPredicate}.
 * <p>
 * This enum provides a set of predefined comparison operations between two integer values (e.g., equality,
 * greater than, less than, etc.). Each condition is represented by a symbolic operation and an associated
 * {@link IntBiPredicate} that performs the comparison.
 * </p>
 * <p>
 * It also supports negating certain conditions, returning the logical opposite of the current condition.
 * </p>
 */
public enum IntBiCondition implements IntBiPredicate {

    EQUAL_TO("==", (value, other) -> value == other),
    NOT_EQUAL_TO("!=", (value, other) -> value != other),
    GREATER_THAN(">", (value, other) -> value > other),
    GREATER_OR_EQUAL(">=", (value, other) -> value >= other),
    LESS_THAN("<", (value, other) -> value < other),
    LESS_OR_EQUAL("<=", (value, other) -> value <= other),
    BETWEEN_ZERO_AND("∈ [0, toExclusive), toExclusive =", (value, other) -> value >= 0 && value < other),
    BETWEEN_ZERO_AND_CLOSED("∈ [0, toInclusive], where toInclusive =", (value, other) -> value >= 0 && value < other),
    POWER_OF_TWO(" = 2^", (value, other) -> other < (Integer.SIZE - 1) && value == (1 << other)),
    LOG2(" = log2(other), other = ", (value, other) -> value < (Integer.SIZE - 1) && other == (1 << value));

    // The symbolic representation of the operation (e.g., "==", "!=")
    private final String operation;

    // The predicate that performs the comparison operation
    private final IntBiPredicate predicate;

    /**
     * Constructor for the {@link IntBiCondition} enum.
     *
     * @param operation The symbolic representation of the comparison operation.
     * @param predicate The predicate that performs the comparison.
     */
    IntBiCondition(final String operation,
                   final IntBiPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    /**
     * Tests the comparison condition between two integer values.
     *
     * @param value The first integer value.
     * @param other The second integer value.
     * @return {@code true} if the condition holds for the two values, {@code false} otherwise.
     */
    @Override
    public boolean test(final int value,
                        final int other) {
        return predicate.test(value, other);
    }

    /**
     * Returns the negation of this condition.
     * <p>
     * For certain predefined conditions (e.g., EQUAL_TO, LESS_THAN), this method returns the logical negation.
     * For other conditions, it uses the default implementation of {@link IntBiPredicate#negate()}.
     * </p>
     *
     * @return The negated {@link IntBiPredicate} for this condition.
     */
    @Override
    public IntBiPredicate negate() {
        switch (this) {
            case EQUAL_TO:
                return NOT_EQUAL_TO;
            case NOT_EQUAL_TO:
                return EQUAL_TO;
            case GREATER_THAN:
                return LESS_OR_EQUAL;
            case GREATER_OR_EQUAL:
                return LESS_THAN;
            case LESS_THAN:
                return GREATER_OR_EQUAL;
            case LESS_OR_EQUAL:
                return GREATER_THAN;
            default:
                return IntBiPredicate.super.negate();
        }
    }

    /**
     * Returns the symbolic representation of this condition.
     *
     * @return The string representation of the comparison operation (e.g., "==", ">=").
     */
    @Override
    public String toString() {
        return operation;
    }
}
