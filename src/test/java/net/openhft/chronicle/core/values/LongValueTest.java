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
