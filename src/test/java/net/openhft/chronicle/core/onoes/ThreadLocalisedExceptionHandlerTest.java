/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.test.RecordingExceptionHandlerStub;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class ThreadLocalisedExceptionHandlerTest {

    private RecordingExceptionHandlerStub defaultHandler;
    private ThreadLocalisedExceptionHandler tlExceptionHandler;

    @BeforeEach
    void setUp() {
        defaultHandler = new RecordingExceptionHandlerStub();
        tlExceptionHandler = new ThreadLocalisedExceptionHandler(defaultHandler);
    }

    @Test
    void testUnwrapExceptionHandler() {
        assertSame(defaultHandler, ThreadLocalisedExceptionHandler.unwrap(tlExceptionHandler));
    }

    @Test
    void testIsEnabled() {
        defaultHandler.enabled(true);
        assertTrue(tlExceptionHandler.isEnabled(Exception.class));
    }

    // Add more tests as necessary for other methods and edge cases.
}
