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

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExceptionKeyTest {

    @Test
    public void testEqualsAndHashCode() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        ExceptionKey ek1b = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        assertEquals(ek1, ek1b);
        assertEquals(ek1.hashCode(), ek1b.hashCode());
        assertEquals("ExceptionKey{level=PERF, clazz=class net.openhft.chronicle.core.onoes.ExceptionKeyTest, message='one', throwable=}", ek1.toString());
        ExceptionKey ek2 = new ExceptionKey(LogLevel.WARN, getClass(), "two", null);
        assertEquals("ExceptionKey{level=WARN, clazz=class net.openhft.chronicle.core.onoes.ExceptionKeyTest, message='two', throwable=}", ek2.toString());
        assertNotEquals(ek1, ek2);
        assertNotEquals(ek1.hashCode(), ek2.hashCode());
    }

    @Test
    public void containsTextReturnsTrueWhenMessageMatches() {
        ExceptionKey exceptionKey = new ExceptionKey(LogLevel.WARN, getClass(), "this string matches", null);
        assertTrue(exceptionKey.containsText("matches"));
    }

    @Test
    public void containsTextReturnsFalseWhenMessageAndThrowableAreNull() {
        ExceptionKey exceptionKey = new ExceptionKey(LogLevel.WARN, getClass(), null, null);
        assertFalse(exceptionKey.containsText("matches"));
    }

    @Test
    public void containsTextReturnsTrueWhenMessageIsInThrowableMessage() {
        ExceptionKey exceptionKey = new ExceptionKey(LogLevel.WARN, getClass(), null, new RuntimeException("this string matches"));
        assertTrue(exceptionKey.containsText("matches"));
    }

    @Test
    public void containsTextReturnsTrueWhenMessageIsInThrowableMessageCause() {
        ExceptionKey exceptionKey = new ExceptionKey(LogLevel.WARN, getClass(), null,
                new RuntimeException("no match",
                        new RuntimeException(null,
                                new RuntimeException("this string matches"))));
        assertTrue(exceptionKey.containsText("matches"));
    }

    @Test
    public void containsTextDoesNotGetLostInCircularReference() {
        assertFalse(new ExceptionKey(LogLevel.WARN, getClass(), null, new SelfCausedException("no match")).containsText("matches"));
        assertTrue(new ExceptionKey(LogLevel.WARN, getClass(), null, new SelfCausedException("this string matches")).containsText("matches"));
    }

    private static class SelfCausedException extends Exception {

        public SelfCausedException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable getCause() {
            return this;
        }
    }
}