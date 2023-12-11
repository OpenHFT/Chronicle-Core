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

import net.openhft.chronicle.core.util.LongTriPredicate;

import static java.util.Objects.requireNonNull;

public enum LongTriCondition implements LongTriPredicate {

    BETWEEN("∈ [fromInclusive, toExclusive), where (fromInclusive, toExclusive) = ", (value, otherFirst, otherSecond) -> value >= otherFirst && value < otherSecond),
    BETWEEN_CLOSED("∈ [fromInclusive, toInclusive], where (fromInclusive, toInclusive) = ", (value, otherFirst, otherSecond) -> value >= otherFirst && value <= otherSecond),
    BETWEEN_ZERO_AND_ENSURING("∈ [0, index - size ], where (index, size) = ", (value, otherFirst, otherSecond) -> value >= 0 && value <= (otherFirst - otherSecond));

    private final String operation;
    private final LongTriPredicate predicate;

    LongTriCondition(final String operation,
                     final LongTriPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public boolean test(final long value,
                        final long otherFirst,
                        final long otherSecond) {
        return predicate.test(value, otherFirst, otherSecond);
    }

    @Override
    public String toString() {
        return operation;
    }
}
