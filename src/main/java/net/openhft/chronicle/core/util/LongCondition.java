package net.openhft.chronicle.core.util;

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
    INT_CONVERTIBLE(Integer.MIN_VALUE, Integer.MAX_VALUE),
    EVEN_POWER_OF_TWO(" bitcount == 1", value -> value > 0 && Long.bitCount(value) == 1);
    // PRIME

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
        }
        return LongPredicate.super.negate();
    }

    @Override
    public String toString() {
        return operation;
    }
}