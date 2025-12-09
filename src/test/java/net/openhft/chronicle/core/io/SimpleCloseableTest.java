/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleCloseableTest {

    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    static class TestableSimpleCloseable extends SimpleCloseable {
        private boolean performCloseCalled = false;

        @Override
        protected void performClose() {
            if (!performCloseCalled) {
                super.performClose();
                performCloseCalled = true;
            }
        }

        boolean isPerformCloseCalled() {
            return performCloseCalled;
        }
    }

    @Test
    void testClose() {
        TestableSimpleCloseable closeable = new TestableSimpleCloseable();

        assertFalse(closeable.isClosed());
        closeable.close();
        assertTrue(closeable.isClosed());
        assertTrue(closeable.isPerformCloseCalled());

        closeable.close();
        assertTrue(closeable.isClosed());
    }

    @Test
    void testIsClosed() {
        TestableSimpleCloseable closeable = new TestableSimpleCloseable();

        assertFalse(closeable.isClosed());
        closeable.close();
        assertTrue(closeable.isClosed());
    }
}
