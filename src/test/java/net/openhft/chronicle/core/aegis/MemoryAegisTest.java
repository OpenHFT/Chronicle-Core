/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MemoryAegisTest extends CoreTestCommon {

    @Test
    public void byteArrayRangeAcceptsValidSlices() {
        byte[] bytes = new byte[8];

        assertTrue(MemoryAegis.assertByteArrayRange(bytes, 0, bytes.length));
        assertTrue(MemoryAegis.assertByteArrayRange(bytes, bytes.length, 0));
    }

    @Test
    public void byteArrayRangeRejectsOverflowingSlice() {
        assertAssertionError("slice exceeds bytes.length",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], 6, 3));
    }

    @Test
    public void stringRangeRejectsPastEnd() {
        assertAssertionError("slice exceeds text.length()",
                () -> MemoryAegis.assertStringRange("abc", 2, 2));
    }

    @Test
    public void addressRangeRejectsWraparound() {
        assertAssertionError("native range wraps",
                () -> MemoryAegis.assertAddressRange(Long.MAX_VALUE - 1, 4));
    }

    @Test
    public void objectRangeRejectsZeroOffset() {
        assertAssertionError("offset must be > 0",
                () -> MemoryAegis.assertObjectRange(new Object(), 0, 1));
    }

    @Test
    public void objectOrAddressRangeAcceptsNativeWhenObjectNull() {
        assertTrue(MemoryAegis.assertObjectOrAddressRange(null, 64, Long.BYTES));
    }

    @Test
    public void objectOrAddressRangeRejectsWrappedNativeRange() {
        assertAssertionError("native range wraps",
                () -> MemoryAegis.assertObjectOrAddressRange(null, Long.MAX_VALUE - 1, 4));
    }

    private static void assertAssertionError(final String messagePart,
                                             final ThrowingRunnable action) {
        try {
            action.run();
            fail("Expected AssertionError");
        } catch (AssertionError expected) {
            assertTrue(expected.getMessage().contains(messagePart));
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }
}
