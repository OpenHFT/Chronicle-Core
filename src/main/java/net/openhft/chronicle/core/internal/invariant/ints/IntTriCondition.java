package net.openhft.chronicle.core.internal.invariant.ints;

import net.openhft.chronicle.core.util.IntTriPredicate;

import static java.util.Objects.requireNonNull;

public enum IntTriCondition implements IntTriPredicate {

    BETWEEN("∈ [fromInclusive, toExclusive), where (fromInclusive, toExclusive) = ", (value, otherFirst, otherSecond) -> value >= otherFirst && value < otherSecond),
    BETWEEN_CLOSED("∈ [fromInclusive, toInclusive], where (fromInclusive, toInclusive) = ", (value, otherFirst, otherSecond) -> value >= otherFirst && value <= otherSecond),
    BETWEEN_ZERO_AND_ENSURING("∈ [0, index - size ], where (index, size) = ", (value, otherFirst, otherSecond) -> value >= 0 && value <= (otherFirst - otherSecond));

    private final String operation;
    private final IntTriPredicate predicate;

    IntTriCondition(final String operation,
                    final IntTriPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public boolean test(final int value,
                        final int otherFirst,
                        final int otherSecond) {
        return predicate.test(value, otherFirst, otherSecond);
    }

    @Override
    public String toString() {
        return operation;
    }
}