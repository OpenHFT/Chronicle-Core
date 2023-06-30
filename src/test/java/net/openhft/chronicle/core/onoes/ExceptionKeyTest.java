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

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class ExceptionKeyTest extends CoreTestCommon {

    @Test
    public void testEqualsAndHashCode() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        ExceptionKey ek1b = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        assertEquals(ek1, ek1b);
        assertEquals(ek1.hashCode(), ek1b.hashCode());

        ExceptionKey ek2 = new ExceptionKey(LogLevel.WARN, getClass(), "two", null);
        assertNotEquals(ek1, ek2);
        assertNotEquals(ek1.hashCode(), ek2.hashCode());
    }

    @Test
    public void testEqualsWithDifferentThrowable() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.ERROR, getClass(), "message", new RuntimeException("error1"));
        ExceptionKey ek2 = new ExceptionKey(LogLevel.ERROR, getClass(), "message", new RuntimeException("error2"));
        assertNotEquals(ek1, ek2);
    }

    @Test
    public void testToString() {
        Throwable throwable = new RuntimeException("error");
        ExceptionKey ek = new ExceptionKey(LogLevel.ERROR, getClass(), "message", throwable);

        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        String expectedString = "ExceptionKey{" +
                "level=" + LogLevel.ERROR +
                ", clazz=" + getClass() +
                ", message='message'" +
                ", throwable=" + stringWriter +
                '}';
        assertEquals(expectedString, ek.toString());
}

    @Test
    public void testNullFields() {
        ExceptionKey ek = new ExceptionKey(null, null, null, null);
        assertNotNull(ek.toString()); // Check toString doesn't throw an exception with null fields.
    }
}
