//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.internal.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NopThreadConfinementAsserterTest {

    @Test
    void assertThreadConfinedShouldDoNothing() {
        NopThreadConfinementAsserter asserter = NopThreadConfinementAsserter.INSTANCE;

        assertDoesNotThrow(asserter::assertThreadConfined, "assertThreadConfined should not throw any exceptions");
    }
}
