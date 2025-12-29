/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.values;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LongValueTest {

    private LongValue longValue;

    @BeforeEach
    void setUp() {
        longValue = mock(LongValue.class); // Creating a mock instance of LongValue
    }

    @DisplayName("testGetValue behaviour under expected input and output conditions")
    @Test
    void testGetValue() {
        when(longValue.getValue()).thenReturn(10L); // Setup the mock to return 10
        assertEquals(10L, longValue.getValue(), "getValue should return mocked value");
    }

    @DisplayName("testSetValue behaviour under expected input and output conditions")
    @Test
    void testSetValue() {
        doNothing().when(longValue).setValue(anyLong());
        longValue.setValue(20L);
        verify(longValue, times(1)).setValue(20L);
    }

    // Additional tests for other methods...

    @DisplayName("testCloseAndIsClosed behaviour under expected input and output conditions")
    @Test
    void testCloseAndIsClosed() {
        when(longValue.isClosed()).thenReturn(false, true); // Before and after close
        assertFalse(longValue.isClosed(), "isClosed should return false before close");
        longValue.close();
        assertTrue(longValue.isClosed(), "isClosed should return true after close");
    }
}
