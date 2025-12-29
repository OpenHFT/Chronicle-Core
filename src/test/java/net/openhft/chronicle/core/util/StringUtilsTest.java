/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.junit.jupiter.api.DisplayName;
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
                "parse method should throw for lone plus sign");
        assertEquals("For input string: \"+\"", firstCharEx.getMessage(),
                "parse method should report invalid lone plus sign in error message");
        scenarios++;

        // Null
        NumberFormatException nullEx = assertThrows(NumberFormatException.class, () -> method.apply(null, 0),
                "parse method should throw for null input");
        String nullMessage = nullEx.getMessage();
        assertTrue(nullMessage.contains("Input sequence is null"),
                "parse method should report null input in error message: " + nullMessage);
        scenarios++;

        // Max radix
        NumberFormatException maxRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 37),
                "parse method should throw for radix above maximum");
        assertEquals("radix 37 greater than Character.MAX_RADIX", maxRadixEx.getMessage(),
                "parse method should report radix exceeding maximum in error message");
        scenarios++;

        // Min radix
        NumberFormatException minRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 0),
                "parse method should throw for radix below minimum");
        assertEquals("radix 0 less than Character.MIN_RADIX", minRadixEx.getMessage(),
                "parse method should report radix below minimum in error message");
        scenarios++;

        return scenarios;
    }

    @DisplayName("StringBuilder and CharSequence equality matches content")
    @Test
    void testIsEqualWithStringBuilderAndCharSequence() {
        StringBuilder sb = new StringBuilder("test");
        CharSequence cs = "test";

        assertTrue(StringUtils.isEqual(sb, cs), "StringUtils.isEqual should recognise identical StringBuilder and String content");
        assertFalse(StringUtils.isEqual(sb, "different"), "StringUtils.isEqual should recognise differing StringBuilder and String content as unequal");
    }

    @DisplayName("setLength truncates StringBuilder content correctly in place")
    @Test
    void testSetLengthOfStringBuilder() {
        StringBuilder sb = new StringBuilder("test");
        StringUtils.setLength(sb, 2);

        assertEquals("te", sb.toString(), "StringUtils.setLength should truncate StringBuilder to specified length");
    }

    @DisplayName("set replaces StringBuilder content with new text")
    @Test
    void testSetStringBuilderContent() {
        StringBuilder sb = new StringBuilder("original");
        StringUtils.set(sb, "updated");

        assertEquals("updated", sb.toString(), "StringUtils.set should replace StringBuilder content with new string");
    }

    @DisplayName("endsWith matches expected suffixes for String values")
    @Test
    void testEndsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.endsWith(cs, "String"), "endsWith should match uppercase suffix \"String\" for: " + cs);
        assertTrue(StringUtils.endsWith(cs, "string"), "endsWith should match lowercase suffix \"string\" for: " + cs);
    }

    @DisplayName("startsWith matches expected prefixes for String values")
    @Test
    void testStartsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.startsWith(cs, "test"), "startsWith should match prefix \"test\" for: " + cs);
        assertFalse(StringUtils.startsWith(cs, "String"), "startsWith should not match prefix \"String\" for: " + cs);
    }

    @DisplayName("CharSequence equality compares content correctly for inputs")
    @Test
    void testIsEqualWithCharSequences() {
        CharSequence cs1 = "test";
        CharSequence cs2 = "test";
        CharSequence cs3 = "different";

        assertTrue(StringUtils.isEqual(cs1, cs2), "StringUtils.isEqual should recognise identical CharSequence content");
        assertFalse(StringUtils.isEqual(cs1, cs3), "StringUtils.isEqual should recognise differing CharSequence content as unequal");
    }

    @DisplayName("equalsCaseIgnore matches strings ignoring case correctly")
    @Test
    void testEqualsCaseIgnore() {
        CharSequence cs1 = "TestString";
        CharSequence cs2 = "teststring";

        assertTrue(StringUtils.equalsCaseIgnore(cs1, cs2), "equalsCaseIgnore should match strings that differ only in case");
        assertFalse(StringUtils.equalsCaseIgnore(cs1, "AnotherString"), "equalsCaseIgnore should not match strings with different content");
    }

    @DisplayName("toString handles null and non null objects behaviour under expected input and output conditions")
    @Test
    void testToStringMethod() {
        Object obj = "test";
        assertNull(StringUtils.toString(null), "StringUtils.toString should return null for null object");
        assertEquals("test", StringUtils.toString(obj), "StringUtils.toString should convert non-null object to string");
    }

    @DisplayName("extractBytes returns UTF 8 bytes for String input")
    @Test
    void testExtractBytesString() {
        String str = "test";
        byte[] expectedBytes = str.getBytes(UTF_8);
        assertArrayEquals(expectedBytes, StringUtils.extractBytes(str), "StringUtils.extractBytes should return UTF-8 bytes from string");
    }

    @DisplayName("newString builds String from char array")
    @Test
    void testNewStringFromChars() {
        char[] chars = {'t', 'e', 's', 't'};
        assertEquals("test", StringUtils.newString(chars), "StringUtils.newString should create string from character array");
    }

    @DisplayName("newStringFromBytes builds String from UTF 8 bytes")
    @Test
    void testNewStringFromBytes() {
        byte[] bytes = "test".getBytes(UTF_8);
        assertEquals("test", StringUtils.newStringFromBytes(bytes), "StringUtils.newStringFromBytes should create string from UTF-8 byte array");
    }

    @DisplayName("firstLowerCase lowercases only the first character behaviour under expected input and output conditions")
    @Test
    void testFirstLowerCase() {
        assertEquals("", StringUtils.firstLowerCase(""), "firstLowerCase should leave empty string unchanged");
        assertEquals("99", StringUtils.firstLowerCase("99"), "firstLowerCase should leave numeric string unchanged");
        assertEquals("a", StringUtils.firstLowerCase("A"), "firstLowerCase should lowercase a single uppercase character");
        assertEquals("a", StringUtils.firstLowerCase("a"), "firstLowerCase should leave a single lowercase character unchanged");
        assertEquals("aA", StringUtils.firstLowerCase("AA"), "firstLowerCase should lowercase only the first character in uppercase sequence");
        assertEquals("aa", StringUtils.firstLowerCase("Aa"), "firstLowerCase should lowercase the first character in mixed case");
    }

    @DisplayName("toTitleCase formats mixed case strings correctly")
    @Test
    void testToTitleCase() {
        assertEquals("", StringUtils.toTitleCase(""), "toTitleCase should return empty string unchanged");
        assertEquals("99", StringUtils.toTitleCase("99"), "toTitleCase should return numeric string unchanged");
        assertEquals("A", StringUtils.toTitleCase("A"), "toTitleCase should return single uppercase letter unchanged");
        assertEquals("A", StringUtils.toTitleCase("a"), "toTitleCase should convert single lowercase letter to uppercase");
        assertEquals("AA", StringUtils.toTitleCase("AA"), "toTitleCase should return two uppercase letters unchanged");
        assertEquals("AA", StringUtils.toTitleCase("Aa"), "toTitleCase should uppercase second letter when first is uppercase");
        assertEquals("AAA", StringUtils.toTitleCase("AAA"), "toTitleCase should return three uppercase letters unchanged");
        assertEquals("AA_A", StringUtils.toTitleCase("AaA"), "toTitleCase should insert underscore before uppercase after lowercase");
        assertEquals("A_AA", StringUtils.toTitleCase("AAa"), "toTitleCase should insert underscore between uppercase run and final lowercase");
        assertEquals("AAA", StringUtils.toTitleCase("Aaa"), "toTitleCase should uppercase all lowercase letters after uppercase first");

        assertEquals("AAAA", StringUtils.toTitleCase("AAAA"), "toTitleCase should return four uppercase letters unchanged");
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAA"), "toTitleCase should insert underscore before uppercase sequence after lowercase");
        assertEquals("A_AA_A", StringUtils.toTitleCase("AAaA"), "toTitleCase should insert underscores around lowercase in mixed case sequence");
        assertEquals("AAA_A", StringUtils.toTitleCase("AaaA"), "toTitleCase should insert underscore before final uppercase after lowercase sequence");
        assertEquals("AA_AA", StringUtils.toTitleCase("AAAa"), "toTitleCase should insert underscore between uppercase run and lowercase-uppercase pair");
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAa"), "toTitleCase should insert underscore between two uppercase-lowercase pairs");
        assertEquals("A_AAA", StringUtils.toTitleCase("AAaa"), "toTitleCase should insert underscore before uppercased lowercase sequence");
        assertEquals("AAAA", StringUtils.toTitleCase("Aaaa"), "toTitleCase should uppercase all lowercase letters in title case word");
    }

    @DisplayName("extractChars returns characters from StringBuilder content")
    @Test
    void shouldGetCharsOfStringBuilder() {
        final StringBuilder sb = new StringBuilder(11).append("foobar_nine");
        final char[] chars = StringUtils.extractChars(sb);
        assertEquals(sb.toString(), new String(chars), "extractChars should return character array matching StringBuilder content");
    }

    @DisplayName("extractChars returns characters from String content")
    @Test
    void shouldGetCharsOfString() {
        final String s = "foobar_nine";
        final char[] chars = StringUtils.extractChars(s);
        assertEquals(s, new String(chars), "extractChars should return character array matching String content");
    }

    @DisplayName("extractBytes returns UTF 8 bytes from String")
    @Test
    void shouldExtractBytesFromString() {
        assertArrayEquals("foobar".getBytes(UTF_8), StringUtils.extractBytes("foobar"), "extractBytes should return UTF-8 bytes matching String encoding");
    }

    @DisplayName("hash64 uses bytes from StringBuilder content")
    @Test
    void shouldExtractBytesFromStringBuilder() {
        // uses StringUtils.extractBytes/extractChars as appropriate
        assertEquals(0xdf8d42fa7e05af8aL, Maths.hash64(new StringBuilder("foobar")), "hash64 should produce consistent hash from StringBuilder bytes");
    }

    @DisplayName("newString creates String from char array")
    @Test
    void shouldCreateNewStringFromChars() {
        final char[] chars = {'A', 'B', 'C'};
        assertEquals(new String(chars), StringUtils.newString(chars), "newString should create string equivalent to standard String constructor");
    }

    @DisplayName("newStringFromBytes creates String from byte array")
    @Test
    void shouldCreateNewStringFromBytes() {
        final byte[] bytes = {'A', 'B', 'C'};
        String expected = new String(bytes, UTF_8);
        String actual = StringUtils.newStringFromBytes(bytes);
        assertEquals(expected, actual, "newStringFromBytes should create string equivalent to standard UTF-8 String constructor");
    }

    @DisplayName("parseDouble handles standard double formats correctly")
    @Test
    void testParseDouble() {
        for (double d : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double
                .POSITIVE_INFINITY, 0.0, -1.0, 1.0, 9999.0}) {
            assertEquals(d, StringUtils.parseDouble(Double.toString(d)), 0,
                    "parseDouble should round-trip special values through string representation d=" + d);
        }

        assertEquals(1.0, StringUtils.parseDouble("1"), 0, "parseDouble should parse integer string as double value");
        assertEquals(0.0, StringUtils.parseDouble("-0"), 0, "parseDouble should parse negative zero string as positive zero");
        assertEquals(123.0, StringUtils.parseDouble("123"), 0, "parseDouble should parse multi-digit integer string as double");
        assertEquals(-1.0, StringUtils.parseDouble("-1"), 0, "parseDouble should parse negative integer string as double");
    }

    @DisplayName("parseDouble handles edge case strings correctly")
    @Test
    void testParseDoubleEdgeCases() {
        // Trailing dot
        assertEquals(123.0, StringUtils.parseDouble("123."), 0, "parseDouble should parse number with trailing decimal point");
        // Leading dot
        assertEquals(0.5, StringUtils.parseDouble(".5"), 0, "parseDouble should parse decimal with leading dot and no integer part");
        assertEquals(-0.5, StringUtils.parseDouble("-.5"), 0, "parseDouble should parse negative decimal with leading dot");
        // Lone dot currently treated as zero by the parser
        assertEquals(0.0, StringUtils.parseDouble("."), 0, "parseDouble should treat lone decimal point as zero");
        // Large integer value remains finite and comparable to JDK parse
        String big = "9223372036854775807"; // Long.MAX_VALUE as a string
        assertEquals(Double.parseDouble(big), StringUtils.parseDouble(big), 0,
                "parseDouble should parse large integer value consistently with JDK parser");
    }

    @DisplayName("parseInt handles standard numeric scenarios correctly")
    @Test
    void testParseInt() {
        assertEquals(6, validate((s, integer) -> (long) StringUtils.parseInt(s, integer)),
                "validate should confirm parseInt handles all standard parsing scenarios");
    }

    @DisplayName("parseLong handles standard numeric scenarios correctly")
    @Test
    void testParseLong() {
        assertEquals(6, validate(StringUtils::parseLong),
                "validate should confirm parseLong handles all standard parsing scenarios");
    }

    @DisplayName("reverse reverses StringBuilder content from offset")
    @Test
    void reverse() {
        StringBuilder stringBuilder = new StringBuilder("test");
        StringUtils.reverse(stringBuilder, 0);
        assertEquals("tset", stringBuilder.toString(), "StringUtils.reverse should reverse StringBuilder content from specified offset");
    }

    @DisplayName("equalsCaseIgnore compares case insensitive strings correctly")
    @Test
    void equalsCaseIgnore_equals() {
        assertTrue(StringUtils.equalsCaseIgnore("aaa", "AAA"), "equalsCaseIgnore should match strings differing only in case");
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AAAA"), "equalsCaseIgnore should not match strings of different lengths");
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AA_"), "equalsCaseIgnore should not match strings with different characters");
    }

    @DisplayName("startsWith matches valid prefixes for inputs")
    @Test
    void startsWith_isValidPrefix() {
        String input = "abcd";
        String prefix = "ab";
        String wrongPrefix = "abe";
        assertTrue(StringUtils.startsWith(input, prefix),
                "startsWith should match prefix \"" + prefix + "\" for input=" + input);
        assertFalse(StringUtils.startsWith(input, wrongPrefix),
                "startsWith should not match mismatched prefix \"" + wrongPrefix + "\" for input=" + input);
    }

    @DisplayName("startsWith rejects longer search string inputs")
    @Test
    void startsWith_searchStringTooLong() {
        String input = "a";
        String prefix = "ab";
        assertFalse(StringUtils.startsWith(input, prefix),
                "startsWith should not match overlong prefix \"" + prefix + "\" for input=" + input);
    }

    @DisplayName("endsWith matches valid suffixes for inputs")
    @Test
    void endsWith_isValidSuffix() {
        String input = "abcd";
        String suffix = "cd";
        String wrongSuffix = "ed";
        assertTrue(StringUtils.endsWith(input, suffix),
                "endsWith should match suffix \"" + suffix + "\" for input=" + input);
        assertFalse(StringUtils.endsWith(input, wrongSuffix),
                "endsWith should not match mismatched suffix \"" + wrongSuffix + "\" for input=" + input);
    }

    @DisplayName("endsWith rejects longer search string inputs")
    @Test
    void endsWith_searchStringIsTooLong() {
        String input = "abcd";
        String suffix = "aaabcd";
        assertFalse(StringUtils.endsWith(input, suffix),
                "endsWith should not match overlong suffix \"" + suffix + "\" for input=" + input);
    }

    @DisplayName("isEqual compares StringBuilder and String correctly")
    @Test
    void testIsEqual() {

        // The same instances
        StringBuilder emptySb = new StringBuilder();
        assertTrue(StringUtils.isEqual(emptySb, emptySb), "StringUtils.isEqual should recognise the same instance as equal");

        // Null cases
        assertTrue(StringUtils.isEqual(null, null), "StringUtils.isEqual should recognise two nulls as equal");
        assertFalse(StringUtils.isEqual(emptySb, null), "StringUtils.isEqual should recognise non-null and null as unequal");
        assertFalse(StringUtils.isEqual(null, emptySb), "StringUtils.isEqual should recognise null and non-null as unequal");

        // Different lengths
        assertFalse(StringUtils.isEqual(new StringBuilder(), "a"), "StringUtils.isEqual should recognise different length sequences as unequal");

        // Same lengths & ASCII
        assertFalse(StringUtils.isEqual(new StringBuilder().append('a'), "b"), "StringUtils.isEqual should recognise different ASCII characters as unequal");
        assertFalse(StringUtils.isEqual(new StringBuilder().append("test"), "Test"), "StringUtils.isEqual should recognise case-different ASCII strings as unequal");
        assertTrue(StringUtils.isEqual(new StringBuilder().append("TheSame"), "TheSame"),
                "StringUtils.isEqual should recognise identical ASCII strings as equal");

        // Same lengths & UTF-8
        assertFalse(StringUtils.isEqual(new StringBuilder().append('\u0394'), "\u0393"), "StringUtils.isEqual should recognise different Unicode characters as unequal");
        assertFalse(StringUtils.isEqual(new StringBuilder().append("\u0394\u0394\u0394\u0394\u0394"), "\u0394\u0394\u20AC\u0394\u0394"),
                "StringUtils.isEqual should recognise Unicode strings differing in middle as unequal");
        assertTrue(StringUtils.isEqual(new StringBuilder().append("\u0394\u0394\u0394\u0394\u0394"), "\u0394\u0394\u0394\u0394\u0394"),
                "StringUtils.isEqual should recognise identical Unicode strings as equal");

        // Empty strings
        assertTrue(StringUtils.isEqual(new StringBuilder(), ""), "StringUtils.isEqual should recognise empty StringBuilder and empty String as equal");
    }
}
