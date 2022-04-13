package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.util.ThreadConfinementLifecycle;

public interface ThreadConfinementAsserter {

    /**
     * Asserts that this thread is the only thread that has ever called this
     * method.
     *
     * @throws IllegalStateException if another thread called this method previously.
     */
    void assertThreadConfined();

    /**
     * Creates and returns a new ThreadConfinementAsserter if assertions are enabled, otherwise
     * returns a no-op asserter.
     *
     * @return Creates and returns a new ThreadConfinementAsserter if assertions are enabled, otherwise
     * returns a no-op asserter
     */
    static ThreadConfinementAsserter create() {
        return ThreadConfinementLifecycle.create();
    }

    /**
     * Creates and returns a new ThreadConfinementAsserter that is always enabled.
     *
     * @return Creates and returns a new enabled ThreadConfinementAsserter
     */
    static ThreadConfinementAsserter createEnabled() {
        return ThreadConfinementLifecycle.createEnabled();
    }

}
