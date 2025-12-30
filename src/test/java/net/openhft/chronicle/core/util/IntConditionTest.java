/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static net.openhft.chronicle.core.internal.invariant.ints.IntCondition.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IntConditionTest extends CoreTestCommon {

    @DisplayName("Positive values satisfy predicate expectations in tests")
    @Test
    void positive() {
        assertEquals(3, test(
                POSITIVE, NON_POSITIVE,
                Arrays.asList(
                        entry(-1, false),
                        entry(0, false),
                        entry(1, true)
                )
        ), "Positive predicate scenarios cover negative zero and positive values");
    }

    @DisplayName("Negative values satisfy predicate expectations in tests")
    @Test
    void negative() {
        assertEquals(3, test(
                NEGATIVE, NON_NEGATIVE,
                Arrays.asList(
                        entry(-1, true),
                        entry(0, false),
                        entry(1, false)
                )
        ), "Negative predicate scenarios cover negative zero and positive values");
    }

    @DisplayName("Zero value satisfies predicate expectations in tests")
    @Test
    void zero() {
        assertEquals(3, test(
                ZERO, NON_ZERO,
                Arrays.asList(
                        entry(-1, false),
                        entry(0, true),
                        entry(1, false)
                )
        ), "Zero predicate scenarios cover negative zero and positive values");
    }

    @DisplayName("Byte convertible predicate accepts boundary values")
    @Test
    void byteConvertible() {
        assertEquals(5, test(
                BYTE_CONVERTIBLE,
                Arrays.asList(
                        entry(Byte.MIN_VALUE - 1, false),
                        entry(Byte.MIN_VALUE, true),
                        entry(0, true),
                        entry(Byte.MAX_VALUE, true),
                        entry(Byte.MAX_VALUE + 1, false)
                )
        ), "Byte conversion scenarios cover bounds and overflow values");
    }

    @DisplayName("Short convertible predicate accepts boundary values")
    @Test
    void shortConvertible() {
        assertEquals(5, test(
                SHORT_CONVERTIBLE,
                Arrays.asList(
                        entry(Short.MIN_VALUE - 1, false),
                        entry(Short.MIN_VALUE, true),
                        entry(0, true),
                        entry(Short.MAX_VALUE, true),
                        entry(Short.MAX_VALUE + 1, false)
                )
        ), "Short conversion scenarios cover bounds and overflow values");
    }

    @DisplayName("Even power of two int condition")
    @Test
    void evenPowerOfTwo() {
        assertEquals(7, test(
                EVEN_POWER_OF_TWO,
                Arrays.asList(
                        entry(0, false),
                        entry(1, true),
                        entry(2, true),
                        entry(4, true),
                        entry(Integer.MAX_VALUE, false),
                        entry(Integer.MIN_VALUE, false),
                        entry(-2, false)
                )
        ), "Even power-of-two scenarios cover limits and invalid values");
    }

    private final int test(IntCondition predicate,
                           IntCondition negatedPredicate,
                           List<Map.Entry<Integer, Boolean>> expected) {

        assertEquals(predicate.negate(), negatedPredicate, "Negated predicate matches expected logical inversion");

        expected.forEach(e -> {
            assertEquals(e.getValue(), predicate.test(e.getKey()), e.getKey() + " expected " + e.getValue());
            assertNotEquals(e.getValue(), negatedPredicate.test(e.getKey()), e.getKey() + " expected " + !e.getValue());
        });
        return expected.size();
    }

    private final int test(IntCondition predicate,
                           List<Map.Entry<Integer, Boolean>> expected) {

        expected.forEach(e -> {
            assertEquals(e.getValue(), predicate.test(e.getKey()), e.getKey() + " expected " + e.getValue());
        });
        return expected.size();
    }

    private static Map.Entry<Integer, Boolean> entry(int value, boolean expected) {
        return new AbstractMap.SimpleImmutableEntry<>(value, expected);
    }
}
