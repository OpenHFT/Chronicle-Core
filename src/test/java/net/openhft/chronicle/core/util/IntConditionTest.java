/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

import static net.openhft.chronicle.core.internal.invariant.ints.IntCondition.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class IntConditionTest extends CoreTestCommon {

    @Test
    public void positive() {
        test(
                POSITIVE, NON_POSITIVE,
                entry(-1, false),
                entry(0, false),
                entry(1, true)
        );
    }

    @Test
    public void negative() {
        test(
                NEGATIVE, NON_NEGATIVE,
                entry(-1, true),
                entry(0, false),
                entry(1, false)
        );
    }

    @Test
    public void zero() {
        test(
                ZERO, NON_ZERO,
                entry(-1, false),
                entry(0, true),
                entry(1, false)
        );
    }

    @Test
    public void byteConvertible() {
        test(
                BYTE_CONVERTIBLE,
                entry(Byte.MIN_VALUE - 1, false),
                entry(Byte.MIN_VALUE, true),
                entry(0, true),
                entry(Byte.MAX_VALUE, true),
                entry(Byte.MAX_VALUE + 1, false)
        );
    }

    @Test
    public void shortConvertible() {
        test(
                SHORT_CONVERTIBLE,
                entry(Short.MIN_VALUE - 1, false),
                entry(Short.MIN_VALUE, true),
                entry(0, true),
                entry(Short.MAX_VALUE, true),
                entry(Short.MAX_VALUE + 1, false)
        );
    }

    @Test
    public void evenPowerOfTwo() {
        test(
                EVEN_POWER_OF_TWO,
                entry(0, false),
                entry(1, true),
                entry(2, true),
                entry(4, true),
                entry(Integer.MAX_VALUE, false),
                entry(Integer.MIN_VALUE, false),
                entry(-2, false)
        );
    }

    private void test(IntCondition predicate,
                      IntCondition negatedPredicate,
                      Map.Entry<Integer, Boolean>... expected) {

        assertEquals(predicate.negate(), negatedPredicate);

        Arrays.stream(expected)
                .forEach(e -> {
                    assertEquals(e.getKey() + " expected " + e.getValue(), e.getValue(), predicate.test(e.getKey()));
                    assertNotEquals(e.getKey() + " expected " + !e.getValue(), e.getValue(), negatedPredicate.test(e.getKey()));
                });
    }

    private void test(IntCondition predicate,
                      Map.Entry<Integer, Boolean>... expected) {

        Arrays.stream(expected)
                .forEach(e -> {
                    assertEquals(e.getKey() + " expected " + e.getValue(), e.getValue(), predicate.test(e.getKey()));
                });
    }

    private static Map.Entry<Integer, Boolean> entry(int value, boolean expected) {
        return new AbstractMap.SimpleImmutableEntry<>(value, expected);
    }
}
