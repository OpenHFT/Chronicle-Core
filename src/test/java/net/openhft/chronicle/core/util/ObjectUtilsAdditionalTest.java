/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectUtilsAdditionalTest {

    @Test
    public void booleanParsingAcceptsYesTrueAndNoFalse() {
        assertTrue(ObjectUtils.isTrue("t"), "booleanParsingAcceptsYesTrueAndNoFalse: L18");
        assertTrue(ObjectUtils.isTrue("y"), "booleanParsingAcceptsYesTrueAndNoFalse: L19");
        assertTrue(ObjectUtils.isTrue("yes"), "booleanParsingAcceptsYesTrueAndNoFalse: L20");
        assertTrue(ObjectUtils.isTrue("true"), "booleanParsingAcceptsYesTrueAndNoFalse: L21");
        assertFalse(ObjectUtils.isTrue("foo"), "booleanParsingAcceptsYesTrueAndNoFalse: L22");
        assertFalse(ObjectUtils.isTrue(null), "booleanParsingAcceptsYesTrueAndNoFalse: L23");

        assertTrue(ObjectUtils.isFalse("f"), "booleanParsingAcceptsYesTrueAndNoFalse: L25");
        assertTrue(ObjectUtils.isFalse("n"), "booleanParsingAcceptsYesTrueAndNoFalse: L26");
        assertTrue(ObjectUtils.isFalse("no"), "booleanParsingAcceptsYesTrueAndNoFalse: L27");
        assertTrue(ObjectUtils.isFalse("false"), "booleanParsingAcceptsYesTrueAndNoFalse: L28");
        assertFalse(ObjectUtils.isFalse("bar"), "booleanParsingAcceptsYesTrueAndNoFalse: L29");
        assertFalse(ObjectUtils.isFalse(null), "booleanParsingAcceptsYesTrueAndNoFalse: L30");
    }

    @Test
    public void convertTextToBoolean() {
        assertEquals(Boolean.TRUE, ObjectUtils.convertTo(Boolean.class, "yes"), "convertTextToBoolean: L35");
        assertEquals(Boolean.FALSE, ObjectUtils.convertTo(Boolean.class, "no"), "convertTextToBoolean: L36");
    }

    @Test
    public void convertTextUsingValueOfParseAndConstructor() {
        Object v1 = ObjectUtils.convertTo(WithValueOf.class, "x1");
        assertTrue(v1 instanceof WithValueOf, "convertTextUsingValueOfParseAndConstructor: L42");

        Object v2 = ObjectUtils.convertTo(WithParse.class, "x2");
        assertTrue(v2 instanceof WithParse, "convertTextUsingValueOfParseAndConstructor: L45");

        Object v3 = ObjectUtils.convertTo(WithCtor.class, "x3");
        assertTrue(v3 instanceof WithCtor, "convertTextUsingValueOfParseAndConstructor: L48");
    }

    @Test
    public void convertListToObjectArray() {
        List<Object> list = Arrays.asList("a", 1);
        Object[] arr = ObjectUtils.convertTo(Object[].class, list);
        assertArrayEquals(new Object[]{"a", 1}, arr, "convertListToObjectArray: L55");
    }

    @Test
    public void requireNonNullThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> ObjectUtils.requireNonNull(null));
        assertEquals("abc", ObjectUtils.requireNonNull("abc"), "requireNonNullThrowsOnNull: L61");
    }

    @Test
    public void convertNumberToBigDecimalFromNumberPath() {
        BigDecimal bd = (BigDecimal) ObjectUtils.convertToNumber(BigDecimal.class, 5L);
        assertEquals(BigDecimal.valueOf(5L), bd, "convertNumberToBigDecimalFromNumberPath: L67");
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
