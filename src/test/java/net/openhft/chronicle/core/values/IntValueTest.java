package net.openhft.chronicle.core.values;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IntValueTest {

    private IntValue intValue;

    @BeforeEach
    void setUp() {
        intValue = mock(IntValue.class); // Create a mock object of IntValue
    }

    @Test
    void testGetValue() {
        when(intValue.getValue()).thenReturn(10); // Setup the mock to return 10
        assertEquals(10, intValue.getValue());
    }

    @Test
    void testSetValue() {
        doNothing().when(intValue).setValue(anyInt());
        intValue.setValue(20);
        verify(intValue, times(1)).setValue(20);
    }

    @Test
    void testCloseAndIsClosed() {
        when(intValue.isClosed()).thenReturn(false, true); // Before and after close
        assertFalse(intValue.isClosed());
        intValue.close();
        assertTrue(intValue.isClosed());
    }
}
