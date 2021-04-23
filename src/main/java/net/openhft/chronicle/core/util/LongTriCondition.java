package net.openhft.chronicle.core.util;

import static java.util.Objects.requireNonNull;

public enum LongTriCondition implements LongTriPredicate {

    BETWEEN("∈ [fromInclusive, toExclusive), where (fromInclusive, toExclusive) = ", (value, otherFirst, otherSecond) -> value > otherFirst && value <= otherSecond),
    BETWEEN_CLOSED("∈ [fromInclusive, toInclusive], where (fromInclusive, toInclusive) = ", (value, otherFirst, otherSecond) -> value >= otherFirst && value <= otherSecond);

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