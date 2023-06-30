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

package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ExceptionKeyGptTest extends CoreTestCommon {

    @Test
    public void testDifferentClasses() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.PERF, CoreTestCommon.class, "one", null);

        assertNotEquals(ek1, ek2);
        assertNotEquals(ek1.hashCode(), ek2.hashCode());
    }

    @Test
    public void testDifferentLevels() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.DEBUG, getClass(), "one", null);

        assertNotEquals(ek1, ek2);
        assertNotEquals(ek1.hashCode(), ek2.hashCode());
    }

    @Test
    public void testDifferentMessages() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.PERF, getClass(), "two", null);

        assertNotEquals(ek1, ek2);
        assertNotEquals(ek1.hashCode(), ek2.hashCode());
    }

    @Test
    public void testSameObjects() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);

        assertEquals(ek1, ek1);
        assertEquals(ek1.hashCode(), ek1.hashCode());
    }

    @Test
    public void testWithThrowables() {
        RuntimeException exception = new RuntimeException("Test");
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", exception);
        ExceptionKey ek2 = new ExceptionKey(LogLevel.PERF, getClass(), "one", exception);

        assertEquals(ek1, ek2);
        assertEquals(ek1.hashCode(), ek2.hashCode());
    }

    @Test
    public void testWithDifferentThrowables() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", new RuntimeException("Test"));
        ExceptionKey ek2 = new ExceptionKey(LogLevel.PERF, getClass(), "one", new NullPointerException("Test"));

        assertNotEquals(ek1, ek2);
        assertNotEquals(ek1.hashCode(), ek2.hashCode());
    }
}