/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link UnsafeMemory#isEqual(long, String, int)} compares 8-bit text in native memory with a String.
 * Bytes are unsigned: 0x80 to 0xFF must match the characters 128 to 255, as written by write8bit.
 */
class UnsafeMemoryIsEqualTest extends CoreTestCommon {

    private static final int CAPACITY = 512;

    private long addr;

    static Stream<UnsafeMemory> memories() {
        return Stream.of(new UnsafeMemory(), new UnsafeMemory.ARMMemory());
    }

    @BeforeEach
    void allocate() {
        addr = UNSAFE.allocateMemory(CAPACITY);
        UNSAFE.setMemory(addr, CAPACITY, (byte) 0);
    }

    @AfterEach
    void free() {
        UNSAFE.freeMemory(addr);
        addr = 0;
    }

    private static String allByteValues() {
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < 256; i++)
            sb.append((char) i);
        return sb.toString();
    }

    @ParameterizedTest
    @MethodSource("memories")
    void everyUnsignedByteValueMatchesTheCharacterWithTheSameValue(UnsafeMemory memory) {
        for (int i = 0; i < 256; i++)
            UNSAFE.putByte(addr + i, (byte) i);

        assertTrue(memory.isEqual(addr, allByteValues(), 256));
    }

    @ParameterizedTest
    @MethodSource("memories")
    void eachHighByteMatchesItsOwnCharacterOnly(UnsafeMemory memory) {
        for (int value = 128; value < 256; value++) {
            UNSAFE.putByte(addr, (byte) value);

            assertTrue(memory.isEqual(addr, String.valueOf((char) value), 1), "byte " + value);
            assertFalse(memory.isEqual(addr, String.valueOf((char) (value - 128)), 1), "byte " + value + " low twin");
        }
    }

    @ParameterizedTest
    @MethodSource("memories")
    void textWrittenAs8bitComparesEqual(UnsafeMemory memory) {
        // Latin-1 text with characters above 127, written with unicode escapes to keep this file ASCII
        String text = "caf\u00e9 cr\u00e8me \u00a3" + "42 \u00ff";
        memory.write8bit(text, 0, null, addr, text.length());

        assertTrue(memory.isEqual(addr, text, text.length()));
    }

    @ParameterizedTest
    @MethodSource("memories")
    void asciiTextStillComparesEqual(UnsafeMemory memory) {
        String text = "ORDER-0000123";
        memory.write8bit(text, 0, null, addr, text.length());

        assertTrue(memory.isEqual(addr, text, text.length()));
    }

    @ParameterizedTest
    @MethodSource("memories")
    void aDifferingByteIsDetectedAtAnyPosition(UnsafeMemory memory) {
        String text = allByteValues();
        for (int position = 0; position < 256; position += 17) {
            for (int i = 0; i < 256; i++)
                UNSAFE.putByte(addr + i, (byte) i);
            UNSAFE.putByte(addr + position, (byte) (position + 1));

            assertFalse(memory.isEqual(addr, text, 256), "position " + position);
        }
    }

    @ParameterizedTest
    @MethodSource("memories")
    void onlyTheRequestedLengthIsCompared(UnsafeMemory memory) {
        String text = "\u00c4BC-and-a-different-tail";
        memory.write8bit("\u00c4BC-but-not-the-same", 0, null, addr, 4);

        assertTrue(memory.isEqual(addr, text, 4));
        assertFalse(memory.isEqual(addr, text, 6));
    }

    @ParameterizedTest
    @MethodSource("memories")
    void aCharacterAbove255NeverMatchesTheByteWithItsLowBits(UnsafeMemory memory) {
        UNSAFE.putByte(addr, (byte) 0x41);
        UNSAFE.putByte(addr + 1, (byte) 0x00);

        assertFalse(memory.isEqual(addr, "\u0141", 1));
        assertFalse(memory.isEqual(addr + 1, "\u0100", 1));
        assertTrue(memory.isEqual(addr, "A", 1));
    }
}
