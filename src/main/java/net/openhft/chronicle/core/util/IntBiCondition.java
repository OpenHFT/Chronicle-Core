package net.openhft.chronicle.core.util;

import static java.util.Objects.requireNonNull;

public enum IntBiCondition implements IntBiPredicate {

    EQUALS("==", (value, other) -> value == other),
    GREATER_THAN(">", (value, other) -> value > other),
    GREATER_OR_EQUAL(">=", (value, other) -> value >= other),
    LESS_THAN("<", (value, other) -> value < other),
    LESS_OR_EQUAL("<=", (value, other) -> value <= other),
    IN_RANGE_ZERO("∈ [0, toExclusive), toExclusive =", (value, other) -> value >=0 && value < other),
    IN_RANGE_ZERO_CLOSED("∈ [0, toInclusive], where toInclusive =", (value, other) -> value >=0 && value < other),
    POWER_OF_TWO(" = 2^", (value, other) -> other < (Integer.SIZE - 1) && value == (1 << other));

    private final String operation;
    private final IntBiPredicate predicate;

    IntBiCondition(final String operation,
                   final IntBiPredicate predicate) {
        this.operation = requireNonNull(operation);
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public boolean test(final int value,
                        final int other) {
        return predicate.test(value, other);
    }

    @Override
    public String toString() {
        return operation;
    }
}