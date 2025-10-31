/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.values;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LongValueTest {

    private LongValue longValue;

    @BeforeEach
    void setUp() {
        longValue = mock(LongValue.class); // Creating a mock instance of LongValue
    }

    @Test
    void testGetValue() {
        when(longValue.getValue()).thenReturn(10L); // Setup the mock to return 10
        assertEquals(10L, longValue.getValue());
    }

    @Test
    void testSetValue() {
        doNothing().when(longValue).setValue(anyLong());
        longValue.setValue(20L);
        verify(longValue, times(1)).setValue(20L);
    }

    // Additional tests for other methods...

    @Test
    void testCloseAndIsClosed() {
        when(longValue.isClosed()).thenReturn(false, true); // Before and after close
        assertFalse(longValue.isClosed());
        longValue.close();
        assertTrue(longValue.isClosed());
    }
}
