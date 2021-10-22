package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;

enum NopThreadConfinementAsserter implements ThreadConfinementAsserter {
    INSTANCE;

    @Override
    public void assertThreadConfined() {
        // Do nothing
    }
}