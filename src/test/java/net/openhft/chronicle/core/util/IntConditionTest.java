/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

import static net.openhft.chronicle.core.internal.invariant.ints.IntCondition.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class IntConditionTest extends CoreTestCommon {

    @Test
    public void positive() {
        assertEquals(3, test(
                POSITIVE, NON_POSITIVE,
                entry(-1, false),
                entry(0, false),
                entry(1, true)
        ), "positive: scenarios");
    }

    @Test
    public void negative() {
        assertEquals(3, test(
                NEGATIVE, NON_NEGATIVE,
                entry(-1, true),
                entry(0, false),
                entry(1, false)
        ), "negative: scenarios");
    }

    @Test
    public void zero() {
        assertEquals(3, test(
                ZERO, NON_ZERO,
                entry(-1, false),
                entry(0, true),
                entry(1, false)
        ), "zero: scenarios");
    }

    @Test
    public void byteConvertible() {
        assertEquals(5, test(
                BYTE_CONVERTIBLE,
                entry(Byte.MIN_VALUE - 1, false),
                entry(Byte.MIN_VALUE, true),
                entry(0, true),
                entry(Byte.MAX_VALUE, true),
                entry(Byte.MAX_VALUE + 1, false)
        ), "byteConvertible: scenarios");
    }

    @Test
    public void shortConvertible() {
        assertEquals(5, test(
                SHORT_CONVERTIBLE,
                entry(Short.MIN_VALUE - 1, false),
                entry(Short.MIN_VALUE, true),
                entry(0, true),
                entry(Short.MAX_VALUE, true),
                entry(Short.MAX_VALUE + 1, false)
        ), "shortConvertible: scenarios");
    }

    @Test
    public void evenPowerOfTwo() {
        assertEquals(7, test(
                EVEN_POWER_OF_TWO,
                entry(0, false),
                entry(1, true),
                entry(2, true),
                entry(4, true),
                entry(Integer.MAX_VALUE, false),
                entry(Integer.MIN_VALUE, false),
                entry(-2, false)
        ), "evenPowerOfTwo: scenarios");
    }

    @SafeVarargs
    private final int test(IntCondition predicate,
                           IntCondition negatedPredicate,
                           Map.Entry<Integer, Boolean>... expected) {

        assertEquals(predicate.negate(), negatedPredicate, "negated predicate");

        Arrays.stream(expected)
                .forEach(e -> {
                    assertEquals(e.getValue(), predicate.test(e.getKey()), e.getKey() + " expected " + e.getValue());
                    assertNotEquals(e.getValue(), negatedPredicate.test(e.getKey()), e.getKey() + " expected " + !e.getValue());
                });
        return expected.length;
    }

    @SafeVarargs
    private final int test(IntCondition predicate,
                           Map.Entry<Integer, Boolean>... expected) {

        Arrays.stream(expected)
                .forEach(e -> {
                    assertEquals(e.getValue(), predicate.test(e.getKey()), e.getKey() + " expected " + e.getValue());
                });
        return expected.length;
    }

    private static Map.Entry<Integer, Boolean> entry(int value, boolean expected) {
        return new AbstractMap.SimpleImmutableEntry<>(value, expected);
    }
}
