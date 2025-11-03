/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.announcer;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalAnnouncerPrettyTest {

    private static String pretty(String s) throws Exception {
        Method m = InternalAnnouncer.class.getDeclaredMethod("pretty", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, s);
    }

    @Test
    void formatsCommonCases() throws Exception {
        assertEquals("Chronicle Queue", pretty("chronicle-queue"));
        assertEquals("-chronicle - Queue ", pretty("-chronicle---queue-"));
        assertEquals("A", pretty("a"));
        assertEquals("", pretty(""));
    }
}
