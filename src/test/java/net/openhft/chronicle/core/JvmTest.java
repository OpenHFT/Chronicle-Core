/*
 * Copyright 2016 higherfrequencytrading.com
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

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.threads.ThreadDump;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import javax.naming.ConfigurationException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Created by peter on 26/02/2016.
 */
public class JvmTest {

    private ThreadDump threadDump;

    @Before
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @After
    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Test(expected = ConfigurationException.class)
    public void testRethrow() {
        Jvm.rethrow(new ConfigurationException());
    }

    @Test
    public void testTrimStackTrace() {
        // TODO: 26/02/2016
    }

    @Test
    public void testTrimFirst() {
        // TODO: 26/02/2016

    }

    @Test
    public void testTrimLast() {
        // TODO: 26/02/2016

    }

    @Test
    public void testIsInternal() {
        assertTrue(Jvm.isInternal(String.class.getName()));
        assertFalse(Jvm.isInternal(getClass().getName()));
    }

    @Test
    public void testPause() {
        // TODO: 26/02/2016

    }

    @Test
    public void testBusyWaitMicros() {
        // TODO: 26/02/2016
    }

    @Test
    public void testGetField() {
        // TODO: 26/02/2016

    }

    @Test
    public void testGetValue() {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        long address = Jvm.getValue(bb, "address");
        assertEquals(((DirectBuffer) bb).address(), address);

    }

    @Test
    public void testLockWithStack() {
        // TODO: 26/02/2016

    }

    @Test
    public void testUsedDirectMemory() {
        long used = Jvm.usedDirectMemory();
        ByteBuffer.allocateDirect(4 << 10);
        assertEquals(used + (4 << 10), Jvm.usedDirectMemory());
    }

    @Test
    public void testMaxDirectMemory() {
        long maxDirectMemory = Jvm.maxDirectMemory();
        assertTrue(maxDirectMemory > 0);
    }
}