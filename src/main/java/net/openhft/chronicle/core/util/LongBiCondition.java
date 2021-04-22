package net.openhft.chronicle.core.util;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

public enum LongBiCondition implements LongBiPredicate {

    EQUALS("==", (value, other) -> value == other),
    GREATER_THAN(">", (value, other) -> value > other),
    GREATER_OR_EQUAL(">=", (value, other) -> value >= other),
    LESS_THAN("<", (value, other) -> value < other),
    LESS_OR_EQUAL("<=", (value, other) -> value <= other),
    IN_RANGE_ZERO("∈ [0, toExclusive), toExclusive =", (value, other) -> value >=0 && value < other),
    IN_RANGE_ZERO_CLOSED("∈ [0, toInclusive], where toInclusive =", (value, other) -> value >=0 && value < other),
    POWER_OF_TWO(" = 2^", (value, other) -> other < (Long.SIZE - 1) && value == (1L << other));

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
    public String toString() {
        return operation;
    }
}