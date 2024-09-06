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

package net.openhft.chronicle.core.internal.invariant.longs;

import java.util.function.LongPredicate;

import static java.util.Objects.requireNonNull;

/**
 * Enum representing various conditions or predicates on long values, implementing {@link LongPredicate}.
 * <p>
 * This enum provides a set of predefined conditions for testing long values, such as positivity, negativity,
 * zero, alignment, and more. Each condition is defined by a symbolic operation and an associated {@link LongPredicate}
 * that performs the test.
 * </p>
 * <p>
 * It also supports negating certain conditions, returning the logical opposite of the current condition.
 * </p>
 */
public enum LongCondition implements LongPredicate {

    POSITIVE("> 0", value -> value > 0),
    NEGATIVE("< 0", value -> value < 0),
    ZERO("== 0", value -> value == 0),
    NON_POSITIVE("<= 0", value -> value <= 0),
    NON_NEGATIVE(">= 0", value -> value >= 0),
    NON_ZERO("!= 0", value -> value != 0),
    BYTE_CONVERTIBLE(Byte.MIN_VALUE, Byte.MAX_VALUE),
    SHORT_CONVERTIBLE(Short.MIN_VALUE, Short.MAX_VALUE),

    /**
     * Condition representing that the value is a positive power of two.
     */
    EVEN_POWER_OF_TWO(" > 0 && bitcount == 1", value -> value > 0 && Long.bitCount(value) == 1),

    /**
     * Condition representing that the value is short-aligned (i.e., divisible by {@link Short#BYTES}).
     */
    SHORT_ALIGNED("short aligned", value -> (value & (Short.BYTES - 1)) == 0),
    INT_ALIGNED("int aligned", value -> (value & (Integer.BYTES - 1)) == 0),
    LONG_ALIGNED("long aligned", value -> (value & (Long.BYTES - 1)) == 0);

    // Symbolic representation of the condition
    private final String operation;

    // Predicate that defines the condition
    private final LongPredicate predicate;

    /**
     * Constructor for creating a {@link LongCondition} based on an operation string and predicate.
     *
     * @param operation The symbolic representation of the condition.
     * @param predicate The predicate defining the condition.
     */
    LongCondition(final String operation,
                  final LongPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    /**
     * Constructor for creating a {@link LongCondition} that checks if a value is within a given range.
     *
     * @param fromInclusive The inclusive lower bound of the range.
     * @param toInclusive   The inclusive upper bound of the range.
     */
    LongCondition(final long fromInclusive,
                  final long toInclusive) {
        this.operation = "∈ [" + fromInclusive + ", " + toInclusive + "]";
        this.predicate = value -> value >= fromInclusive && value <= toInclusive;
    }

    /**
     * Tests the condition on the provided long value.
     *
     * @param value The long value to test.
     * @return {@code true} if the value satisfies the condition, {@code false} otherwise.
     */
    @Override
    public boolean test(final long value) {
        return predicate.test(value);
    }

    /**
     * Returns the negation of this condition.
     * <p>
     * For certain predefined conditions (e.g., POSITIVE, ZERO), this method returns the logical opposite.
     * For other conditions, it uses the default implementation of {@link LongPredicate#negate()}.
     * </p>
     *
     * @return The negated {@link LongPredicate} for this condition.
     */
    @Override
    public LongPredicate negate() {
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
                return LongPredicate.super.negate();
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
