/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class ObjectUtilsTest {
    @Test
    public void testImmutable() {
        for (@NotNull Class c : new Class[]{
                String.class,
                Integer.class,
                Date.class,
                BigDecimal.class,
                ZonedDateTime.class,
        }) {
            assertEquals(c.getName(), ObjectUtils.Immutability.MAYBE, ObjectUtils.isImmutable(c));
        }
        for (@NotNull Class c : new Class[]{
                // StringBuilder.class, // StringBuilder implements Comparable in Java 11
                ArrayList.class,
                HashMap.class,
        }) {
            assertEquals(c.getName(), ObjectUtils.Immutability.NO, ObjectUtils.isImmutable(c));
        }
    }

    @Test
    public void testConvert() {
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1));
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1L));
        assertEquals(1, (int) ObjectUtils.convertTo(int.class, '1'));
        assertEquals(1L, (long) ObjectUtils.convertTo(long.class, '1'));
        assertEquals(1.0, ObjectUtils.convertTo(double.class, '1'), 0.0);
    }
}