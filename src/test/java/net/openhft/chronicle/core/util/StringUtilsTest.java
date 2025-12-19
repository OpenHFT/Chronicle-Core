/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsTest extends CoreTestCommon {

    private static int validate(BiFunction<String, Integer, Long> method) {
        int scenarios = 0;
        assertEquals(100, (long) method.apply("100", 10), "should parse positive decimal string to numeric value");
        assertEquals(-100, (long) method.apply("-100", 10), "should parse negative decimal string to numeric value");
        scenarios += 2;

        // Lone char
        NumberFormatException firstCharEx = assertThrows(NumberFormatException.class, () -> method.apply("+", 10));
        assertEquals("For input string: \"+\"", firstCharEx.getMessage(), "should report invalid lone plus sign in error message");
        scenarios++;

        // Null
        NumberFormatException nullEx = assertThrows(NumberFormatException.class, () -> method.apply(null, 0));
        assertEquals("null", nullEx.getMessage(), "should report null input in error message");
        scenarios++;

        // Max radix
        NumberFormatException maxRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 37));
        assertEquals("radix 37 greater than Character.MAX_RADIX", maxRadixEx.getMessage(), "should report radix exceeding maximum in error message");
        scenarios++;

        // Min radix
        NumberFormatException minRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 0));
        assertEquals("radix 0 less than Character.MIN_RADIX", minRadixEx.getMessage(), "should report radix below minimum in error message");
        scenarios++;

        return scenarios;
    }

    @Test
    public void testIsEqualWithStringBuilderAndCharSequence() {
        StringBuilder sb = new StringBuilder("test");
        CharSequence cs = "test";

        assertTrue(StringUtils.isEqual(sb, cs), "should recognize StringBuilder and String with identical content as equal");
        assertFalse(StringUtils.isEqual(sb, "different"), "should recognize StringBuilder and String with different content as unequal");
    }

    @Test
    public void testSetLengthOfStringBuilder() {
        StringBuilder sb = new StringBuilder("test");
        StringUtils.setLength(sb, 2);

        assertEquals("te", sb.toString(), "should truncate StringBuilder to specified length");
    }

    @Test
    public void testSetStringBuilderContent() {
        StringBuilder sb = new StringBuilder("original");
        StringUtils.set(sb, "updated");

        assertEquals("updated", sb.toString(), "should replace StringBuilder content with new string");
    }

    @Test
    public void testEndsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.endsWith(cs, "String"), "should match exact case suffix");
        assertTrue(StringUtils.endsWith(cs, "string"), "should match suffix case-insensitively");
    }

    @Test
    public void testStartsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.startsWith(cs, "test"), "should match when prefix appears at start");
        assertFalse(StringUtils.startsWith(cs, "String"), "should not match when prefix appears only later in string");
    }

    @Test
    public void testIsEqualWithCharSequences() {
        CharSequence cs1 = "test";
        CharSequence cs2 = "test";
        CharSequence cs3 = "different";

        assertTrue(StringUtils.isEqual(cs1, cs2), "should recognize CharSequences with identical content as equal");
        assertFalse(StringUtils.isEqual(cs1, cs3), "should recognize CharSequences with different content as unequal");
    }

    @Test
    public void testEqualsCaseIgnore() {
        CharSequence cs1 = "TestString";
        CharSequence cs2 = "teststring";

        assertTrue(StringUtils.equalsCaseIgnore(cs1, cs2), "should match strings that differ only in case");
        assertFalse(StringUtils.equalsCaseIgnore(cs1, "AnotherString"), "should not match strings with different content");
    }

    @Test
    public void testToStringMethod() {
        Object obj = "test";
        assertNull(StringUtils.toString(null), "should return null when converting null object to string");
        assertEquals("test", StringUtils.toString(obj), "should convert non-null object to its string representation");
    }

    @Test
    public void testExtractBytesString() {
        String str = "test";
        byte[] expectedBytes = str.getBytes(UTF_8);
        assertArrayEquals(expectedBytes, StringUtils.extractBytes(str), "should extract UTF-8 byte array from string");
    }

    @Test
    public void testNewStringFromChars() {
        char[] chars = {'t', 'e', 's', 't'};
        assertEquals("test", StringUtils.newString(chars), "should create string from character array");
    }

    @Test
    public void testNewStringFromBytes() {
        byte[] bytes = "test".getBytes(UTF_8);
        assertEquals("test", StringUtils.newStringFromBytes(bytes), "should create string from UTF-8 byte array");
    }

    @Test
    public void testFirstLowerCase() {
        assertEquals("", StringUtils.firstLowerCase(""), "should return empty string unchanged when lowercasing first character");
        assertEquals("99", StringUtils.firstLowerCase("99"), "should return numeric string unchanged when no letter to lowercase");
        assertEquals("a", StringUtils.firstLowerCase("A"), "should convert single uppercase character to lowercase");
        assertEquals("a", StringUtils.firstLowerCase("a"), "should return single lowercase character unchanged");
        assertEquals("aA", StringUtils.firstLowerCase("AA"), "should lowercase only first character when multiple uppercase");
        assertEquals("aa", StringUtils.firstLowerCase("Aa"), "should lowercase only first character when already mixed case");
    }

    @Test
    public void testToTitleCase() {
        assertEquals("", StringUtils.toTitleCase(""), "should return empty string unchanged when converting to title case");
        assertEquals("99", StringUtils.toTitleCase("99"), "should return numeric string unchanged when converting to title case");
        assertEquals("A", StringUtils.toTitleCase("A"), "should return single uppercase letter unchanged");
        assertEquals("A", StringUtils.toTitleCase("a"), "should convert single lowercase letter to uppercase");
        assertEquals("AA", StringUtils.toTitleCase("AA"), "should return two uppercase letters unchanged");
        assertEquals("AA", StringUtils.toTitleCase("Aa"), "should uppercase second lowercase letter when first is uppercase");
        assertEquals("AAA", StringUtils.toTitleCase("AAA"), "should return three uppercase letters unchanged");
        assertEquals("AA_A", StringUtils.toTitleCase("AaA"), "should insert underscore before uppercase after lowercase");
        assertEquals("A_AA", StringUtils.toTitleCase("AAa"), "should insert underscore between uppercase run and final lowercase");
        assertEquals("AAA", StringUtils.toTitleCase("Aaa"), "should uppercase all lowercase letters after uppercase first");

        assertEquals("AAAA", StringUtils.toTitleCase("AAAA"), "should return four uppercase letters unchanged");
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAA"), "should insert underscore before uppercase sequence after lowercase");
        assertEquals("A_AA_A", StringUtils.toTitleCase("AAaA"), "should insert underscores around lowercase in mixed case sequence");
        assertEquals("AAA_A", StringUtils.toTitleCase("AaaA"), "should insert underscore before final uppercase after lowercase sequence");
        assertEquals("AA_AA", StringUtils.toTitleCase("AAAa"), "should insert underscore between uppercase run and lowercase-uppercase pair");
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAa"), "should insert underscore between two uppercase-lowercase pairs");
        assertEquals("A_AAA", StringUtils.toTitleCase("AAaa"), "should insert underscore before uppercased lowercase sequence");
        assertEquals("AAAA", StringUtils.toTitleCase("Aaaa"), "should uppercase all lowercase letters in title case word");
    }

    @Test
    public void shouldGetCharsOfStringBuilder() {
        final StringBuilder sb = new StringBuilder(11).append("foobar_nine");
        final char[] chars = StringUtils.extractChars(sb);
        assertEquals(sb.toString(), new String(chars), "should extract character array matching StringBuilder content");
    }

    @Test
    public void shouldGetCharsOfString() {
        final String s = "foobar_nine";
        final char[] chars = StringUtils.extractChars(s);
        assertEquals(s, new String(chars), "should extract character array matching String content");
    }

    @Test
    public void shouldExtractBytesFromString() {
        assertArrayEquals("foobar".getBytes(UTF_8), StringUtils.extractBytes("foobar"), "should extract UTF-8 bytes matching String encoding");
    }

    @Test
    public void shouldExtractBytesFromStringBuilder() {
        // uses StringUtils.extractBytes/extractChars as appropriate
        assertEquals(0xdf8d42fa7e05af8aL, Maths.hash64(new StringBuilder("foobar")), "should produce consistent hash from StringBuilder bytes");
    }

    @Test
    public void shouldCreateNewStringFromChars() {
        final char[] chars = {'A', 'B', 'C'};
        assertEquals(new String(chars), StringUtils.newString(chars), "should create string equivalent to standard String constructor");
    }

    @Test
    public void shouldCreateNewStringFromBytes() {
        final byte[] bytes = {'A', 'B', 'C'};
        String expected = new String(bytes, UTF_8);
        String actual = StringUtils.newStringFromBytes(bytes);
        assertEquals(expected, actual, "should create string from bytes equivalent to standard UTF-8 String constructor");
    }

    @Test
    public void testParseDouble() {
        for (double d : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double
                .POSITIVE_INFINITY, 0.0, -1.0, 1.0, 9999.0}) {
            assertEquals(d, StringUtils.parseDouble(Double.toString(d)), 0, "should parse double special values and round-trip through string representation");
        }

        assertEquals(1.0, StringUtils.parseDouble("1"), 0, "should parse integer string as double value");
        assertEquals(0.0, StringUtils.parseDouble("-0"), 0, "should parse negative zero string as positive zero");
        assertEquals(123.0, StringUtils.parseDouble("123"), 0, "should parse multi-digit integer string as double");
        assertEquals(-1.0, StringUtils.parseDouble("-1"), 0, "should parse negative integer string as double");
    }

    @Test
    public void testParseDoubleEdgeCases() {
        // Trailing dot
        assertEquals(123.0, StringUtils.parseDouble("123."), 0, "should parse number with trailing decimal point");
        // Leading dot
        assertEquals(0.5, StringUtils.parseDouble(".5"), 0, "should parse decimal with leading dot and no integer part");
        assertEquals(-0.5, StringUtils.parseDouble("-.5"), 0, "should parse negative decimal with leading dot");
        // Lone dot currently treated as zero by the parser
        assertEquals(0.0, StringUtils.parseDouble("."), 0, "should treat lone decimal point as zero");
        // Large integer value remains finite and comparable to JDK parse
        String big = "9223372036854775807"; // Long.MAX_VALUE as a string
        assertEquals(Double.parseDouble(big), StringUtils.parseDouble(big), 0, "should parse large integer value consistently with JDK parser");
    }

    @Test
    public void testParseInt() {
        assertEquals(6, validate((s, integer) -> (long) StringUtils.parseInt(s, integer)), "should validate parseInt handles all standard parsing scenarios");
    }

    @Test
    public void testParseLong() {
        assertEquals(6, validate(StringUtils::parseLong), "should validate parseLong handles all standard parsing scenarios");
    }

    @Test
    public void reverse() {
        StringBuilder stringBuilder = new StringBuilder("test");
        StringUtils.reverse(stringBuilder, 0);
        assertEquals("tset", stringBuilder.toString(), "should reverse StringBuilder content from specified offset");
    }

    @Test
    public void equalsCaseIgnore_equals() {
        assertTrue(StringUtils.equalsCaseIgnore("aaa", "AAA"), "should match strings differing only in case");
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AAAA"), "should not match strings of different lengths");
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AA_"), "should not match strings with different characters");
    }

    @Test
    public void startsWith_isValidPrefix() {
        assertTrue(StringUtils.startsWith("abcd", "ab"), "should match valid prefix at string start");
        assertFalse(StringUtils.startsWith("abcd", "abe"), "should not match prefix that diverges from string");
    }

    @Test
    public void startsWith_searchStringTooLong() {
        assertFalse(StringUtils.startsWith("a", "ab"), "should not match prefix when search string exceeds target string length");
    }

    @Test
    public void endsWith_isValidSuffix() {
        assertTrue(StringUtils.endsWith("abcd", "cd"), "should match valid suffix at string end");
        assertFalse(StringUtils.endsWith("abcd", "ed"), "should not match suffix that diverges from string");
    }

    @Test
    public void endsWith_searchStringIsTooLong() {
        assertFalse(StringUtils.endsWith("abcd", "aaabcd"), "should not match suffix when search string exceeds target string length");
    }

    @Test
    public void testIsEqual() {

        // The same instances
        StringBuilder emptySb = new StringBuilder();
        assertTrue(StringUtils.isEqual(emptySb, emptySb), "should recognize same instance as equal to itself");

        // Null cases
        assertTrue(StringUtils.isEqual(null, null), "should recognize two nulls as equal");
        assertFalse(StringUtils.isEqual(emptySb, null), "should recognize non-null and null as unequal");
        assertFalse(StringUtils.isEqual(null, emptySb), "should recognize null and non-null as unequal");

        // Different lengths
        assertFalse(StringUtils.isEqual(new StringBuilder(), "a"), "should recognize sequences of different lengths as unequal");

        // Same lengths & ASCII
        assertFalse(StringUtils.isEqual(new StringBuilder().append('a'), "b"), "should recognize different ASCII characters as unequal");
        assertFalse(StringUtils.isEqual(new StringBuilder().append("test"), "Test"), "should recognize case-different ASCII strings as unequal");
        assertTrue(StringUtils.isEqual(new StringBuilder().append("TheSame"), "TheSame"), "should recognize identical ASCII strings as equal");

        // Same lengths & UTF-8
        assertFalse(StringUtils.isEqual(new StringBuilder().append('\u0394'), "\u0393"), "should recognize different Unicode characters as unequal");
        assertFalse(StringUtils.isEqual(new StringBuilder().append("\u0394\u0394\u0394\u0394\u0394"), "\u0394\u0394\u20AC\u0394\u0394"), "should recognize Unicode strings differing in middle as unequal");
        assertTrue(StringUtils.isEqual(new StringBuilder().append("\u0394\u0394\u0394\u0394\u0394"), "\u0394\u0394\u0394\u0394\u0394"), "should recognize identical Unicode strings as equal");

        // Empty strings
        assertTrue(StringUtils.isEqual(new StringBuilder(), ""), "should recognize empty StringBuilder and empty String as equal");
    }
}
