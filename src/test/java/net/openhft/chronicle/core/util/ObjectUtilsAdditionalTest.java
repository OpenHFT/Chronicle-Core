/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilsAdditionalTest {

    @Test
    @DisplayName("Boolean text parsing recognises yes and no")
    void booleanParsingAcceptsYesTrueAndNoFalse() {
        assertTrue(ObjectUtils.isTrue("t"), "isTrue should return true for 't'");
        assertTrue(ObjectUtils.isTrue("y"), "isTrue should return true for 'y'");
        assertTrue(ObjectUtils.isTrue("yes"), "isTrue should return true for 'yes'");
        assertTrue(ObjectUtils.isTrue("true"), "isTrue should return true for 'true' string input");
        assertFalse(ObjectUtils.isTrue("foo"), "isTrue should return false for unrecognised string");
        assertFalse(ObjectUtils.isTrue(null), "isTrue should return false for null boolean input value");

        assertTrue(ObjectUtils.isFalse("f"), "isFalse should return true for 'f'");
        assertTrue(ObjectUtils.isFalse("n"), "isFalse should return true for 'n'");
        assertTrue(ObjectUtils.isFalse("no"), "isFalse should return true for 'no' string input");
        assertTrue(ObjectUtils.isFalse("false"), "isFalse should return true for 'false' string input");
        assertFalse(ObjectUtils.isFalse("bar"), "isFalse should return false for unrecognised string");
        assertFalse(ObjectUtils.isFalse(null), "isFalse should return false for null boolean input value");
    }

    @Test
    @DisplayName("Text conversion maps yes and no booleans")
    void convertTextToBoolean() {
        assertEquals(Boolean.TRUE, ObjectUtils.convertTo(Boolean.class, "yes"), "convertTo should convert 'yes' to Boolean.TRUE");
        assertEquals(Boolean.FALSE, ObjectUtils.convertTo(Boolean.class, "no"), "convertTo should convert 'no' to Boolean.FALSE");
    }

    @Test
    @DisplayName("Text conversion uses valueOf parse and constructor")
    void convertTextUsingValueOfParseAndConstructor() {
        Object v1 = ObjectUtils.convertTo(WithValueOf.class, "x1");
        assertInstanceOf(WithValueOf.class, v1, "valueOf-based conversion should return WithValueOf instance");

        Object v2 = ObjectUtils.convertTo(WithParse.class, "x2");
        assertInstanceOf(WithParse.class, v2, "parse-based conversion should return WithParse instance");

        Object v3 = ObjectUtils.convertTo(WithCtor.class, "x3");
        assertInstanceOf(WithCtor.class, v3, "constructor-based conversion should return WithCtor instance");
    }

    @Test
    @DisplayName("List conversion builds object array elements")
    void convertListToObjectArray() {
        List<Object> list = Arrays.asList("a", 1);
        Object[] arr = ObjectUtils.convertTo(Object[].class, list);
        assertArrayEquals(new Object[]{"a", 1}, arr, "arrays should contain identical elements");
    }

    @Test
    @DisplayName("requireNonNull rejects null argument with exception")
    void requireNonNullThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> requireNonNull(null),
                "requireNonNull should throw for null input");
        assertEquals("abc", requireNonNull("abc"), "requireNonNull should return argument when non-null");
    }

    @Test
    @DisplayName("Numeric conversion yields BigDecimal from long")
    void convertNumberToBigDecimalFromNumberPath() {
        BigDecimal bd = (BigDecimal) ObjectUtils.convertToNumber(BigDecimal.class, 5L);
        assertEquals(BigDecimal.valueOf(5L), bd, "convertToNumber should convert Long to BigDecimal");
    }

    @Test
    @DisplayName("convertToNumber rejects unsupported numeric target types")
    void convertToNumberRejectsUnsupportedTypes() {
        assertThrows(UnsupportedOperationException.class,
                () -> ObjectUtils.convertToNumber(StringBuilder.class, 12L),
                "convertToNumber should reject unsupported numeric targets");
    }

    @Test
    @DisplayName("addAll returns new array for empty and populated inputs")
    void addAllHandlesEmptyAndNonEmptyAdditionalArgs() {
        String[] single = ObjectUtils.addAll("alpha");
        assertArrayEquals(new String[]{"alpha"}, single, "addAll should return array containing only the first element");

        String[] combined = ObjectUtils.addAll("alpha", "beta", "gamma");
        assertArrayEquals(new String[]{"alpha", "beta", "gamma"}, combined, "addAll should append additional elements");
    }

    @Test
    @DisplayName("sizeOf supports map and array inputs")
    void sizeOfHandlesMapAndArrayInputs() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        assertEquals(2, ObjectUtils.sizeOf(map), "sizeOf should return map size");

        int[] values = {1, 2, 3};
        assertEquals(3, ObjectUtils.sizeOf(values), "sizeOf should return array length");
    }

    @Test
    @DisplayName("convertTo array path handles object arrays and rejects primitive arrays")
    void convertToArrayHandlesObjectAndRejectsPrimitiveArrays() {
        String[] strings = ObjectUtils.convertTo(String[].class, new Object[]{"a", "b"});
        assertArrayEquals(new String[]{"a", "b"}, strings, "convertTo should map object array elements into target array");

        int[] ints = {1, 2};
        int[] converted = ObjectUtils.convertTo(int[].class, ints);
        assertSame(ints, converted, "convertTo should return primitive arrays unchanged when types already match");
    }

    @Test
    @DisplayName("newInstanceOrNull returns null when constructor cannot be instantiated")
    void newInstanceOrNullReturnsNullOnFailure() {
        assertNull(ObjectUtils.newInstanceOrNull(Runnable.class),
                "newInstanceOrNull should return null for interfaces without defaults");
    }

    @Test
    @DisplayName("isFalse wraps invalid CharSequence access in AssertionError")
    void isFalseWrapsInvalidCharSequenceAccess() {
        CharSequence broken = new CharSequence() {
            @Override
            public int length() {
                return 1;
            }

            @Override
            public char charAt(int index) {
                throw new IndexOutOfBoundsException("charAt index out of bounds for broken CharSequence");
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return "x";
            }
        };
        AssertionError error = assertThrows(AssertionError.class, () -> ObjectUtils.isFalse(broken),
                "isFalse should wrap invalid CharSequence access in AssertionError");
        assertTrue(error.getMessage().contains("Failed to read first character in isFalse"),
                "isFalse should include context when wrapping invalid CharSequence access");
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
