/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest extends CoreTestCommon {

    @Test
    void testIsEqualWithStringBuilderAndCharSequence() {
        StringBuilder sb = new StringBuilder("test");
        CharSequence cs = "test";

        assertTrue(StringUtils.isEqual(sb, cs));
        assertFalse(StringUtils.isEqual(sb, "different"));
    }

    @Test
    void testSetLengthOfStringBuilder() {
        StringBuilder sb = new StringBuilder("test");
        StringUtils.setLength(sb, 2);

        assertEquals("te", sb.toString());
    }

    @Test
    void testSetStringBuilderContent() {
        StringBuilder sb = new StringBuilder("original");
        StringUtils.set(sb, "updated");

        assertEquals("updated", sb.toString());
    }

    @Test
    void testEndsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.endsWith(cs, "String"));
        assertTrue(StringUtils.endsWith(cs, "string")); // case-insensitive
    }

    @Test
    void testStartsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.startsWith(cs, "test"));
        assertFalse(StringUtils.startsWith(cs, "String"));
    }

    @Test
    void testIsEqualWithCharSequences() {
        CharSequence cs1 = "test";
        CharSequence cs2 = "test";
        CharSequence cs3 = "different";

        assertTrue(StringUtils.isEqual(cs1, cs2));
        assertFalse(StringUtils.isEqual(cs1, cs3));
    }

    @Test
    void testEqualsCaseIgnore() {
        CharSequence cs1 = "TestString";
        CharSequence cs2 = "teststring";

        assertTrue(StringUtils.equalsCaseIgnore(cs1, cs2));
        assertFalse(StringUtils.equalsCaseIgnore(cs1, "AnotherString"));
    }

    @Test
    void testToStringMethod() {
        Object obj = "test";
        assertNull(StringUtils.toString(null));
        assertEquals("test", StringUtils.toString(obj));
    }

    @Test
    void testExtractBytesString() {
        String str = "test";
        byte[] expectedBytes = str.getBytes(StandardCharsets.ISO_8859_1);
        assertArrayEquals(expectedBytes, StringUtils.extractBytes(str));
    }

    @Test
    void testNewStringFromChars() {
        char[] chars = {'t', 'e', 's', 't'};
        assertEquals("test", StringUtils.newString(chars));
    }

    @Test
    void testNewStringFromBytes() {
        byte[] bytes = "test".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals("test", StringUtils.newStringFromBytes(bytes));
    }

    @Test
    void testFirstLowerCase() {
        assertEquals("", StringUtils.firstLowerCase(""));
        assertEquals("99", StringUtils.firstLowerCase("99"));
        assertEquals("a", StringUtils.firstLowerCase("A"));
        assertEquals("a", StringUtils.firstLowerCase("a"));
        assertEquals("aA", StringUtils.firstLowerCase("AA"));
        assertEquals("aa", StringUtils.firstLowerCase("Aa"));
    }

    @Test
    void testToTitleCase() {
        assertEquals("", StringUtils.toTitleCase(""));
        assertEquals("99", StringUtils.toTitleCase("99"));
        assertEquals("A", StringUtils.toTitleCase("A"));
        assertEquals("A", StringUtils.toTitleCase("a"));
        assertEquals("AA", StringUtils.toTitleCase("AA"));
        assertEquals("AA", StringUtils.toTitleCase("Aa"));
        assertEquals("AAA", StringUtils.toTitleCase("AAA"));
        assertEquals("AA_A", StringUtils.toTitleCase("AaA"));
        assertEquals("A_AA", StringUtils.toTitleCase("AAa"));
        assertEquals("AAA", StringUtils.toTitleCase("Aaa"));

        assertEquals("AAAA", StringUtils.toTitleCase("AAAA"));
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAA"));
        assertEquals("A_AA_A", StringUtils.toTitleCase("AAaA"));
        assertEquals("AAA_A", StringUtils.toTitleCase("AaaA"));
        assertEquals("AA_AA", StringUtils.toTitleCase("AAAa"));
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAa"));
        assertEquals("A_AAA", StringUtils.toTitleCase("AAaa"));
        assertEquals("AAAA", StringUtils.toTitleCase("Aaaa"));
    }

    @Test
    void shouldGetCharsOfStringBuilder() {
        final StringBuilder sb = new StringBuilder(11).append("foobar_nine");
        final char[] chars = StringUtils.extractChars(sb);
        assertEquals(sb.toString(), new String(chars));
    }

    @Test
    void shouldGetCharsOfString() {
        final String s = "foobar_nine";
        final char[] chars = StringUtils.extractChars(s);
        assertEquals(s, new String(chars));
    }

    @Test
    void shouldExtractBytesFromString() {
        assertTrue(Arrays.equals(
                "foobar".getBytes(StandardCharsets.US_ASCII),
                StringUtils.extractBytes("foobar")));
    }

    @Test
    void shouldExtractBytesFromStringBuilder() {
        // uses StringUtils.extractBytes/extractChars as appropriate
        assertEquals(0xdf8d42fa7e05af8aL, Maths.hash64(new StringBuilder("foobar")));
    }

    @Test
    void shouldCreateNewStringFromChars() {
        final char[] chars = {'A', 'B', 'C'};
        assertEquals(new String(chars), StringUtils.newString(chars));
    }

    @Test
    void shouldCreateNewStringFromBytes() {
        final byte[] bytes = {'A', 'B', 'C'};
        String expected = new String(bytes, StandardCharsets.ISO_8859_1);
        String actual = StringUtils.newStringFromBytes(bytes);
        assertEquals(expected, actual);
    }

    @Test
    void testParseDouble() {
        for (double d : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double
                .POSITIVE_INFINITY, 0.0, -1.0, 1.0, 9999.0}) {
            assertEquals(d, StringUtils.parseDouble(Double.toString(d)), 0);
        }

        assertEquals(1.0, StringUtils.parseDouble("1"), 0);
        assertEquals(0.0, StringUtils.parseDouble("-0"), 0);
        assertEquals(123.0, StringUtils.parseDouble("123"), 0);
        assertEquals(-1.0, StringUtils.parseDouble("-1"), 0);
    }

    @Test
    void testParseDoubleEdgeCases() {
        // Trailing dot
        assertEquals(123.0, StringUtils.parseDouble("123."), 0);
        // Leading dot
        assertEquals(0.5, StringUtils.parseDouble(".5"), 0);
        assertEquals(-0.5, StringUtils.parseDouble("-.5"), 0);
        // Lone dot currently treated as zero by the parser
        assertEquals(0.0, StringUtils.parseDouble("."), 0);
        // Large integer value remains finite and comparable to JDK parse
        String big = "9223372036854775807"; // Long.MAX_VALUE as a string
        assertEquals(Double.parseDouble(big), StringUtils.parseDouble(big), 0);
    }

    @Test
    void testParseInt() {
        validate((s, integer) -> (long) StringUtils.parseInt(s, integer));
    }

    @Test
    void testParseLong() {
        validate(StringUtils::parseLong);
    }

    private static void validate(BiFunction<String, Integer, Long> method) {
        assertEquals(100, (long) method.apply("100", 10));
        assertEquals(-100, (long) method.apply("-100", 10));

        // Lone char
        NumberFormatException firstCharEx = assertThrows(NumberFormatException.class, () -> method.apply("+", 10));
        assertEquals("For input string: \"+\"", firstCharEx.getMessage());

        // Null
        NumberFormatException nullEx = assertThrows(NumberFormatException.class, () -> method.apply(null, 0));
        assertEquals("null", nullEx.getMessage());

        // Max radix
        NumberFormatException maxRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 37));
        assertEquals("radix 37 greater than Character.MAX_RADIX", maxRadixEx.getMessage());

        // Min radix
        NumberFormatException minRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 0));
        assertEquals("radix 0 less than Character.MIN_RADIX", minRadixEx.getMessage());
    }

    @Test
    void reverse() {
        StringBuilder stringBuilder = new StringBuilder("test");
        StringUtils.reverse(stringBuilder, 0);
        assertEquals("tset", stringBuilder.toString());
    }

    @Test
    void equalsCaseIgnore_equals() {
        assertTrue(StringUtils.equalsCaseIgnore("aaa", "AAA"));
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AAAA"));
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AA_"));
    }

    @Test
    void startsWith_isValidPrefix() {
        assertTrue(StringUtils.startsWith("abcd", "ab"));
        assertFalse(StringUtils.startsWith("abcd", "abe"));
    }

    @Test
    void startsWith_searchStringTooLong() {
        assertFalse(StringUtils.startsWith("a", "ab"));
    }

    @Test
    void endsWith_isValidSuffix() {
        assertTrue(StringUtils.endsWith("abcd", "cd"));
        assertFalse(StringUtils.endsWith("abcd", "ed"));
    }

    @Test
    void endsWith_searchStringIsTooLong() {
        assertFalse(StringUtils.endsWith("abcd", "aaabcd"));
    }

    @Test
    void testIsEqual() {

        // The same instances
        StringBuilder emptySb = new StringBuilder();
        assertTrue(StringUtils.isEqual(emptySb, emptySb));

        // Null cases
        assertTrue(StringUtils.isEqual(null, null));
        assertFalse(StringUtils.isEqual(emptySb, null));
        assertFalse(StringUtils.isEqual(null, emptySb));

        // Different lengths
        assertFalse(StringUtils.isEqual(new StringBuilder(), "a"));

        // Same lengths & ASCII
        assertFalse(StringUtils.isEqual(new StringBuilder().append("a"), "b"));
        assertFalse(StringUtils.isEqual(new StringBuilder().append("test"), "Test"));
        assertTrue(StringUtils.isEqual(new StringBuilder().append("TheSame"), "TheSame"));

        // Same lengths & UTF-8
        assertFalse(StringUtils.isEqual(new StringBuilder().append("Δ"), "Γ"));
        assertFalse(StringUtils.isEqual(new StringBuilder().append("ΔΔΔΔΔ"), "ΔΔ€ΔΔ"));
        assertTrue(StringUtils.isEqual(new StringBuilder().append("ΔΔΔΔΔ"), "ΔΔΔΔΔ"));

        // Empty strings
        assertTrue(StringUtils.isEqual(new StringBuilder(), ""));
    }
}
