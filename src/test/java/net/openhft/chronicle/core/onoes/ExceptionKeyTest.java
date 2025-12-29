/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ExceptionKeyTest extends CoreTestCommon {

    @DisplayName("testEqualsAndHashCode behaviour under expected input and output conditions")
    @Test
    void testEqualsAndHashCode() {
        ExceptionKey ek1 = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        ExceptionKey ek1b = new ExceptionKey(LogLevel.PERF, getClass(), "one", null);
        assertEquals(ek1, ek1b, "ExceptionKey with identical parameters should be equal");
        assertEquals(ek1.hashCode(), ek1b.hashCode(), "ExceptionKey with identical parameters should have same hashCode");
        assertEquals("ExceptionKey{level=PERF, clazz=class net.openhft.chronicle.core.onoes.ExceptionKeyTest, message='one', throwable=}", ek1.toString(),
                "ExceptionKey toString should include level, class, message and throwable for PERF key");
        ExceptionKey ek2 = new ExceptionKey(LogLevel.WARN, getClass(), "two", null);
        assertEquals("ExceptionKey{level=WARN, clazz=class net.openhft.chronicle.core.onoes.ExceptionKeyTest, message='two', throwable=}", ek2.toString(),
                "ExceptionKey toString should include level, class, message and throwable for WARN key");
        assertNotEquals(ek1, ek2, "ExceptionKey values with different levels or messages should not be equal");
        assertNotEquals(ek1.hashCode(), ek2.hashCode(), "ExceptionKey values with different fields should not share a hashCode");
    }
}
