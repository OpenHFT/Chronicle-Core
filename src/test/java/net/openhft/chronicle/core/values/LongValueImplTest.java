/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.values;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongValueImplTest {

    @Test
    void testSetValueAndGetValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getValue(), "getValue should return value set by setValue");
    }

    @Test
    void testAddValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(5L);
        longValue.addValue(3L);
        assertEquals(8L, longValue.getValue(), "getValue should return sum after addValue");
    }

    @Test
    void testCompareAndSwapValueSuccess() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(15L);
        assertTrue(longValue.compareAndSwapValue(15L, 20L), "CAS succeeds on match");
        assertEquals(20L, longValue.getValue(), "value updates after swap");
    }

    @Test
    void testCompareAndSwapValueFailure() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(15L);
        assertFalse(longValue.compareAndSwapValue(10L, 20L), "CAS fails on mismatch");
        assertEquals(15L, longValue.getValue(), "value stays after failed swap");
    }

    @Test
    void testSetMaxValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(50L);
        longValue.setMaxValue(100L);
        assertEquals(100L, longValue.getValue(), "getValue should return new value after setMaxValue with greater value");
        longValue.setMaxValue(50L); // Should not change the value
        assertEquals(100L, longValue.getValue(), "getValue should return unchanged value after setMaxValue with lesser value");
    }

    @Test
    void testSetMinValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(50L);
        longValue.setMinValue(25L);
        assertEquals(25L, longValue.getValue(), "getValue should return new value after setMinValue with lesser value");
        longValue.setMinValue(50L); // Should not change the value
        assertEquals(25L, longValue.getValue(), "getValue should return unchanged value after setMinValue with greater value");
    }

    @Test
    void testGetAndSetVolatileValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setVolatileValue(123L);
        assertEquals(123L, longValue.getVolatileValue(), "getVolatileValue should return value set by setVolatileValue");
    }

    @Test
    void testSetOrderedValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setOrderedValue(456L);
        assertEquals(456L, longValue.getValue(), "getValue should return value set by setOrderedValue");
    }

    @Test
    void testAddAtomicValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        longValue.addAtomicValue(5L);
        assertEquals(15L, longValue.getValue(), "getValue should return sum after addAtomicValue with positive value");
        longValue.addAtomicValue(-3L);
        assertEquals(12L, longValue.getValue(), "getValue should return sum after addAtomicValue with negative value");
    }

    @Test
    void testIsClosed() {
        LongValueImpl longValue = new LongValueImpl();
        assertFalse(longValue.isClosed(), "isClosed should return false before close");

        longValue.close();
        assertTrue(longValue.isClosed(), "isClosed should return true after close");
    }

    @Test
    void testGetVolatileValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getVolatileValue(), "getVolatileValue should return value set by setValue");
    }

    @Test
    void testGetVolatileValueWithClosedValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getVolatileValue(20L), "getVolatileValue should return current value when not closed");

        longValue.close();
        assertEquals(20L, longValue.getVolatileValue(20L), "getVolatileValue should return closed value when closed");
    }
}
