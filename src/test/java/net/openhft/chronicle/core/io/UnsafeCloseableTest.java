package net.openhft.chronicle.core.io;

import junit.framework.TestCase;
import org.junit.Test;

public class UnsafeCloseableTest extends TestCase {

    private final UnsafeCloseable uc;

    public UnsafeCloseableTest() {
        uc = new UnsafeCloseable() {
        };
        uc.close();
    }

    @Test
    public void testGetLong() {
        try {
            uc.getLong();
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testSetLong() {
        try {
            uc.setLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testGetVolatileLong() {
        assertEquals(128, uc.getVolatileLong(128));
    }

    @Test
    public void testSetVolatileLong() {
        try {
            uc.setVolatileLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testTestGetVolatileLong() {
        try {
            uc.getVolatileLong();
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testSetOrderedLong() {
        try {
            uc.setOrderedLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testAddLong() {
        try {
            uc.addLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testAddAtomicLong() {
        try {
            uc.addAtomicLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testCompareAndSwapLong() {
        try {
            uc.compareAndSwapLong(0, 0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }
}