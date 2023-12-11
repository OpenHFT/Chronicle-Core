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

public enum LongCondition implements LongPredicate {

    POSITIVE("> 0", value -> value > 0),
    NEGATIVE("< 0", value -> value < 0),
    ZERO("== 0", value -> value == 0),
    NON_POSITIVE("<= 0", value -> value <= 0),
    NON_NEGATIVE(">= 0", value -> value >= 0),
    NON_ZERO("!= 0", value -> value != 0),
    BYTE_CONVERTIBLE(Byte.MIN_VALUE, Byte.MAX_VALUE),
    SHORT_CONVERTIBLE(Short.MIN_VALUE, Short.MAX_VALUE),
    EVEN_POWER_OF_TWO(" > 0 && bitcount == 1", value -> value > 0 && Long.bitCount(value) == 1),
    // EVEN, ODD
    // PRIME
    SHORT_ALIGNED("short aligned", value -> (value & (Short.BYTES - 1)) == 0),
    INT_ALIGNED("int aligned", value -> (value & (Integer.BYTES - 1)) == 0),
    LONG_ALIGNED("long aligned", value -> (value & (Long.BYTES - 1)) == 0);

    private final String operation;
    private final LongPredicate predicate;

    LongCondition(final String operation,
                  final LongPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    LongCondition(final long fromInclusive,
                  final long toInclusive) {
        this.operation = "∈ [" + fromInclusive + ", " + toInclusive + "]";
        this.predicate = value -> value >= fromInclusive && value <= toInclusive;
    }

    @Override
    public boolean test(final long value) {
        return predicate.test(value);
    }

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

    @Override
    public String toString() {
        return operation;
    }
}
