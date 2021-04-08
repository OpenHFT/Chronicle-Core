package net.openhft.chronicle.core.resource;

import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

public interface ManagedResourceSupport {

    void close();

    // idempotency, returns true iff open
    boolean closeIfOpen();

    int referenceId();

    StackTrace createdHere();

    void assertOpen();

    void assertThreadConfined();

    @NotNull
    State state();

    enum State { OPEN, CLOSING, CLOSED}

    default boolean is(State state) {
        assertThreadConfined();
        return state() == state;
    }


    @NotNull
    static ManagedResourceSupportBuilder builder() {
        return null;
    }

}