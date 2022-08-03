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

package net.openhft.chronicle.core.io;

import junit.framework.TestCase;
import org.junit.Test;

public class UnsafeCloseableTest extends TestCase {

    private final UnsafeCloseable uc;

    public UnsafeCloseableTest() {
        uc = new UnsafeCloseable() {
        };
        uc.close();
    }

    @Test
    public void testGetLong() {
        try {
            uc.getLong();
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testSetLong() {
        try {
            uc.setLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testGetVolatileLong() {
        assertEquals(128, uc.getVolatileLong(128));
    }

    @Test
    public void testSetVolatileLong() {
        try {
            uc.setVolatileLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testTestGetVolatileLong() {
        try {
            uc.getVolatileLong();
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testSetOrderedLong() {
        try {
            uc.setOrderedLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testAddLong() {
        try {
            uc.addLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testAddAtomicLong() {
        try {
            uc.addAtomicLong(0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }

    @Test
    public void testCompareAndSwapLong() {
        try {
            uc.compareAndSwapLong(0, 0);
            fail();
        } catch (IllegalStateException ise) {
            // expected.
        }
    }
}