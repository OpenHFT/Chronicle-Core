/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionKeyTest extends CoreTestCommon {

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
}
