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
        assertEquals(100, (long) method.apply("100", 10), "validate: L19");
        assertEquals(-100, (long) method.apply("-100", 10), "validate: L20");
        scenarios += 2;

        // Lone char
        NumberFormatException firstCharEx = assertThrows(NumberFormatException.class, () -> method.apply("+", 10));
        assertEquals("For input string: \"+\"", firstCharEx.getMessage(), "validate: L25");
        scenarios++;

        // Null
        NumberFormatException nullEx = assertThrows(NumberFormatException.class, () -> method.apply(null, 0));
        assertEquals("null", nullEx.getMessage(), "validate: L30");
        scenarios++;

        // Max radix
        NumberFormatException maxRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 37));
        assertEquals("radix 37 greater than Character.MAX_RADIX", maxRadixEx.getMessage(), "validate: L35");
        scenarios++;

        // Min radix
        NumberFormatException minRadixEx = assertThrows(NumberFormatException.class, () -> method.apply("100", 0));
        assertEquals("radix 0 less than Character.MIN_RADIX", minRadixEx.getMessage(), "validate: L40");
        scenarios++;

        return scenarios;
    }

    @Test
    public void testIsEqualWithStringBuilderAndCharSequence() {
        StringBuilder sb = new StringBuilder("test");
        CharSequence cs = "test";

        assertTrue(StringUtils.isEqual(sb, cs), "testIsEqualWithStringBuilderAndCharSequence: L51");
        assertFalse(StringUtils.isEqual(sb, "different"), "testIsEqualWithStringBuilderAndCharSequence: L52");
    }

    @Test
    public void testSetLengthOfStringBuilder() {
        StringBuilder sb = new StringBuilder("test");
        StringUtils.setLength(sb, 2);

        assertEquals("te", sb.toString(), "testSetLengthOfStringBuilder: L60");
    }

    @Test
    public void testSetStringBuilderContent() {
        StringBuilder sb = new StringBuilder("original");
        StringUtils.set(sb, "updated");

        assertEquals("updated", sb.toString(), "testSetStringBuilderContent: L68");
    }

    @Test
    public void testEndsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.endsWith(cs, "String"), "testEndsWith: L74");
        assertTrue(StringUtils.endsWith(cs, "string"), "testEndsWith: L75"); // case-insensitive
    }

    @Test
    public void testStartsWith() {
        CharSequence cs = "testString";
        assertTrue(StringUtils.startsWith(cs, "test"), "testStartsWith: L81");
        assertFalse(StringUtils.startsWith(cs, "String"), "testStartsWith: L82");
    }

    @Test
    public void testIsEqualWithCharSequences() {
        CharSequence cs1 = "test";
        CharSequence cs2 = "test";
        CharSequence cs3 = "different";

        assertTrue(StringUtils.isEqual(cs1, cs2), "testIsEqualWithCharSequences: L91");
        assertFalse(StringUtils.isEqual(cs1, cs3), "testIsEqualWithCharSequences: L92");
    }

    @Test
    public void testEqualsCaseIgnore() {
        CharSequence cs1 = "TestString";
        CharSequence cs2 = "teststring";

        assertTrue(StringUtils.equalsCaseIgnore(cs1, cs2), "testEqualsCaseIgnore: L100");
        assertFalse(StringUtils.equalsCaseIgnore(cs1, "AnotherString"), "testEqualsCaseIgnore: L101");
    }

    @Test
    public void testToStringMethod() {
        Object obj = "test";
        assertNull(StringUtils.toString(null), "testToStringMethod: L107");
        assertEquals("test", StringUtils.toString(obj), "testToStringMethod: L108");
    }

    @Test
    public void testExtractBytesString() {
        String str = "test";
        byte[] expectedBytes = str.getBytes(UTF_8);
        assertArrayEquals(expectedBytes, StringUtils.extractBytes(str), "testExtractBytesString: L115");
    }

    @Test
    public void testNewStringFromChars() {
        char[] chars = {'t', 'e', 's', 't'};
        assertEquals("test", StringUtils.newString(chars), "testNewStringFromChars: L121");
    }

    @Test
    public void testNewStringFromBytes() {
        byte[] bytes = "test".getBytes(UTF_8);
        assertEquals("test", StringUtils.newStringFromBytes(bytes), "testNewStringFromBytes: L127");
    }

    @Test
    public void testFirstLowerCase() {
        assertEquals("", StringUtils.firstLowerCase(""), "testFirstLowerCase: L132");
        assertEquals("99", StringUtils.firstLowerCase("99"), "testFirstLowerCase: L133");
        assertEquals("a", StringUtils.firstLowerCase("A"), "testFirstLowerCase: L134");
        assertEquals("a", StringUtils.firstLowerCase("a"), "testFirstLowerCase: L135");
        assertEquals("aA", StringUtils.firstLowerCase("AA"), "testFirstLowerCase: L136");
        assertEquals("aa", StringUtils.firstLowerCase("Aa"), "testFirstLowerCase: L137");
    }

    @Test
    public void testToTitleCase() {
        assertEquals("", StringUtils.toTitleCase(""), "testToTitleCase: L142");
        assertEquals("99", StringUtils.toTitleCase("99"), "testToTitleCase: L143");
        assertEquals("A", StringUtils.toTitleCase("A"), "testToTitleCase: L144");
        assertEquals("A", StringUtils.toTitleCase("a"), "testToTitleCase: L145");
        assertEquals("AA", StringUtils.toTitleCase("AA"), "testToTitleCase: L146");
        assertEquals("AA", StringUtils.toTitleCase("Aa"), "testToTitleCase: L147");
        assertEquals("AAA", StringUtils.toTitleCase("AAA"), "testToTitleCase: L148");
        assertEquals("AA_A", StringUtils.toTitleCase("AaA"), "testToTitleCase: L149");
        assertEquals("A_AA", StringUtils.toTitleCase("AAa"), "testToTitleCase: L150");
        assertEquals("AAA", StringUtils.toTitleCase("Aaa"), "testToTitleCase: L151");

        assertEquals("AAAA", StringUtils.toTitleCase("AAAA"), "testToTitleCase: L153");
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAA"), "testToTitleCase: L154");
        assertEquals("A_AA_A", StringUtils.toTitleCase("AAaA"), "testToTitleCase: L155");
        assertEquals("AAA_A", StringUtils.toTitleCase("AaaA"), "testToTitleCase: L156");
        assertEquals("AA_AA", StringUtils.toTitleCase("AAAa"), "testToTitleCase: L157");
        assertEquals("AA_AA", StringUtils.toTitleCase("AaAa"), "testToTitleCase: L158");
        assertEquals("A_AAA", StringUtils.toTitleCase("AAaa"), "testToTitleCase: L159");
        assertEquals("AAAA", StringUtils.toTitleCase("Aaaa"), "testToTitleCase: L160");
    }

    @Test
    public void shouldGetCharsOfStringBuilder() {
        final StringBuilder sb = new StringBuilder(11).append("foobar_nine");
        final char[] chars = StringUtils.extractChars(sb);
        assertEquals(sb.toString(), new String(chars), "shouldGetCharsOfStringBuilder: L167");
    }

    @Test
    public void shouldGetCharsOfString() {
        final String s = "foobar_nine";
        final char[] chars = StringUtils.extractChars(s);
        assertEquals(s, new String(chars), "shouldGetCharsOfString: L174");
    }

    @Test
    public void shouldExtractBytesFromString() {
        assertArrayEquals("foobar".getBytes(UTF_8), StringUtils.extractBytes("foobar"), "shouldExtractBytesFromString: L179");
    }

    @Test
    public void shouldExtractBytesFromStringBuilder() {
        // uses StringUtils.extractBytes/extractChars as appropriate
        assertEquals(0xdf8d42fa7e05af8aL, Maths.hash64(new StringBuilder("foobar")), "shouldExtractBytesFromStringBuilder: L185");
    }

    @Test
    public void shouldCreateNewStringFromChars() {
        final char[] chars = {'A', 'B', 'C'};
        assertEquals(new String(chars), StringUtils.newString(chars), "shouldCreateNewStringFromChars: L192");
    }

    @Test
    public void shouldCreateNewStringFromBytes() {
        final byte[] bytes = {'A', 'B', 'C'};
        String expected = new String(bytes, UTF_8);
        String actual = StringUtils.newStringFromBytes(bytes);
        assertEquals(expected, actual, "shouldCreateNewStringFromBytes: L200");
    }

    @Test
    public void testParseDouble() {
        for (double d : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double
                .POSITIVE_INFINITY, 0.0, -1.0, 1.0, 9999.0}) {
            assertEquals(d, StringUtils.parseDouble(Double.toString(d)), 0, "testParseDouble: L207");
        }

        assertEquals(1.0, StringUtils.parseDouble("1"), 0, "testParseDouble: L210");
        assertEquals(0.0, StringUtils.parseDouble("-0"), 0, "testParseDouble: L211");
        assertEquals(123.0, StringUtils.parseDouble("123"), 0, "testParseDouble: L212");
        assertEquals(-1.0, StringUtils.parseDouble("-1"), 0, "testParseDouble: L213");
    }

    @Test
    public void testParseDoubleEdgeCases() {
        // Trailing dot
        assertEquals(123.0, StringUtils.parseDouble("123."), 0, "testParseDoubleEdgeCases: L219");
        // Leading dot
        assertEquals(0.5, StringUtils.parseDouble(".5"), 0, "testParseDoubleEdgeCases: L221");
        assertEquals(-0.5, StringUtils.parseDouble("-.5"), 0, "testParseDoubleEdgeCases: L222");
        // Lone dot currently treated as zero by the parser
        assertEquals(0.0, StringUtils.parseDouble("."), 0, "testParseDoubleEdgeCases: L224");
        // Large integer value remains finite and comparable to JDK parse
        String big = "9223372036854775807"; // Long.MAX_VALUE as a string
        assertEquals(Double.parseDouble(big), StringUtils.parseDouble(big), 0, "testParseDoubleEdgeCases: L227");
    }

    @Test
    public void testParseInt() {
        assertEquals(6, validate((s, integer) -> (long) StringUtils.parseInt(s, integer)), "testParseInt: scenarios");
    }

    @Test
    public void testParseLong() {
        assertEquals(6, validate(StringUtils::parseLong), "testParseLong: scenarios");
    }

    @Test
    public void reverse() {
        StringBuilder stringBuilder = new StringBuilder("test");
        StringUtils.reverse(stringBuilder, 0);
        assertEquals("tset", stringBuilder.toString(), "reverse: L244");
    }

    @Test
    public void equalsCaseIgnore_equals() {
        assertTrue(StringUtils.equalsCaseIgnore("aaa", "AAA"), "equalsCaseIgnore_equals: L249");
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AAAA"), "equalsCaseIgnore_equals: L250");
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AA_"), "equalsCaseIgnore_equals: L251");
    }

    @Test
    public void startsWith_isValidPrefix() {
        assertTrue(StringUtils.startsWith("abcd", "ab"), "startsWith_isValidPrefix: L256");
        assertFalse(StringUtils.startsWith("abcd", "abe"), "startsWith_isValidPrefix: L257");
    }

    @Test
    public void startsWith_searchStringTooLong() {
        assertFalse(StringUtils.startsWith("a", "ab"), "startsWith_searchStringTooLong: L262");
    }

    @Test
    public void endsWith_isValidSuffix() {
        assertTrue(StringUtils.endsWith("abcd", "cd"), "endsWith_isValidSuffix: L267");
        assertFalse(StringUtils.endsWith("abcd", "ed"), "endsWith_isValidSuffix: L268");
    }

    @Test
    public void endsWith_searchStringIsTooLong() {
        assertFalse(StringUtils.endsWith("abcd", "aaabcd"), "endsWith_searchStringIsTooLong: L273");
    }

    @Test
    public void testIsEqual() {

        // The same instances
        StringBuilder emptySb = new StringBuilder();
        assertTrue(StringUtils.isEqual(emptySb, emptySb), "testIsEqual: L281");

        // Null cases
        assertTrue(StringUtils.isEqual(null, null), "testIsEqual: L284");
        assertFalse(StringUtils.isEqual(emptySb, null), "testIsEqual: L285");
        assertFalse(StringUtils.isEqual(null, emptySb), "testIsEqual: L286");

        // Different lengths
        assertFalse(StringUtils.isEqual(new StringBuilder(), "a"), "testIsEqual: L289");

        // Same lengths & ASCII
        assertFalse(StringUtils.isEqual(new StringBuilder().append('a'), "b"), "testIsEqual: L292");
        assertFalse(StringUtils.isEqual(new StringBuilder().append("test"), "Test"), "testIsEqual: L293");
        assertTrue(StringUtils.isEqual(new StringBuilder().append("TheSame"), "TheSame"), "testIsEqual: L294");

        // Same lengths & UTF-8
        assertFalse(StringUtils.isEqual(new StringBuilder().append('\u0394'), "\u0393"), "testIsEqual: L297");
        assertFalse(StringUtils.isEqual(new StringBuilder().append("\u0394\u0394\u0394\u0394\u0394"), "\u0394\u0394\u20AC\u0394\u0394"), "testIsEqual: L298");
        assertTrue(StringUtils.isEqual(new StringBuilder().append("\u0394\u0394\u0394\u0394\u0394"), "\u0394\u0394\u0394\u0394\u0394"), "testIsEqual: L299");

        // Empty strings
        assertTrue(StringUtils.isEqual(new StringBuilder(), ""), "testIsEqual: L302");
    }
}
