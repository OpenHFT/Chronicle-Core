/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleCloseableTest {

    static class SimpleCloseableStub extends SimpleCloseable {
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
        SimpleCloseableStub closeable = new SimpleCloseableStub();

        assertFalse(closeable.isClosed(), "closeable should not be closed initially");
        closeable.close();
        assertTrue(closeable.isClosed(), "closeable should be closed after calling close");
        assertTrue(closeable.isPerformCloseCalled(), "performClose should have been called");

        closeable.close();
        assertTrue(closeable.isClosed(), "closeable should remain closed after calling close again");
    }

    @Test
    void testIsClosed() {
        SimpleCloseableStub closeable = new SimpleCloseableStub();

        assertFalse(closeable.isClosed(), "isClosed should return false before close is called");
        closeable.close();
        assertTrue(closeable.isClosed(), "isClosed should return true after close is called");
    }
}
