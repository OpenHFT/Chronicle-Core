package net.openhft.chronicle.core.values;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongValueImplTest {

    @Test
    void testSetValueAndGetValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getValue());
    }

    @Test
    void testAddValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(5L);
        longValue.addValue(3L);
        assertEquals(8L, longValue.getValue());
    }

    @Test
    void testCompareAndSwapValueSuccess() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(15L);
        assertTrue(longValue.compareAndSwapValue(15L, 20L));
        assertEquals(20L, longValue.getValue());
    }

    @Test
    void testCompareAndSwapValueFailure() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(15L);
        assertFalse(longValue.compareAndSwapValue(10L, 20L));
        assertEquals(15L, longValue.getValue());
    }

    @Test
    void testSetMaxValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(50L);
        longValue.setMaxValue(100L);
        assertEquals(100L, longValue.getValue());
        longValue.setMaxValue(50L); // Should not change the value
        assertEquals(100L, longValue.getValue());
    }

    @Test
    void testSetMinValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(50L);
        longValue.setMinValue(25L);
        assertEquals(25L, longValue.getValue());
        longValue.setMinValue(50L); // Should not change the value
        assertEquals(25L, longValue.getValue());
    }

    @Test
    void testGetAndSetVolatileValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setVolatileValue(123L);
        assertEquals(123L, longValue.getVolatileValue());
    }

    @Test
    void testSetOrderedValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setOrderedValue(456L);
        assertEquals(456L, longValue.getValue());
    }

    @Test
    void testAddAtomicValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        longValue.addAtomicValue(5L);
        assertEquals(15L, longValue.getValue());
        longValue.addAtomicValue(-3L);
        assertEquals(12L, longValue.getValue());
    }

    @Test
    void testIsClosed() {
        LongValueImpl longValue = new LongValueImpl();
        assertFalse(longValue.isClosed());

        longValue.close();
        assertTrue(longValue.isClosed());
    }

    @Test
    void testGetVolatileValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getVolatileValue());
    }

    @Test
    void testGetVolatileValueWithClosedValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getVolatileValue(20L));

        longValue.close();
        assertEquals(20L, longValue.getVolatileValue(20L));
    }
}
