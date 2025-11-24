/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.util.ThreadConfinementLifecycle;

/**
 * Asserts that certain operations are confined to a single thread, throwing if
 * invoked from multiple threads. Factory methods return either enabled or
 * no-op instances depending on assertion settings.
 */
public interface ThreadConfinementAsserter {

    /**
     * Asserts that this thread is the only thread that has ever called this
     * method.
     *
     * @throws IllegalStateException If another thread called this method previously.
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
