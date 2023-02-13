package net.openhft.chronicle.core.observable;

public interface Observable {

    void dumpState(StateReporter stateReporter);

    default String idString() {
        return getClass().getSimpleName() + "@" + System.identityHashCode(this);
    }
}
