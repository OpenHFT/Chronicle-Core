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
        assertEquals(10L, longValue.getValue(), "testSetValueAndGetValue: L16");
    }

    @Test
    void testAddValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(5L);
        longValue.addValue(3L);
        assertEquals(8L, longValue.getValue(), "testAddValue: L24");
    }

    @Test
    void testCompareAndSwapValueSuccess() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(15L);
        assertTrue(longValue.compareAndSwapValue(15L, 20L), "testCompareAndSwapValueSuccess: L31");
        assertEquals(20L, longValue.getValue(), "testCompareAndSwapValueSuccess: L32");
    }

    @Test
    void testCompareAndSwapValueFailure() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(15L);
        assertFalse(longValue.compareAndSwapValue(10L, 20L), "testCompareAndSwapValueFailure: L39");
        assertEquals(15L, longValue.getValue(), "testCompareAndSwapValueFailure: L40");
    }

    @Test
    void testSetMaxValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(50L);
        longValue.setMaxValue(100L);
        assertEquals(100L, longValue.getValue(), "testSetMaxValue: L48");
        longValue.setMaxValue(50L); // Should not change the value
        assertEquals(100L, longValue.getValue(), "testSetMaxValue: L50");
    }

    @Test
    void testSetMinValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(50L);
        longValue.setMinValue(25L);
        assertEquals(25L, longValue.getValue(), "testSetMinValue: L58");
        longValue.setMinValue(50L); // Should not change the value
        assertEquals(25L, longValue.getValue(), "testSetMinValue: L60");
    }

    @Test
    void testGetAndSetVolatileValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setVolatileValue(123L);
        assertEquals(123L, longValue.getVolatileValue(), "testGetAndSetVolatileValue: L67");
    }

    @Test
    void testSetOrderedValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setOrderedValue(456L);
        assertEquals(456L, longValue.getValue(), "testSetOrderedValue: L74");
    }

    @Test
    void testAddAtomicValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        longValue.addAtomicValue(5L);
        assertEquals(15L, longValue.getValue(), "testAddAtomicValue: L82");
        longValue.addAtomicValue(-3L);
        assertEquals(12L, longValue.getValue(), "testAddAtomicValue: L84");
    }

    @Test
    void testIsClosed() {
        LongValueImpl longValue = new LongValueImpl();
        assertFalse(longValue.isClosed(), "testIsClosed: L90");

        longValue.close();
        assertTrue(longValue.isClosed(), "testIsClosed: L93");
    }

    @Test
    void testGetVolatileValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getVolatileValue(), "testGetVolatileValue: L100");
    }

    @Test
    void testGetVolatileValueWithClosedValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getVolatileValue(20L), "testGetVolatileValueWithClosedValue: L107");

        longValue.close();
        assertEquals(20L, longValue.getVolatileValue(20L), "testGetVolatileValueWithClosedValue: L110");
    }
}
