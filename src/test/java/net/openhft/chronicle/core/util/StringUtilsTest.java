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

@SuppressWarnings("deprecation")
class StringUtilsTest extends CoreTestCommon {

    private static int validate(BiFunction<String, Integer, Long> method) {
        int scenarios = 0;
        assertEquals(100, (long) method.apply("100", 10), "parse method should parse positive decimal string to numeric value");
        assertEquals(-100, (long) method.apply("-100", 10), "parse method should parse negative decimal string to numeric value");
        scenarios += 2;

        // Lone char
        NumberFormatException firstCharEx = assertThrows(NumberFormatException.class, () -> method.apply("+", 10),
                "parse should reject lone plus");
        assertEquals("For input string: \"+\"", firstCharEx.getMessage(), "error message should report invalid lone plus sign");
        scenarios++;

        // Null
        NumberFormatException nullEx = assertThrows(NumberFormatException.class, () -> method.apply(null, 0),
                "parse should reject null input");
        assertEquals("input text is null", nullEx.getMessage(), "error message should report null input");
        scenarios++;

        // Max radix
        NumberFormatException maxRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 37),
                "parse should reject radix above max");
        assertEquals("radix 37 greater than Character.MAX_RADIX", maxRadixEx.getMessage(), "error message should report radix exceeding maximum");
        scenarios++;

        // Min radix
        NumberFormatException minRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 0),
                "parse should reject radix below min");
        assertEquals("radix 0 less than Character.MIN_RADIX", minRadixEx.getMessage(), "error message should report radix below minimum");
        scenarios++;

        return scenarios;
    }

    @Test
    void testIsEqualWithStringBuilderAndCharSequence() {
        StringBuilder sb = new StringBuilder("test");
        CharSequence cs = "test";

        assertTrue(StringUtils.isEqual(sb, cs), "isEqual should treat matching StringBuilder and String as equal");
        assertFalse(StringUtils.isEqual(sb, "different"), "isEqual should treat different content as unequal");
    }

    @Test
    void testSetLengthOfStringBuilder() {
        StringBuilder sb = new StringBuilder("test");
        StringUtils.setLength(sb, 2);

        assertEquals("te", sb.toString(), "setLength should truncate StringBuilder to specified length");
    }

    @Test
    void testSetStringBuilderContent() {
        StringBuilder sb = new StringBuilder("original");
        StringUtils.set(sb, "updated");

        assertEquals("updated", sb.toString(), "set should replace StringBuilder content with new string");
    }

    @Test
    void testEndsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.endsWith(cs, "String"), cs + " should end with String");
        assertTrue(StringUtils.endsWith(cs, "string"), cs + " should end with string ignoring case");
    }

    @Test
    void testStartsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.startsWith(cs, "test"), cs + " should start with test");
        assertFalse(StringUtils.startsWith(cs, "String"), cs + " should not start with String");
    }

    @Test
    void testIsEqualWithCharSequences() {
        CharSequence cs1 = "test";
        CharSequence cs2 = "test";
        CharSequence cs3 = "different";

        assertTrue(StringUtils.isEqual(cs1, cs2), "isEqual should treat matching CharSequences as equal");
        assertFalse(StringUtils.isEqual(cs1, cs3), "isEqual should treat differing CharSequences as unequal");
    }

    @Test
    void testEqualsCaseIgnore() {
        CharSequence cs1 = "TestString";
        CharSequence cs2 = "teststring";

        assertTrue(StringUtils.equalsCaseIgnore(cs1, cs2), "equalsCaseIgnore should match strings that differ only in case");
        assertFalse(StringUtils.equalsCaseIgnore(cs1, "AnotherString"), "equalsCaseIgnore should reject strings with different content");
    }

    @Test
    void testToStringMethod() {
        Object obj = "test";
        assertNull(StringUtils.toString(null), "toString should return null for null input");
        assertEquals("test", StringUtils.toString(obj), "toString should return string representation for non-null input");
    }

    @Test
    void testExtractBytesString() {
        String str = "test";
        byte[] expectedBytes = str.getBytes(UTF_8);
        assertArrayEquals(expectedBytes, StringUtils.extractBytes(str), "extractBytes should return UTF-8 bytes for string input");
    }

    @Test
    void testNewStringFromChars() {
        char[] chars = {'t', 'e', 's', 't'};
        assertEquals("test", StringUtils.newString(chars), "newString should create string from character array");
    }

    @Test
    void testNewStringFromBytes() {
        byte[] bytes = "test".getBytes(UTF_8);
        assertEquals("test", StringUtils.newStringFromBytes(bytes), "newStringFromBytes should create string from UTF-8 bytes");
    }

    @Test
    void testFirstLowerCase() {
        assertEquals("", StringUtils.firstLowerCase(""), "firstLowerCase should keep empty string unchanged");
        assertEquals("99", StringUtils.firstLowerCase("99"), "firstLowerCase should keep numeric string unchanged");
        assertEquals("a", StringUtils.firstLowerCase("A"), "firstLowerCase should lowercase single uppercase character");
        assertEquals("a", StringUtils.firstLowerCase("a"), "firstLowerCase should keep single lowercase character");
        assertEquals("aA", StringUtils.firstLowerCase("AA"), "firstLowerCase should lowercase only first character");
        assertEquals("aa", StringUtils.firstLowerCase("Aa"), "firstLowerCase should lowercase only first character when mixed case");
    }

    @Test
    void testToTitleCase() {
        assertEquals("", StringUtils.toTitleCase(""), "toTitleCase should keep empty string unchanged");
        assertEquals("99", StringUtils.toTitleCase("99"), "toTitleCase should keep numeric string unchanged");
        assertEquals("A", StringUtils.toTitleCase("A"), "toTitleCase should keep single uppercase letter");
        assertEquals("A", StringUtils.toTitleCase("a"), "toTitleCase should uppercase single lowercase letter");
        assertEquals("AA", StringUtils.toTitleCase("AA"), "toTitleCase should keep two uppercase letters");
        assertEquals("AA", StringUtils.toTitleCase("Aa"), "toTitleCase should uppercase second letter when first is uppercase");
        assertEquals("AAA", StringUtils.toTitleCase("AAA"), "toTitleCase should keep three uppercase letters");
        assertEquals("AA_A", StringUtils.toTitleCase("AaA"), "toTitleCase should insert underscore before uppercase after lowercase");
        assertEquals("A_AA", StringUtils.toTitleCase("AAa"), "toTitleCase should insert underscore between uppercase run and final lowercase");
        assertEquals("AAA", StringUtils.toTitleCase("Aaa"), "toTitleCase should uppercase all lowercase letters after uppercase first");

        assertEquals("AAAA", StringUtils.toTitleCase("AAAA"), "toTitleCase should keep four uppercase letters");
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAA"), "toTitleCase should insert underscore before uppercase sequence after lowercase");
        assertEquals("A_AA_A", StringUtils.toTitleCase("AAaA"), "toTitleCase should insert underscores around lowercase in mixed case sequence");
        assertEquals("AAA_A", StringUtils.toTitleCase("AaaA"), "toTitleCase should insert underscore before final uppercase after lowercase sequence");
        assertEquals("AA_AA", StringUtils.toTitleCase("AAAa"), "toTitleCase should insert underscore between uppercase run and lowercase-uppercase pair");
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAa"), "toTitleCase should insert underscore between two uppercase-lowercase pairs");
        assertEquals("A_AAA", StringUtils.toTitleCase("AAaa"), "toTitleCase should insert underscore before uppercased lowercase sequence");
        assertEquals("AAAA", StringUtils.toTitleCase("Aaaa"), "toTitleCase should uppercase all lowercase letters in title case word");
    }

    @Test
    void shouldGetCharsOfStringBuilder() {
        final StringBuilder sb = new StringBuilder(11).append("foobar_nine");
        final char[] chars = StringUtils.extractChars(sb);
        assertEquals(sb.toString(), new String(chars), "extractChars should match StringBuilder content");
    }

    @Test
    void shouldGetCharsOfString() {
        final String s = "foobar_nine";
        final char[] chars = StringUtils.extractChars(s);
        assertEquals(s, new String(chars), "extractChars should match String content");
    }

    @Test
    void shouldExtractBytesFromString() {
        assertArrayEquals("foobar".getBytes(UTF_8), StringUtils.extractBytes("foobar"), "extractBytes should return UTF-8 bytes for literal foobar");
    }

    @Test
    void shouldExtractBytesFromStringBuilder() {
        // uses StringUtils.extractBytes/extractChars as appropriate
        assertEquals(0xdf8d42fa7e05af8aL, Maths.hash64(new StringBuilder("foobar")), "hash64 should produce consistent hash from StringBuilder bytes");
    }

    @Test
    void shouldCreateNewStringFromChars() {
        final char[] chars = {'A', 'B', 'C'};
        assertEquals(new String(chars), StringUtils.newString(chars), "newString should match standard String constructor output");
    }

    @Test
    void shouldCreateNewStringFromBytes() {
        final byte[] bytes = {'A', 'B', 'C'};
        String expected = new String(bytes, UTF_8);
        String actual = StringUtils.newStringFromBytes(bytes);
        assertEquals(expected, actual, "newStringFromBytes should match standard UTF-8 String constructor output");
    }

    @Test
    void testParseDouble() {
        for (double d : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double
                .POSITIVE_INFINITY, 0.0, -1.0, 1.0, 9999.0}) {
            assertEquals(d, StringUtils.parseDouble(Double.toString(d)), 0,
                    "parseDouble should round-trip special value " + d + " via string");
        }

        assertEquals(1.0, StringUtils.parseDouble("1"), 0, "parseDouble should parse integer string as double value");
        assertEquals(0.0, StringUtils.parseDouble("-0"), 0, "parseDouble should treat -0 as 0.0");
        assertEquals(123.0, StringUtils.parseDouble("123"), 0, "parseDouble should parse multi-digit integer string");
        assertEquals(-1.0, StringUtils.parseDouble("-1"), 0, "parseDouble should parse negative integer string");
    }

    @Test
    void testParseDoubleEdgeCases() {
        // Trailing dot
        assertEquals(123.0, StringUtils.parseDouble("123."), 0, "parseDouble should parse trailing decimal point");
        // Leading dot
        assertEquals(0.5, StringUtils.parseDouble(".5"), 0, "parseDouble should parse leading dot without integer part");
        assertEquals(-0.5, StringUtils.parseDouble("-.5"), 0, "parseDouble should parse negative leading dot");
        // Lone dot currently treated as zero by the parser
        assertEquals(0.0, StringUtils.parseDouble("."), 0, "parseDouble should treat lone decimal point as zero");
        // Large integer value remains finite and comparable to JDK parse
        String big = "9223372036854775807"; // Long.MAX_VALUE as a string
        assertEquals(Double.parseDouble(big), StringUtils.parseDouble(big), 0, "parseDouble should match JDK for large integers");
    }

    @Test
    void testParseInt() {
        assertEquals(6, validate((s, integer) -> (long) StringUtils.parseInt(s, integer)), "parseInt validation should cover standard parsing scenarios");
    }

    @Test
    void testParseLong() {
        assertEquals(6, validate(StringUtils::parseLong), "parseLong validation should cover standard parsing scenarios");
    }

    @Test
    void reverse() {
        StringBuilder stringBuilder = new StringBuilder("test");
        StringUtils.reverse(stringBuilder, 0);
        assertEquals("tset", stringBuilder.toString(), "reverse should reverse StringBuilder content from offset");
    }

    @Test
    void equalsCaseIgnore_equals() {
        assertTrue(StringUtils.equalsCaseIgnore("aaa", "AAA"), "equalsCaseIgnore should match strings differing only in case");
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AAAA"), "equalsCaseIgnore should reject strings of different lengths");
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AA_"), "equalsCaseIgnore should reject strings with different characters");
    }

    @Test
    void startsWith_isValidPrefix() {
        String value = "abcd";
        assertTrue(StringUtils.startsWith(value, "ab"), value + " should start with \"ab\"");
        assertFalse(StringUtils.startsWith(value, "abe"), value + " should not start with \"abe\"");
    }

    @Test
    void startsWith_searchStringTooLong() {
        String value = "a";
        assertFalse(StringUtils.startsWith(value, "ab"), value + " should not start with \"ab\"");
    }

    @Test
    void endsWith_isValidSuffix() {
        String value = "abcd";
        assertTrue(StringUtils.endsWith(value, "cd"), value + " should end with \"cd\"");
        assertFalse(StringUtils.endsWith(value, "ed"), value + " should not end with \"ed\"");
    }

    @Test
    void endsWith_searchStringIsTooLong() {
        String value = "abcd";
        assertFalse(StringUtils.endsWith(value, "aaabcd"), value + " should not end with \"aaabcd\"");
    }

    @Test
    void testIsEqual() {

        // The same instances
        StringBuilder emptySb = new StringBuilder();
        assertTrue(StringUtils.isEqual(emptySb, emptySb), "isEqual should treat same instance as equal");

        // Null cases
        assertTrue(StringUtils.isEqual(null, null), "isEqual should treat two nulls as equal");
        assertFalse(StringUtils.isEqual(emptySb, null), "isEqual should treat non-null and null as unequal");
        assertFalse(StringUtils.isEqual(null, emptySb), "isEqual should treat null and non-null as unequal");

        // Different lengths
        assertFalse(StringUtils.isEqual(new StringBuilder(), "a"), "isEqual should treat sequences of different lengths as unequal");

        // Same lengths & ASCII
        assertFalse(StringUtils.isEqual(new StringBuilder().append('a'), "b"), "isEqual should treat different ASCII characters as unequal");
        assertFalse(StringUtils.isEqual(new StringBuilder().append("test"), "Test"), "isEqual should treat case-different ASCII strings as unequal");
        assertTrue(StringUtils.isEqual(new StringBuilder().append("TheSame"), "TheSame"), "isEqual should treat identical ASCII strings as equal");

        // Same lengths & UTF-8
        assertFalse(StringUtils.isEqual(new StringBuilder().append('\u0394'), "\u0393"), "isEqual should treat different Unicode characters as unequal");
        assertFalse(StringUtils.isEqual(new StringBuilder().append("\u0394\u0394\u0394\u0394\u0394"), "\u0394\u0394\u20AC\u0394\u0394"), "isEqual should treat Unicode strings differing in middle as unequal");
        assertTrue(StringUtils.isEqual(new StringBuilder().append("\u0394\u0394\u0394\u0394\u0394"), "\u0394\u0394\u0394\u0394\u0394"), "isEqual should treat identical Unicode strings as equal");

        // Empty strings
        assertTrue(StringUtils.isEqual(new StringBuilder(), ""), "isEqual should treat empty StringBuilder and empty String as equal");
    }
}
