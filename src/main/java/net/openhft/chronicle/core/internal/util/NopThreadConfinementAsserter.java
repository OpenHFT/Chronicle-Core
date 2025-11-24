/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;

/**
 * {@link ThreadConfinementAsserter} implementation that performs no checks.
 * <p>
 * Used when thread confinement assertions are disabled for performance reasons.
 */
enum NopThreadConfinementAsserter implements ThreadConfinementAsserter {
    INSTANCE;

    @Override
    public void assertThreadConfined() {
        // Do nothing
    }
}
