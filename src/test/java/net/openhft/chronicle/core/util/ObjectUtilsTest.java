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

import org.junit.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter on 11/12/16.
 */
public class ObjectUtilsTest {
    @Test
    public void testImmutable() {
        for (Class c : new Class[]{
                String.class,
                Integer.class,
                Date.class,
                BigDecimal.class,
                ZonedDateTime.class,
        }) {
            assertEquals(ObjectUtils.Immutability.MAYBE, ObjectUtils.isImmutable(c));
        }
        for (Class c : new Class[]{
                StringBuilder.class,
                ArrayList.class,
                HashMap.class,
        }) {
            assertEquals(ObjectUtils.Immutability.NO, ObjectUtils.isImmutable(c));
        }
    }
}