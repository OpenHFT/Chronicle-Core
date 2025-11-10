//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;

enum NopThreadConfinementAsserter implements ThreadConfinementAsserter {
    INSTANCE;

    @Override
    public void assertThreadConfined() {
        // Do nothing
    }
}
