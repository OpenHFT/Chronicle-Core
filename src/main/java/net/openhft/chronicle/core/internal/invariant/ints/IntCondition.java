package net.openhft.chronicle.core.internal.invariant.ints;

import java.util.function.IntPredicate;

import static java.util.Objects.requireNonNull;

public enum IntCondition implements IntPredicate {

    POSITIVE("> 0", value -> value > 0),
    NEGATIVE("< 0", value -> value < 0),
    ZERO("== 0", value -> value == 0),
    NON_POSITIVE("<= 0", value -> value <= 0),
    NON_NEGATIVE(">= 0", value -> value >= 0),
    NON_ZERO("!= 0", value -> value != 0),
    BYTE_CONVERTIBLE(Byte.MIN_VALUE, Byte.MAX_VALUE),
    SHORT_CONVERTIBLE(Short.MIN_VALUE, Short.MAX_VALUE),
    EVEN_POWER_OF_TWO(" > 0 && bitcount == 1", value -> value > 0 && Integer.bitCount(value) == 1);
    // EVEN, ODD
    // PRIME
    // SHORT_ALIGNED, INT_ALIGNED, LONG_ALIGNED

    private final String operation;
    private final IntPredicate predicate;

    IntCondition(final String operation,
                 final IntPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    IntCondition(final int fromInclusive,
                 final int toInclusive) {
        this.operation = "∈ [" + fromInclusive + ", " + toInclusive + "]";
        this.predicate = value -> value >= fromInclusive && value <= toInclusive;
    }

    @Override
    public boolean test(final int value) {
        return predicate.test(value);
    }

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

    @Override
    public String toString() {
        return operation;
    }
}