/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnsafeCloseableTest {

    private final UnsafeCloseable uc;

    public UnsafeCloseableTest() {
        uc = new UnsafeCloseable() {
        };
        uc.close();
    }

    @Test
    public void testGetLong() {
        assertThrows(IllegalStateException.class, uc::getLong);
    }

    @Test
    public void testSetLong() {
        assertThrows(IllegalStateException.class, () -> uc.setLong(0));
    }

    @Test
    public void testGetVolatileLong() {
        assertEquals(128, uc.getVolatileLong(128), "getVolatileLong should return fallback value when called on closed UnsafeCloseable");
    }

    @Test
    public void testSetVolatileLong() {
        assertThrows(IllegalStateException.class, () -> uc.setVolatileLong(0));
    }

    @Test
    public void testTestGetVolatileLong() {
        assertThrows(IllegalStateException.class, uc::getVolatileLong);
    }

    @Test
    public void testSetOrderedLong() {
        assertThrows(IllegalStateException.class, () -> uc.setOrderedLong(0));
    }

    @Test
    public void testAddLong() {
        assertThrows(IllegalStateException.class, () -> uc.addLong(0));
    }

    @Test
    public void testAddAtomicLong() {
        assertThrows(IllegalStateException.class, () -> uc.addAtomicLong(0));
    }

    @Test
    public void testCompareAndSwapLong() {
        assertThrows(IllegalStateException.class, () -> uc.compareAndSwapLong(0, 0));
    }
}
