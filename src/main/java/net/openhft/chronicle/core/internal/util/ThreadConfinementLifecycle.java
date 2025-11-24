/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Factory and lifecycle utilities for {@link ThreadConfinementAsserter}.
 * <p>
 * Detects whether assertions are enabled and returns either a real asserter or a no-op
 * implementation accordingly.
 */
public final class ThreadConfinementLifecycle {

    private static final boolean ASSERTIONS_ENABLE = assertionsEnable();

    private ThreadConfinementLifecycle() {}

    public static ThreadConfinementAsserter create() {
        return create(ASSERTIONS_ENABLE);
    }

    public static ThreadConfinementAsserter createEnabled() {
        return create(true);
    }

    static ThreadConfinementAsserter create(boolean active) {
        return active
                ? new VanillaThreadConfinementAsserter()
                : NopThreadConfinementAsserter.INSTANCE;
    }

    static boolean assertionsEnable() {
        final AtomicBoolean ae = new AtomicBoolean();
        assert testAssert(ae);
        return ae.get();
    }

    private static boolean testAssert(final AtomicBoolean ae) {
        ae.set(true);
        return true;
    }
}
