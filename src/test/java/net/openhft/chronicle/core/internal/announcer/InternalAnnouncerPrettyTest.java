/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.announcer;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Formats common cases internal announcer pretty")
    void formatsCommonCases() throws Exception {
        assertEquals("Chronicle Queue", pretty("chronicle-queue"), "pretty should convert hyphenated lowercase to title case with spaces");
        assertEquals("-chronicle - Queue ", pretty("-chronicle---queue-"), "pretty should preserve leading/trailing hyphens and normalize multiple hyphens");
        assertEquals("A", pretty("a"), "pretty should capitalize single lowercase letter");
        assertEquals("", pretty(""), "pretty should return empty string for empty input");
    }
}
