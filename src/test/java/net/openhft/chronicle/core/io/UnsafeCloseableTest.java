/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnsafeCloseableTest {

    private final UnsafeCloseable uc;

    public UnsafeCloseableTest() {
        uc = new UnsafeCloseable() {
        };
        uc.close();
    }

    @DisplayName("testGetLong behaviour under expected input and output conditions")
    @Test
    void testGetLong() {
        assertThrows(IllegalStateException.class, uc::getLong,
                "getLong should throw when called on closed UnsafeCloseable");
    }

    @DisplayName("testSetLong behaviour under expected input and output conditions")
    @Test
    void testSetLong() {
        assertThrows(IllegalStateException.class, () -> uc.setLong(0),
                "setLong should throw when called on closed UnsafeCloseable");
    }

    @DisplayName("testGetVolatileLong behaviour under expected input and output conditions")
    @Test
    void testGetVolatileLong() {
        assertEquals(128, uc.getVolatileLong(128), "getVolatileLong should return fallback value when called on closed UnsafeCloseable");
    }

    @DisplayName("testSetVolatileLong behaviour under expected input and output conditions")
    @Test
    void testSetVolatileLong() {
        assertThrows(IllegalStateException.class, () -> uc.setVolatileLong(0),
                "setVolatileLong should throw when called on closed UnsafeCloseable");
    }

    @DisplayName("testTestGetVolatileLong behaviour under expected input and output conditions")
    @Test
    void testTestGetVolatileLong() {
        assertThrows(IllegalStateException.class, uc::getVolatileLong,
                "getVolatileLong should throw when called on closed UnsafeCloseable");
    }

    @DisplayName("testSetOrderedLong behaviour under expected input and output conditions")
    @Test
    void testSetOrderedLong() {
        assertThrows(IllegalStateException.class, () -> uc.setOrderedLong(0),
                "setOrderedLong should throw when called on closed UnsafeCloseable");
    }

    @DisplayName("testAddLong behaviour under expected input and output conditions")
    @Test
    void testAddLong() {
        assertThrows(IllegalStateException.class, () -> uc.addLong(0),
                "addLong should throw when called on closed UnsafeCloseable");
    }

    @DisplayName("testAddAtomicLong behaviour under expected input and output conditions")
    @Test
    void testAddAtomicLong() {
        assertThrows(IllegalStateException.class, () -> uc.addAtomicLong(0),
                "addAtomicLong should throw when called on closed UnsafeCloseable");
    }

    @DisplayName("testCompareAndSwapLong behaviour under expected input and output conditions")
    @Test
    void testCompareAndSwapLong() {
        assertThrows(IllegalStateException.class, () -> uc.compareAndSwapLong(0, 0),
                "compareAndSwapLong should throw when called on closed UnsafeCloseable");
    }
}
