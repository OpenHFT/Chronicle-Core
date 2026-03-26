/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringInternerTest extends CoreTestCommon {
    @Test
    void testIntern() throws IllegalArgumentException {
        @NotNull StringInterner si = new StringInterner(128);
        for (int i = 0; i < 100; i++) {
            si.intern("" + i);
        }
        assertEquals(82, si.valueCount());
    }

    @Test
    void testInternIndex() throws IllegalArgumentException {
        @NotNull StringInterner si = new StringInterner(128);
        for (int i = 0; i < 100; i++) {
            assertEquals("" + i, si.get(si.index("" + i, null)));
        }
    }

    private String[] uppercase;

    /**
     * an example of the StringInterner used in conjunction with  the uppercase[] to cache another value
     *
     * @throws IllegalArgumentException
     */
    @Test
    void testToUppercaseInternIndex() throws IllegalArgumentException {

        @NotNull StringInterner si = new StringInterner(128);
        uppercase = new String[si.capacity()];
        for (int i = 0; i < 100; i++) {
            String lowerCaseString = randomLowercaseString();
            System.out.println(lowerCaseString.toString());
            int index = si.index(lowerCaseString, this::changed);
            if (index != -1)
                assertEquals(lowerCaseString.toUpperCase(), uppercase[index]);
        }
    }

    private void changed(int index, String value) {
        uppercase[index] = value.toUpperCase();
    }

    private String randomLowercaseString() {
        final String CHARS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        final int count = 1 + (int) ((Math.random() * 10));
        for (int i = 0; i < count; i++) {
            sb.append(CHARS.charAt((int) (Math.random() * CHARS.length())));
        }
        return sb.toString();
    }
}
