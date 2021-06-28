package net.openhft.chronicle.core.internal.invariant.ints;

import net.openhft.chronicle.core.util.IntBiPredicate;

import static java.util.Objects.requireNonNull;

public enum IntBiCondition implements IntBiPredicate {

    EQUAL_TO("==", (value, other) -> value == other),
    NOT_EQUAL_TO("!=", (value, other) -> value != other),
    GREATER_THAN(">", (value, other) -> value > other),
    GREATER_OR_EQUAL(">=", (value, other) -> value >= other),
    LESS_THAN("<", (value, other) -> value < other),
    LESS_OR_EQUAL("<=", (value, other) -> value <= other),
    BETWEEN_ZERO_AND("∈ [0, toExclusive), toExclusive =", (value, other) -> value >= 0 && value < other),
    BETWEEN_ZERO_AND_CLOSED("∈ [0, toInclusive], where toInclusive =", (value, other) -> value >= 0 && value < other),
    POWER_OF_TWO(" = 2^", (value, other) -> other < (Integer.SIZE - 1) && value == (1 << other)),
    LOG2(" = log2(other), other = ", (value, other) -> value < (Integer.SIZE - 1) && other == (1 << value));

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
    public IntBiPredicate negate() {
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
                return IntBiPredicate.super.negate();
        }
    }

    @Override
    public String toString() {
        return operation;
    }
}