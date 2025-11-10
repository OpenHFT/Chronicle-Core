//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThreadLocalisedExceptionHandlerTest {

    private ExceptionHandler defaultHandler;
    private ThreadLocalisedExceptionHandler tlExceptionHandler;

    @BeforeEach
    void setUp() {
        defaultHandler = mock(ExceptionHandler.class);
        tlExceptionHandler = new ThreadLocalisedExceptionHandler(defaultHandler);
    }

    @Test
    void testUnwrapExceptionHandler() {
        assertSame(defaultHandler, ThreadLocalisedExceptionHandler.unwrap(tlExceptionHandler));
    }

    @Test
    void testIsEnabled() {
        when(defaultHandler.isEnabled(Exception.class)).thenReturn(true);
        assertTrue(tlExceptionHandler.isEnabled(Exception.class));
    }

    // Add more tests as necessary for other methods and edge cases.
}
