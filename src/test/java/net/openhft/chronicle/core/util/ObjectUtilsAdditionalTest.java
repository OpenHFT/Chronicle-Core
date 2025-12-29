/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilsAdditionalTest {

    @DisplayName("booleanParsingAcceptsYesTrueAndNoFalse behaviour under expected input and output conditions")
    @Test
    void booleanParsingAcceptsYesTrueAndNoFalse() {
        assertTrue(ObjectUtils.isTrue("t"), "isTrue should return true for 't'");
        assertTrue(ObjectUtils.isTrue("y"), "isTrue should return true for 'y'");
        assertTrue(ObjectUtils.isTrue("yes"), "isTrue should return true for 'yes'");
        assertTrue(ObjectUtils.isTrue("true"), "isTrue should return true for 'true' string input");
        assertFalse(ObjectUtils.isTrue("foo"), "isTrue should return false for unrecognised string");
        assertFalse(ObjectUtils.isTrue(null), "isTrue should return false for null input value");

        assertTrue(ObjectUtils.isFalse("f"), "isFalse should return true for 'f'");
        assertTrue(ObjectUtils.isFalse("n"), "isFalse should return true for 'n'");
        assertTrue(ObjectUtils.isFalse("no"), "isFalse should return true for 'no' string input");
        assertTrue(ObjectUtils.isFalse("false"), "isFalse should return true for 'false' string input");
        assertFalse(ObjectUtils.isFalse("bar"), "isFalse should return false for unrecognised string");
        assertFalse(ObjectUtils.isFalse(null), "isFalse should return false for null input value");
    }

    @DisplayName("convertTextToBoolean behaviour under expected input and output conditions")
    @Test
    void convertTextToBoolean() {
        assertEquals(Boolean.TRUE, ObjectUtils.convertTo(Boolean.class, "yes"), "convertTo should convert 'yes' to Boolean.TRUE");
        assertEquals(Boolean.FALSE, ObjectUtils.convertTo(Boolean.class, "no"), "convertTo should convert 'no' to Boolean.FALSE");
    }

    @DisplayName("convertTextUsingValueOfParseAndConstructor behaviour under expected input and output conditions")
    @Test
    void convertTextUsingValueOfParseAndConstructor() {
        Object v1 = ObjectUtils.convertTo(WithValueOf.class, "x1");
        assertInstanceOf(WithValueOf.class, v1, "valueOf-based conversion should return WithValueOf instance");

        Object v2 = ObjectUtils.convertTo(WithParse.class, "x2");
        assertInstanceOf(WithParse.class, v2, "parse-based conversion should return WithParse instance");

        Object v3 = ObjectUtils.convertTo(WithCtor.class, "x3");
        assertInstanceOf(WithCtor.class, v3, "constructor-based conversion should return WithCtor instance");
    }

    @DisplayName("convertListToObjectArray behaviour under expected input and output conditions")
    @Test
    void convertListToObjectArray() {
        List<Object> list = Arrays.asList("a", 1);
        Object[] arr = ObjectUtils.convertTo(Object[].class, list);
        assertArrayEquals(new Object[]{"a", 1}, arr, "arrays should contain identical elements");
    }

    @DisplayName("requireNonNullThrowsOnNull behaviour under expected input and output conditions")
    @Test
    void requireNonNullThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> ObjectUtils.requireNonNull(null),
                "requireNonNull should throw for null input");
        assertEquals("abc", ObjectUtils.requireNonNull("abc"), "requireNonNull should return input when non-null");
    }

    @DisplayName("convertNumberToBigDecimalFromNumberPath behaviour under expected input and output conditions")
    @Test
    void convertNumberToBigDecimalFromNumberPath() {
        BigDecimal bd = (BigDecimal) ObjectUtils.convertToNumber(BigDecimal.class, 5L);
        assertEquals(BigDecimal.valueOf(5L), bd, "convertToNumber should convert Long to BigDecimal");
    }

    // --- Helpers used by conversion tests ---
    public static final class WithCtor {
        final String v;

        public WithCtor(String v) {
            this.v = v;
        }
    }

    public static final class WithValueOf {
        final String v;

        private WithValueOf(String v) {
            this.v = v;
        }

        public static WithValueOf valueOf(String s) {
            return new WithValueOf(s);
        }
    }

    public static final class WithParse {
        final String v;

        private WithParse(String v) {
            this.v = v;
        }

        public static WithParse parse(CharSequence s) {
            return new WithParse(s.toString());
        }
    }
}
