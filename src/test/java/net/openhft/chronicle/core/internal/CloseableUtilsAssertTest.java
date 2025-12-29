/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.AbstractCloseable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloseableUtilsAssertTest {

    static final class DummyCloseable extends AbstractCloseable {
        boolean performed;
        @Override
        protected void performClose() throws IllegalStateException {
            performed = true;
        }
    }

    @DisplayName("assertCloseablesClosedFindsUnclosedAndCloses behaviour under expected input and output conditions")
    @Test
    void assertCloseablesClosedFindsUnclosedAndCloses() {
        // Ensure tracing is enabled for this test
        AbstractCloseable.enableCloseableTracing();
        DummyCloseable dc = new DummyCloseable();
        // do not close it to simulate leak
        AssertionError ae = assertThrows(AssertionError.class, AbstractCloseable::assertCloseablesClosed,
                "assertCloseablesClosed should throw when leak is detected");
        assertTrue(ae.getSuppressed().length >= 1,
                "suppressed exceptions count should be >= 1 but was " + ae.getSuppressed().length);
        // The helper should have closed leaked resources
        assertTrue(dc.performed, "leaked resource should be closed by assertCloseablesClosed helper");
    }
}
