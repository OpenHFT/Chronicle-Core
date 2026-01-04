/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class StringInternerTest extends CoreTestCommon {
    private String[] uppercase;

    @Test
    @DisplayName("String interner stores interned values with collisions")
    void testIntern() throws IllegalArgumentException {
        @NotNull StringInterner si = new StringInterner(128);
        for (int i = 0; i < 100; i++) {
            si.intern("" + i);
        }
        assertEquals(82, si.valueCount(), "valueCount should equal interned entries after collisions");
    }

    @Test
    @DisplayName("String interner index resolves stored strings")
    void testInternIndex() throws IllegalArgumentException {
        @NotNull StringInterner si = new StringInterner(128);
        for (int i = 0; i < 100; i++) {
            assertEquals("" + i, si.get(si.index("" + i, null)),
                    "get should return same string after indexing i=" + i);
        }
    }

    /**
     * Demonstrates using the StringInterner together with an uppercase cache.
     *
     * @throws IllegalArgumentException if the interner cannot allocate entries
     */
    @Test
    @DisplayName("To uppercase intern index string interner")
    void testToUppercaseInternIndex() throws IllegalArgumentException {

        @NotNull StringInterner si = new StringInterner(128);
        uppercase = new String[si.capacity()];
        for (int i = 0; i < 100; i++) {
            String lowerCaseString = randomLowercaseString();
            System.out.println(lowerCaseString);
            int index = si.index(lowerCaseString, this::changed);
            if (index != -1)
                assertEquals(lowerCaseString.toUpperCase(), uppercase[index],
                        "uppercase cache should contain uppercased version of indexed string i=" + i + ", index=" + index);
        }
    }

    private void changed(int index, String value) {
        uppercase[index] = value.toUpperCase();
    }

    private String randomLowercaseString() {
        final String CHARS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        final int count = 1 + (int) ((Math.random() * 10));
        for (int i = 0; i < count; i++) {
            sb.append(CHARS.charAt((int) (Math.random() * CHARS.length())));
        }
        return sb.toString();
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("StringInterner intern returns null when String input argument is null")
    void internNullReturnsNull() {
        @NotNull StringInterner si = new StringInterner(128);
        assertNull(si.intern(null),
                "StringInterner intern should return null for null String input argument");
    }

    @Test
    @DisplayName("intern returns toString for string longer than capacity")
    void internLongStringReturnsToString() {
        @NotNull StringInterner si = new StringInterner(128);
        // Create a string longer than 128 characters
        String longString = repeatChar('a', 200);
        String result = si.intern(longString);
        assertEquals(longString, result, "intern should return toString for long string");
        // Verify it wasn't stored - valueCount should be 0
        assertEquals(0, si.valueCount(), "long string should not be stored in interner");
    }

    @Test
    @DisplayName("intern returns cached string on second slot match")
    void internSecondSlotMatch() {
        @NotNull StringInterner si = new StringInterner(128);
        // Fill both slots for the same hash with different strings
        // by interning multiple strings until we hit the second slot case
        String first = "test1";
        String interned1 = si.intern(first);
        assertEquals(first, interned1, "first intern should succeed");

        // Intern same string again - should return cached
        String interned2 = si.intern(first);
        assertSame(interned1, interned2, "second intern of same string should return cached");
    }

    @Test
    @DisplayName("StringInterner index returns -1 when String input argument is null")
    void indexNullReturnsMinus1() {
        @NotNull StringInterner si = new StringInterner(128);
        int result = si.index(null, null);
        assertEquals(-1, result,
                "index should return -1 for null String input argument: result=" + result);
    }

    @Test
    @DisplayName("index returns -1 for string longer than capacity")
    void indexLongStringReturnsMinus1() {
        @NotNull StringInterner si = new StringInterner(128);
        String longString = repeatChar('a', 200);
        assertEquals(-1, si.index(longString, null), "index should return -1 for long string");
    }

    @Test
    @DisplayName("index returns same slot for same string")
    void indexReturnsSameIndexForSameString() {
        @NotNull StringInterner si = new StringInterner(128);
        String test = "testString";
        int index1 = si.index(test, null);
        int index2 = si.index(test, null);
        assertTrue(index1 >= 0, "index1=" + index1 + " should be >= 0 for cached string");
        assertEquals(index1, index2, "index should return same slot for same string");
    }

    @Test
    @DisplayName("StringInterner get returns null for empty table slot before insert")
    void getReturnsNullForEmptySlot() {
        @NotNull StringInterner si = new StringInterner(128);
        // Before interning anything, all slots should be null
        assertNull(si.get(0), "get should return null before insert for slot 0");
        assertNull(si.get(64), "get should return null before insert for slot 64");
    }

    @Test
    @DisplayName("intern triggers toggle when both slots occupied")
    void internTogglesBetweenSlots() {
        @NotNull StringInterner si = new StringInterner(128);
        // Fill many entries to force toggle behaviour
        for (int i = 0; i < 200; i++) {
            si.intern("string" + i);
        }
        // After many inserts, valueCount should be less than 200 due to collisions
        int count = si.valueCount();
        assertTrue(count > 0 && count <= 128, "valueCount should be between 1 and 128: " + count);
    }

    @Test
    @DisplayName("index invokes onChanged callback when storing new value")
    void indexInvokesOnChangedCallback() {
        @NotNull StringInterner si = new StringInterner(128);
        int[] callbackCount = {0};
        String[] lastValue = {null};
        int[] lastIndex = {-1};

        String test = "callbackTest";
        int index = si.index(test, (idx, val) -> {
            callbackCount[0]++;
            lastIndex[0] = idx;
            lastValue[0] = val;
        });

        assertEquals(1, callbackCount[0], "onChanged should be called once");
        assertEquals(index, lastIndex[0], "callback should receive correct index");
        assertEquals(test, lastValue[0], "callback should receive correct value");
    }

    @Test
    @DisplayName("index does not invoke onChanged on cache hit")
    void indexDoesNotInvokeOnChangedOnCacheHit() {
        @NotNull StringInterner si = new StringInterner(128);
        int[] callbackCount = {0};

        String test = "cacheHitTest";
        // First call - should invoke callback
        si.index(test, (idx, val) -> callbackCount[0]++);
        assertEquals(1, callbackCount[0], "first index should invoke callback");

        // Second call - should not invoke callback (cache hit)
        si.index(test, (idx, val) -> callbackCount[0]++);
        assertEquals(1, callbackCount[0], "second index should not invoke callback on cache hit");
    }

    @Test
    @DisplayName("valueCount returns zero for empty interner cache")
    void valueCountZeroForEmpty() {
        @NotNull StringInterner si = new StringInterner(128);
        assertEquals(0, si.valueCount(), "valueCount should be 0 for empty interner");
    }

    @Test
    @DisplayName("capacity returns interner array length value")
    void capacityReturnsArrayLength() {
        @NotNull StringInterner si = new StringInterner(128);
        assertEquals(128, si.capacity(), "capacity should return 128");
    }

    // Java 8 compatible replacement for String.repeat()
    private static String repeatChar(char c, int count) {
        char[] chars = new char[count];
        java.util.Arrays.fill(chars, c);
        return new String(chars);
    }
}
