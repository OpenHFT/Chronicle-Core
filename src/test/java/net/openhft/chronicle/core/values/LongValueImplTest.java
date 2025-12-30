/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.values;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongValueImplTest {

    @DisplayName("Long wrapper reads stored long field")
    @Test
    void testSetValueAndGetValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getValue(), "getValue should return value set by setValue");
    }

    @DisplayName("Long wrapper adds delta to stored number")
    @Test
    void testAddValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(5L);
        longValue.addValue(3L);
        assertEquals(8L, longValue.getValue(), "getValue should return sum after addValue");
    }

    @DisplayName("Compare swap updates number on match success")
    @Test
    void testCompareAndSwapValueSuccess() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(15L);
        assertTrue(longValue.compareAndSwapValue(15L, 20L), "compareAndSwapValue should return true when expected value matches");
        assertEquals(20L, longValue.getValue(), "getValue should return new value after successful compareAndSwapValue");
    }

    @DisplayName("Compare swap keeps number when expectation fails")
    @Test
    void testCompareAndSwapValueFailure() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(15L);
        assertFalse(longValue.compareAndSwapValue(10L, 20L), "compareAndSwapValue should return false when expected value does not match");
        assertEquals(15L, longValue.getValue(), "getValue should return unchanged value after failed compareAndSwapValue");
    }

    @DisplayName("Max setter keeps greater number only")
    @Test
    void testSetMaxValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(50L);
        longValue.setMaxValue(100L);
        assertEquals(100L, longValue.getValue(), "getValue should return new value after setMaxValue with greater value");
        longValue.setMaxValue(50L); // Should not change the value
        assertEquals(100L, longValue.getValue(), "getValue should return unchanged value after setMaxValue with lesser value");
    }

    @DisplayName("Min setter keeps smaller number only")
    @Test
    void testSetMinValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(50L);
        longValue.setMinValue(25L);
        assertEquals(25L, longValue.getValue(), "getValue should return new value after setMinValue with lesser value");
        longValue.setMinValue(50L); // Should not change the value
        assertEquals(25L, longValue.getValue(), "getValue should return unchanged value after setMinValue with greater value");
    }

    @DisplayName("Volatile field mirrors stored long number")
    @Test
    void testGetAndSetVolatileValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setVolatileValue(123L);
        assertEquals(123L, longValue.getVolatileValue(), "getVolatileValue should return value set by setVolatileValue");
    }

    @DisplayName("Ordered write updates underlying long field")
    @Test
    void testSetOrderedValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setOrderedValue(456L);
        assertEquals(456L, longValue.getValue(), "getValue should return value set by setOrderedValue");
    }

    @DisplayName("Atomic add adjusts long number for delta")
    @Test
    void testAddAtomicValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        longValue.addAtomicValue(5L);
        assertEquals(15L, longValue.getValue(), "getValue should return sum after addAtomicValue with positive value");
        longValue.addAtomicValue(-3L);
        assertEquals(12L, longValue.getValue(), "getValue should return sum after addAtomicValue with negative value");
    }

    @DisplayName("Close flag reports closed state transitions")
    @Test
    void testIsClosed() {
        LongValueImpl longValue = new LongValueImpl();
        assertFalse(longValue.isClosed(), "isClosed should return false before close");

        longValue.close();
        assertTrue(longValue.isClosed(), "isClosed should return true after close");
    }

    @DisplayName("Volatile access mirrors stored long number")
    @Test
    void testGetVolatileValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getVolatileValue(), "getVolatileValue should return value set by setValue");
    }

    @DisplayName("Closed fallback uses provided long number")
    @Test
    void testGetVolatileValueWithClosedValue() {
        LongValueImpl longValue = new LongValueImpl();
        longValue.setValue(10L);
        assertEquals(10L, longValue.getVolatileValue(20L), "getVolatileValue should return current value when not closed");

        longValue.close();
        assertEquals(20L, longValue.getVolatileValue(20L), "getVolatileValue should return closed value when closed");
    }
}
