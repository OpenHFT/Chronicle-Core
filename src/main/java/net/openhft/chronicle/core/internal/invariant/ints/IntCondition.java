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

import java.util.function.IntPredicate;

import static java.util.Objects.requireNonNull;

/**
 * Enum representing various conditions or predicates on integers, implementing {@link IntPredicate}.
 * <p>
 * This enum provides a set of predefined conditions to test against integer values (e.g., positivity, negativity,
 * zero, alignment, etc.). Each condition is defined by a symbolic operation and an associated {@link IntPredicate}
 * that performs the test.
 * </p>
 * <p>
 * It also supports negating certain conditions, returning the logical opposite of the current condition.
 * </p>
 */
public enum IntCondition implements IntPredicate {

    POSITIVE("> 0", value -> value > 0),
    NEGATIVE("< 0", value -> value < 0),
    ZERO("== 0", value -> value == 0),
    NON_POSITIVE("<= 0", value -> value <= 0),
    NON_NEGATIVE(">= 0", value -> value >= 0),
    NON_ZERO("!= 0", value -> value != 0),
    BYTE_CONVERTIBLE(Byte.MIN_VALUE, Byte.MAX_VALUE),
    SHORT_CONVERTIBLE(Short.MIN_VALUE, Short.MAX_VALUE),
    EVEN_POWER_OF_TWO(" > 0 && bitcount == 1", value -> value > 0 && Integer.bitCount(value) == 1),
    // EVEN, ODD
    // PRIME
    SHORT_ALIGNED("short aligned", value -> (value & (Short.BYTES - 1)) == 0),
    INT_ALIGNED("int aligned", value -> (value & (Integer.BYTES - 1)) == 0),
    LONG_ALIGNED("long aligned", value -> (value & (Long.BYTES - 1)) == 0);

    // Symbolic representation of the condition
    private final String operation;

    // Predicate that defines the condition
    private final IntPredicate predicate;

    /**
     * Constructor for creating an {@link IntCondition} based on an operation string and predicate.
     *
     * @param operation The symbolic representation of the condition.
     * @param predicate The predicate defining the condition.
     */
    IntCondition(final String operation,
                 final IntPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    /**
     * Constructor for creating an {@link IntCondition} that checks if a value is within a given range.
     *
     * @param fromInclusive The inclusive lower bound of the range.
     * @param toInclusive   The inclusive upper bound of the range.
     */
    IntCondition(final int fromInclusive,
                 final int toInclusive) {
        this.operation = "∈ [" + fromInclusive + ", " + toInclusive + "]";
        this.predicate = value -> value >= fromInclusive && value <= toInclusive;
    }

    /**
     * Tests the condition on the provided integer value.
     *
     * @param value The integer value to test.
     * @return {@code true} if the value satisfies the condition, {@code false} otherwise.
     */
    @Override
    public boolean test(final int value) {
        return predicate.test(value);
    }

    /**
     * Returns the negation of this condition.
     * <p>
     * For certain predefined conditions (e.g., POSITIVE, ZERO), this method returns the logical opposite.
     * For other conditions, it uses the default implementation of {@link IntPredicate#negate()}.
     * </p>
     *
     * @return The negated {@link IntPredicate} for this condition.
     */
    @Override
    public IntPredicate negate() {
        switch (this) {
            case POSITIVE:
                return NON_POSITIVE;
            case NEGATIVE:
                return NON_NEGATIVE;
            case ZERO:
                return NON_ZERO;
            case NON_POSITIVE:
                return POSITIVE;
            case NON_NEGATIVE:
                return NEGATIVE;
            case NON_ZERO:
                return ZERO;
            default:
                return IntPredicate.super.negate();
        }
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