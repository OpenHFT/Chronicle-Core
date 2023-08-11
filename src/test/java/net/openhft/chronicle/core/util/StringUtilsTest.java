/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Maths;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class StringUtilsTest extends CoreTestCommon {
    @Test
    public void testFirstLowerCase() {
        assertEquals("", StringUtils.firstLowerCase(""));
        assertEquals("99", StringUtils.firstLowerCase("99"));
        assertEquals("a", StringUtils.firstLowerCase("A"));
        assertEquals("a", StringUtils.firstLowerCase("a"));
        assertEquals("aA", StringUtils.firstLowerCase("AA"));
        assertEquals("aa", StringUtils.firstLowerCase("Aa"));
    }

    @Test
    public void testToTitleCase() {
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
    public void shouldGetCharsOfStringBuilder() {
        final StringBuilder sb = new StringBuilder(11).append("foobar_nine");
        final char[] chars = StringUtils.extractChars(sb);
        assertEquals(sb.toString(), new String(chars));
    }

    @Test
    public void shouldGetCharsOfString() {
        final String s = "foobar_nine";
        final char[] chars = StringUtils.extractChars(s);
        assertEquals(s, new String(chars));
    }

    @Test
    public void shouldExtractBytesFromString() {
        assumeTrue(Jvm.isJava9Plus());

        assertThat("Is this test running on JDK9 with compact strings disabled?",
                StringUtils.extractBytes("foobar"), is("foobar".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    public void shouldExtractBytesFromStringBuilder() {
        // uses StringUtils.extractBytes/extractChars as appropriate
        assertEquals(0xdf8d42fa7e05af8aL,
                Maths.hash64(new StringBuilder("foobar")));
    }

    @Test
    public void shouldCreateNewStringFromChars() {
        final char[] chars = {'A', 'B', 'C'};
        assertEquals(new String(chars), StringUtils.newString(chars));
    }

    @Test
    public void shouldCreateNewStringFromBytes() {
        assumeTrue(Jvm.isJava9Plus());

        final byte[] bytes = {'A', 'B', 'C'};
        assertEquals(StringUtils.newStringFromBytes(bytes), new String(bytes));
    }

    @Test
    public void testParseDouble() {
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
    public void testParseInt() {
        validate((s, integer) -> (long) StringUtils.parseInt(s, integer));
    }

    @Test
    public void testParseLong() {
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
    public void reverse() {
        StringBuilder stringBuilder = new StringBuilder("test");
        StringUtils.reverse(stringBuilder, 0);
        assertEquals("tset", stringBuilder.toString());
    }

    @Test
    public void equalsCaseIgnore_equals() {
        assertTrue(StringUtils.equalsCaseIgnore("aaa", "AAA"));
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AAAA"));
        assertFalse(StringUtils.equalsCaseIgnore("aaa", "AA_"));
    }

    @Test
    public void startsWith_isValidPrefix() {
        assertTrue(StringUtils.startsWith("abcd", "ab"));
        assertFalse(StringUtils.startsWith("abcd", "abe"));
    }

    @Test
    public void startsWith_searchStringTooLong() {
        assertFalse(StringUtils.startsWith("a", "ab"));
    }

    @Test
    public void endsWith_isValidSuffix() {
        assertTrue(StringUtils.endsWith("abcd", "cd"));
        assertFalse(StringUtils.endsWith("abcd", "ed"));
    }

    @Test
    public void endsWith_searchStringIsTooLong() {
        assertFalse(StringUtils.endsWith("abcd", "aaabcd"));
    }

    @Test
    public void testIsEqual() {

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