/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Intentionally does not extend CoreTestCommon: that base class uses JUnit 4
// @Before/@After and this project runs only the Jupiter engine (no vintage),
// so inherited lifecycle methods would silently not fire.
@DisplayName("MemoryAegis assertion edge coverage tests")
class MemoryAegisTest {

    @Test
    @DisplayName("accepts valid byte-array slices, including zero-length at the end")
    void byteArrayRangeAcceptsValidSlices() {
        byte[] bytes = new byte[8];

        assertTrue(MemoryAegis.assertByteArrayRange(bytes, 0, bytes.length));
        assertTrue(MemoryAegis.assertByteArrayRange(bytes, bytes.length, 0));
        assertTrue(MemoryAegis.assertByteArrayRange(bytes, 3, 4));
    }

    @Test
    @DisplayName("rejects a null byte array")
    void byteArrayRangeRejectsNullArray() {
        assertAssertionError("bytes must not be null",
                () -> MemoryAegis.assertByteArrayRange(null, 0, 1));
    }

    @Test
    @DisplayName("rejects a negative byte-array offset")
    void byteArrayRangeRejectsNegativeOffset() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], -1, 1));
    }

    @Test
    @DisplayName("rejects a negative byte-array length")
    void byteArrayRangeRejectsNegativeLength() {
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], 0, -1));
    }

    @Test
    @DisplayName("rejects a byte-array length larger than the array")
    void byteArrayRangeRejectsLengthBeyondSize() {
        assertAssertionError("length exceeds bytes.length",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], 0, 9));
    }

    @Test
    @DisplayName("rejects a byte-array slice that overruns the end")
    void byteArrayRangeRejectsOverflowingSlice() {
        assertAssertionError("slice exceeds bytes.length",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], 6, 3));
    }

    @Test
    @DisplayName("rejects Integer.MIN_VALUE byte-array offset")
    void byteArrayRangeRejectsIntegerMinOffset() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], Integer.MIN_VALUE, 1));
    }

    @Test
    @DisplayName("rejects Integer.MIN_VALUE byte-array length")
    void byteArrayRangeRejectsIntegerMinLength() {
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], 0, Integer.MIN_VALUE));
    }

    @Test
    @DisplayName("rejects Integer.MAX_VALUE byte-array length against a small array")
    void byteArrayRangeRejectsIntegerMaxLength() {
        assertAssertionError("length exceeds bytes.length",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], 0, Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("rejects Integer.MAX_VALUE byte-array offset against a small array")
    void byteArrayRangeRejectsIntegerMaxOffset() {
        assertAssertionError("slice exceeds bytes.length",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], Integer.MAX_VALUE, 1));
    }

    @Test
    @DisplayName("rejects a negative byte-array offset even when length is zero")
    void byteArrayRangeRejectsNegativeOffsetWithZeroLength() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], -1, 0));
    }

    @Test
    @DisplayName("rejects a byte-array offset past the end even when length is zero")
    void byteArrayRangeRejectsOffsetPastEndWithZeroLength() {
        assertAssertionError("slice exceeds bytes.length",
                () -> MemoryAegis.assertByteArrayRange(new byte[8], 9, 0));
    }

    @Test
    @DisplayName("accepts a valid char-array slice")
    void charArrayRangeAcceptsValidSlice() {
        char[] chars = new char[4];
        assertTrue(MemoryAegis.assertCharArrayRange(chars, 1, 2));
    }

    @Test
    @DisplayName("rejects a null char array")
    void charArrayRangeRejectsNullArray() {
        assertAssertionError("chars must not be null",
                () -> MemoryAegis.assertCharArrayRange(null, 0, 1));
    }

    @Test
    @DisplayName("rejects a char-array slice that overruns the end")
    void charArrayRangeRejectsOverflowingSlice() {
        assertAssertionError("slice exceeds chars.length",
                () -> MemoryAegis.assertCharArrayRange(new char[4], 3, 2));
    }

    @Test
    @DisplayName("rejects a negative char-array offset")
    void charArrayRangeRejectsNegativeOffset() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertCharArrayRange(new char[4], -1, 1));
    }

    @Test
    @DisplayName("rejects a negative char-array length")
    void charArrayRangeRejectsNegativeLength() {
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertCharArrayRange(new char[4], 0, -1));
    }

    @Test
    @DisplayName("rejects Integer.MIN_VALUE char-array offset and length")
    void charArrayRangeRejectsIntegerMinEdges() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertCharArrayRange(new char[4], Integer.MIN_VALUE, 1));
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertCharArrayRange(new char[4], 0, Integer.MIN_VALUE));
    }

    @Test
    @DisplayName("rejects Integer.MAX_VALUE char-array length and offset against a small array")
    void charArrayRangeRejectsIntegerMaxEdges() {
        assertAssertionError("length exceeds chars.length",
                () -> MemoryAegis.assertCharArrayRange(new char[4], 0, Integer.MAX_VALUE));
        assertAssertionError("slice exceeds chars.length",
                () -> MemoryAegis.assertCharArrayRange(new char[4], Integer.MAX_VALUE, 1));
    }

    @Test
    @DisplayName("rejects an invalid char-array offset even when length is zero")
    void charArrayRangeRejectsInvalidOffsetWithZeroLength() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertCharArrayRange(new char[4], -1, 0));
        assertAssertionError("slice exceeds chars.length",
                () -> MemoryAegis.assertCharArrayRange(new char[4], 5, 0));
    }

    @Test
    @DisplayName("accepts a valid string slice")
    void stringRangeAcceptsValidSlice() {
        assertTrue(MemoryAegis.assertStringRange("abcd", 1, 2));
    }

    @Test
    @DisplayName("rejects a null input string")
    void stringRangeRejectsNullText() {
        assertAssertionError("text must not be null",
                () -> MemoryAegis.assertStringRange(null, 0, 1));
    }

    @Test
    @DisplayName("rejects a string slice that overruns the end")
    void stringRangeRejectsPastEnd() {
        assertAssertionError("slice exceeds text.length()",
                () -> MemoryAegis.assertStringRange("abc", 2, 2));
    }

    @Test
    @DisplayName("rejects Integer.MIN_VALUE string start and length")
    void stringRangeRejectsIntegerMinEdges() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertStringRange("abc", Integer.MIN_VALUE, 1));
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertStringRange("abc", 0, Integer.MIN_VALUE));
    }

    @Test
    @DisplayName("rejects Integer.MAX_VALUE string length and start against a short string")
    void stringRangeRejectsIntegerMaxEdges() {
        assertAssertionError("length exceeds text.length()",
                () -> MemoryAegis.assertStringRange("abc", 0, Integer.MAX_VALUE));
        assertAssertionError("slice exceeds text.length()",
                () -> MemoryAegis.assertStringRange("abc", Integer.MAX_VALUE, 1));
    }

    @Test
    @DisplayName("rejects an invalid string start even when length is zero")
    void stringRangeRejectsInvalidStartWithZeroLength() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertStringRange("abc", -1, 0));
        assertAssertionError("slice exceeds text.length()",
                () -> MemoryAegis.assertStringRange("abc", 4, 0));
    }

    @Test
    @DisplayName("accepts the configured native lower bound")
    void addressRangeAcceptsConfiguredLowerBound() {
        assertTrue(MemoryAegis.assertAddressRange(NativeAddressSpace.minAddress(), 1));
    }

    @Test
    @DisplayName("accepts zero-length access at the configured native lower bound")
    void addressRangeAcceptsZeroLengthAtLowerBound() {
        assertTrue(MemoryAegis.assertAddressRange(NativeAddressSpace.minAddress(), 0));
    }

    @Test
    @DisplayName("accepts zero-length access at the inclusive native upper bound")
    void addressRangeAcceptsZeroLengthAtConfiguredUpperBound() {
        assertTrue(MemoryAegis.assertAddressRange(NativeAddressSpace.maxAddressInclusive(), 0));
    }

    @Test
    @DisplayName("accepts a single-byte access that ends exactly at the native ceiling")
    void addressRangeAcceptsSingleByteRangeEndingAtCeiling() {
        assertTrue(MemoryAegis.assertAddressRange(NativeAddressSpace.maxAddressInclusive(), 1));
    }

    @Test
    @DisplayName("rejects a negative native length")
    void addressRangeRejectsNegativeLength() {
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertAddressRange(NativeAddressSpace.minAddress(), -1));
    }

    @Test
    @DisplayName("rejects reserved low native address space")
    void addressRangeRejectsReservedLowAddressSpace() {
        assertAssertionError("address must be >=",
                () -> MemoryAegis.assertAddressRange(NativeAddressSpace.minAddress() - 1, 1));
    }

    @Test
    @DisplayName("rejects the exclusive native upper bound even for zero-length access")
    void addressRangeRejectsConfiguredExclusiveUpperBound() {
        assertAssertionError("address must be <=",
                () -> MemoryAegis.assertAddressRange(NativeAddressSpace.maxAddressExclusive(), 0));
    }

    @Test
    @DisplayName("rejects a native range that overflows the configured address space")
    void addressRangeRejectsConfiguredUpperBoundOverflow() {
        assertAssertionError("native range exceeds configured address space",
                () -> MemoryAegis.assertAddressRange(NativeAddressSpace.maxAddressInclusive(), 2));
    }

    @Test
    @DisplayName("rejects Long.MIN_VALUE as a native address")
    void addressRangeRejectsLongMinAddress() {
        assertAssertionError("address must be >=",
                () -> MemoryAegis.assertAddressRange(Long.MIN_VALUE, 1));
    }

    @Test
    @DisplayName("rejects Long.MAX_VALUE as a native address")
    void addressRangeRejectsLongMaxAddress() {
        assertAssertionError("address must be <=",
                () -> MemoryAegis.assertAddressRange(Long.MAX_VALUE, 1));
    }

    @Test
    @DisplayName("rejects Long.MIN_VALUE as a native length")
    void addressRangeRejectsLongMinLength() {
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertAddressRange(NativeAddressSpace.minAddress(), Long.MIN_VALUE));
    }

    @Test
    @DisplayName("rejects Long.MAX_VALUE as a native length at a valid address")
    void addressRangeRejectsLongMaxLength() {
        assertAssertionError("native range exceeds configured address space",
                () -> MemoryAegis.assertAddressRange(NativeAddressSpace.minAddress(), Long.MAX_VALUE));
    }

    @Test
    @DisplayName("rejects the legacy x86-64 [vsyscall] address")
    void addressRangeRejectsVsyscallMapping() {
        // The legacy x86-64 vsyscall page sits at a sign-extended kernel-canonical
        // address (0xFFFFFFFFFF600000). MemoryAegis deliberately models only
        // ordinary non-negative user-space ranges, so the negative long value
        // must be rejected by the lower-bound guard.
        final long vsyscallAddress = 0xFFFFFFFFFF600000L;
        assertTrue(vsyscallAddress < 0, "vsyscall address should be negative in signed long");
        assertAssertionError("address must be >=",
                () -> MemoryAegis.assertAddressRange(vsyscallAddress, 1));
    }

    @Test
    @DisplayName("rejects an invalid native address even when length is zero")
    void addressRangeRejectsInvalidAddressWithZeroLength() {
        assertAssertionError("address must be >=",
                () -> MemoryAegis.assertAddressRange(NativeAddressSpace.minAddress() - 1, 0));
        assertAssertionError("address must be >=",
                () -> MemoryAegis.assertAddressRange(Long.MIN_VALUE, 0));
        assertAssertionError("address must be <=",
                () -> MemoryAegis.assertAddressRange(Long.MAX_VALUE, 0));
    }

    @Test
    @DisplayName("accepts zero object-relative offset")
    void objectRangeAcceptsZeroOffset() {
        assertTrue(MemoryAegis.assertObjectRange(new Object(), 0, 4));
    }

    @Test
    @DisplayName("accepts zero-length object-relative access at zero offset")
    void objectRangeAcceptsZeroLengthAtZeroOffset() {
        assertTrue(MemoryAegis.assertObjectRange(new Object(), 0, 0));
    }

    @Test
    @DisplayName("accepts zero-length object-relative access at the inclusive upper bound")
    void objectRangeAcceptsZeroLengthAtConfiguredUpperBound() {
        assertTrue(MemoryAegis.assertObjectRange(new Object(), NativeAddressSpace.maxAddressInclusive(), 0));
    }

    @Test
    @DisplayName("accepts a single-byte object-relative access that ends exactly at the ceiling")
    void objectRangeAcceptsSingleByteRangeEndingAtCeiling() {
        assertTrue(MemoryAegis.assertObjectRange(new Object(), NativeAddressSpace.maxAddressInclusive(), 1));
    }

    @Test
    @DisplayName("accepts a typical object field offset")
    void objectRangeAcceptsTypicalFieldOffset() {
        assertTrue(MemoryAegis.assertObjectRange(new Object(), 16, Long.BYTES));
    }

    @Test
    @DisplayName("rejects a null object target")
    void objectRangeRejectsNullObject() {
        assertAssertionError("object must not be null",
                () -> MemoryAegis.assertObjectRange(null, 0, 1));
    }

    @Test
    @DisplayName("rejects a negative object-relative offset")
    void objectRangeRejectsNegativeOffset() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertObjectRange(new Object(), -1, 1));
    }

    @Test
    @DisplayName("rejects a negative object-relative length")
    void objectRangeRejectsNegativeLength() {
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertObjectRange(new Object(), 0, -1));
    }

    @Test
    @DisplayName("rejects the exclusive object-relative upper bound even for zero-length access")
    void objectRangeRejectsConfiguredExclusiveUpperBound() {
        assertAssertionError("offset must be <=",
                () -> MemoryAegis.assertObjectRange(new Object(), NativeAddressSpace.maxAddressExclusive(), 0));
    }

    @Test
    @DisplayName("rejects an object-relative range that overflows the configured address space")
    void objectRangeRejectsConfiguredUpperBoundOverflow() {
        assertAssertionError("object range exceeds configured address space",
                () -> MemoryAegis.assertObjectRange(new Object(), NativeAddressSpace.maxAddressInclusive(), 2));
    }

    @Test
    @DisplayName("rejects an object-relative length at or above the exclusive upper bound")
    void objectRangeRejectsLengthAtConfiguredExclusiveUpperBound() {
        assertAssertionError("length must be <=",
                () -> MemoryAegis.assertObjectRange(new Object(), 0, NativeAddressSpace.maxAddressExclusive()));
    }

    @Test
    @DisplayName("rejects Long.MIN_VALUE object-relative offset")
    void objectRangeRejectsLongMinOffset() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertObjectRange(new Object(), Long.MIN_VALUE, 1));
    }

    @Test
    @DisplayName("rejects Long.MAX_VALUE object-relative offset")
    void objectRangeRejectsLongMaxOffset() {
        assertAssertionError("offset must be <=",
                () -> MemoryAegis.assertObjectRange(new Object(), Long.MAX_VALUE, 1));
    }

    @Test
    @DisplayName("rejects Long.MIN_VALUE object-relative length")
    void objectRangeRejectsLongMinLength() {
        assertAssertionError("length must be >= 0",
                () -> MemoryAegis.assertObjectRange(new Object(), 0, Long.MIN_VALUE));
    }

    @Test
    @DisplayName("rejects Long.MAX_VALUE object-relative length")
    void objectRangeRejectsLongMaxLength() {
        assertAssertionError("length must be <=",
                () -> MemoryAegis.assertObjectRange(new Object(), 0, Long.MAX_VALUE));
    }

    @Test
    @DisplayName("rejects an invalid object-relative offset even when length is zero")
    void objectRangeRejectsInvalidOffsetWithZeroLength() {
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertObjectRange(new Object(), -1, 0));
        assertAssertionError("offset must be >= 0",
                () -> MemoryAegis.assertObjectRange(new Object(), Long.MIN_VALUE, 0));
        assertAssertionError("offset must be <=",
                () -> MemoryAegis.assertObjectRange(new Object(), Long.MAX_VALUE, 0));
    }

    private static void assertAssertionError(final String messagePart,
                                             final Runnable action) {
        final AssertionError actual = assertThrows(AssertionError.class, action::run);
        final String actualMessage = actual.getMessage();
        assertTrue(actualMessage != null && actualMessage.contains(messagePart),
                () -> "expected message to contain: \"" + messagePart
                        + "\", actual: \"" + actualMessage + "\"");
    }
}
