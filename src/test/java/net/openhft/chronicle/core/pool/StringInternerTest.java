/*
 * Copyright 2016-2020 chronicle.software
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

package net.openhft.chronicle.core.pool;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringInternerTest {


    @Test
    public void testIntern() throws IllegalArgumentException {
        @NotNull StringInterner si = new StringInterner(128);
        for (int i = 0; i < 100; i++) {
            si.intern("" + i);
        }
        assertEquals(82, si.valueCount());
    }

    @Test
    public void testInternIndex() throws IllegalArgumentException {
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
    public void testToUppercaseInternIndex() throws IllegalArgumentException {

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


    private String randomLowercaseString() {
        final String CHARS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        final int count = 1 + (int) ((Math.random() * 10));
        for (int i = 0; i < count; i++) {
            sb.append(CHARS.charAt((int) (Math.random() * CHARS.length())));
        }
        return sb.toString();
    }

    private void changed(int index, String value) {
        uppercase[index] = value.toUpperCase();
    }


}