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

    // --- Helpers used by conversion tests ---
    public static final class WithCtor {
        final String v; public WithCtor(String v) { this.v = v; }
    }
    public static final class WithValueOf { final String v; private WithValueOf(String v){this.v=v;} public static WithValueOf valueOf(String s){return new WithValueOf(s);} }
    public static final class WithParse { final String v; private WithParse(String v){this.v=v;} public static WithParse parse(CharSequence s){return new WithParse(s.toString());} }

    @Test
    public void booleanParsingAcceptsYesTrueAndNoFalse() {
        assertTrue(ObjectUtils.isTrue("t"));
        assertTrue(ObjectUtils.isTrue("y"));
        assertTrue(ObjectUtils.isTrue("yes"));
        assertTrue(ObjectUtils.isTrue("true"));
        assertFalse(ObjectUtils.isTrue("foo"));
        assertFalse(ObjectUtils.isTrue(null));

        assertTrue(ObjectUtils.isFalse("f"));
        assertTrue(ObjectUtils.isFalse("n"));
        assertTrue(ObjectUtils.isFalse("no"));
        assertTrue(ObjectUtils.isFalse("false"));
        assertFalse(ObjectUtils.isFalse("bar"));
        assertFalse(ObjectUtils.isFalse(null));
    }

    @Test
    public void convertTextToBoolean() {
        assertEquals(Boolean.TRUE, ObjectUtils.convertTo(Boolean.class, "yes"));
        assertEquals(Boolean.FALSE, ObjectUtils.convertTo(Boolean.class, "no"));
    }

    @Test
    public void convertTextUsingValueOfParseAndConstructor() {
        Object v1 = ObjectUtils.convertTo(WithValueOf.class, "x1");
        assertTrue(v1 instanceof WithValueOf);

        Object v2 = ObjectUtils.convertTo(WithParse.class, "x2");
        assertTrue(v2 instanceof WithParse);

        Object v3 = ObjectUtils.convertTo(WithCtor.class, "x3");
        assertTrue(v3 instanceof WithCtor);
    }

    @Test
    public void convertListToObjectArray() {
        List<Object> list = Arrays.asList("a", 1);
        Object[] arr = ObjectUtils.convertTo(Object[].class, list);
        assertArrayEquals(new Object[]{"a", 1}, arr);
    }

    @Test
    public void requireNonNullThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> ObjectUtils.<Object>requireNonNull(null));
        assertEquals("abc", ObjectUtils.requireNonNull("abc"));
    }

    @Test
    public void convertNumberToBigDecimalFromNumberPath() {
        BigDecimal bd = (BigDecimal) ObjectUtils.convertToNumber(BigDecimal.class, 5L);
        assertEquals(BigDecimal.valueOf(5L), bd);
    }
}

