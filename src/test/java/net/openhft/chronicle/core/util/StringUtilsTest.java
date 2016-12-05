/*
 * Copyright 2016 higherfrequencytrading.com
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

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Rob Austin.
 */
public class StringUtilsTest extends TestCase {
    public void testFirstLowerCase() throws Exception {
        assertEquals("", StringUtils.firstLowerCase(""));
        assertEquals("99", StringUtils.firstLowerCase("99"));
        assertEquals("a", StringUtils.firstLowerCase("A"));
        assertEquals("a", StringUtils.firstLowerCase("a"));
        assertEquals("aA", StringUtils.firstLowerCase("AA"));
        assertEquals("aa", StringUtils.firstLowerCase("Aa"));
    }

    public void testToTitleCase() throws Exception {
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
    public void testParseDouble() throws IOException {
        for (double d : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double
                .POSITIVE_INFINITY, 0.0, -1.0, 1.0, 9999.0}) {
            assertEquals(d, StringUtils.parseDouble(Double.toString(d)), 0);
        }

        assertEquals(1.0, StringUtils.parseDouble("1"), 0);
        assertEquals(0.0, StringUtils.parseDouble("-0"), 0);
        assertEquals(123.0, StringUtils.parseDouble("123"), 0);
        assertEquals(-1.0, StringUtils.parseDouble("-1"), 0);
    }
}