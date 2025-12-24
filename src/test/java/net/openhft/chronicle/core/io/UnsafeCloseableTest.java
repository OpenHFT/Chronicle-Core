/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

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

    @Test
    void testGetLong() {
        assertThrows(IllegalStateException.class, uc::getLong,
                "getLong should reject closed instance");
    }

    @Test
    void testSetLong() {
        assertThrows(IllegalStateException.class, () -> uc.setLong(0),
                "setLong should reject closed instance");
    }

    @Test
    void testGetVolatileLong() {
        assertEquals(128, uc.getVolatileLong(128), "getVolatileLong should return fallback value when called on closed UnsafeCloseable");
    }

    @Test
    void testSetVolatileLong() {
        assertThrows(IllegalStateException.class, () -> uc.setVolatileLong(0),
                "setVolatileLong should reject closed instance");
    }

    @Test
    void testTestGetVolatileLong() {
        assertThrows(IllegalStateException.class, uc::getVolatileLong,
                "getVolatileLong should reject closed instance");
    }

    @Test
    void testSetOrderedLong() {
        assertThrows(IllegalStateException.class, () -> uc.setOrderedLong(0),
                "setOrderedLong should reject closed instance");
    }

    @Test
    void testAddLong() {
        assertThrows(IllegalStateException.class, () -> uc.addLong(0),
                "addLong should reject closed instance");
    }

    @Test
    void testAddAtomicLong() {
        assertThrows(IllegalStateException.class, () -> uc.addAtomicLong(0),
                "addAtomicLong should reject closed instance");
    }

    @Test
    void testCompareAndSwapLong() {
        assertThrows(IllegalStateException.class, () -> uc.compareAndSwapLong(0, 0),
                "compareAndSwapLong should reject closed instance");
    }
}
