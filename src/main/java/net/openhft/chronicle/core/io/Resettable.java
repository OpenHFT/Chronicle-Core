package net.openhft.chronicle.core.io;

/**
 * A DTO or component which can be reset to it's initial state.
 */
public interface Resettable {
    default void reset() {
    }
}
