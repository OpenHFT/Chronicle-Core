/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnsafeCloseableTest {

    private final UnsafeCloseable uc;

    public UnsafeCloseableTest() {
        uc = new UnsafeCloseable() {
        };
        uc.close();
    }

    @Test
    void testGetLong() {
        try {
            uc.getLong();
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    void testSetLong() {
        try {
            uc.setLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    void testGetVolatileLong() {
        assertEquals(128, uc.getVolatileLong(128));
    }

    @Test
    void testSetVolatileLong() {
        try {
            uc.setVolatileLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    void testTestGetVolatileLong() {
        try {
            uc.getVolatileLong();
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    void testSetOrderedLong() {
        try {
            uc.setOrderedLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    void testAddLong() {
        try {
            uc.addLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    void testAddAtomicLong() {
        try {
            uc.addAtomicLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    void testCompareAndSwapLong() {
        try {
            uc.compareAndSwapLong(0, 0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }
}
