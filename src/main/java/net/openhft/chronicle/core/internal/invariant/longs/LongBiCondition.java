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

import net.openhft.chronicle.core.util.LongBiPredicate;

import static java.util.Objects.requireNonNull;

public enum LongBiCondition implements LongBiPredicate {

    EQUAL_TO("==", (value, other) -> value == other),
    NOT_EQUAL_TO("!=", (value, other) -> value != other),
    GREATER_THAN(">", (value, other) -> value > other),
    GREATER_OR_EQUAL(">=", (value, other) -> value >= other),
    LESS_THAN("<", (value, other) -> value < other),
    LESS_OR_EQUAL("<=", (value, other) -> value <= other),
    BETWEEN_ZERO_AND("∈ [0, toExclusive), toExclusive =", (value, other) -> value >= 0 && value < other),
    BETWEEN_ZERO_AND_CLOSED("∈ [0, toInclusive], where toInclusive =", (value, other) -> value >= 0 && value < other),
    POWER_OF_TWO(" = 2^", (value, other) -> other < (Integer.SIZE - 1) && value == (1L << other)),
    LOG2(" = log2(other), other = ", (value, other) -> value < (Integer.SIZE - 1) && other == (1L << value));

    private final String operation;
    private final LongBiPredicate predicate;

    LongBiCondition(final String operation,
                    final LongBiPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public boolean test(final long value,
                        final long other) {
        return predicate.test(value, other);
    }

    @Override
    public LongBiPredicate negate() {
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
                return LongBiPredicate.super.negate();
        }
    }

    @Override
    public String toString() {
        return operation;
    }
}