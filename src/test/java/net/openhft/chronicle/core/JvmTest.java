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

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.core.util.Time;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static net.openhft.chronicle.core.Jvm.getProcessId;
import static net.openhft.chronicle.core.Jvm.isArm;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

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

    @Test
    public void addToClassPath() {
        Jvm.addToClassPath(JvmTest.class);
    }

    @Test(expected = ConfigurationException.class)
    public void testRethrow() {
        throw Jvm.rethrow(new ConfigurationException());
    }

    @Test
    public void shouldGetMajorVersion() {
        assertTrue(Jvm.majorVersion() > 0);
    }

    @Test
    public void testIsInternal() {
        assertTrue(Jvm.isInternal(String.class.getName()));
        assertFalse(Jvm.isInternal(getClass().getName()));
    }

    @Test
    public void testGetValue() {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        long address = Jvm.getValue(bb, "address");
        assertEquals(((DirectBuffer) bb).address(), address);

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

    @Test
    public void enableSignals() {
        Jvm.signalHandler(signal -> System.out.println(signal + " occurred"));
    }

    @Test
    public void classMetrics() {
        assumeFalse(isArm());
        assertEquals("ClassMetrics{offset=12, length=16}",
                Jvm.classMetrics(ClassA.class).toString());
        assertEquals("ClassMetrics{offset=12, length=16}",
                Jvm.classMetrics(ClassB.class).toString());
        assertEquals("ClassMetrics{offset=12, length=16}",
                Jvm.classMetrics(ClassC.class).toString());
        try {
            Jvm.classMetrics(ClassD.class);
            fail();
        } catch (IllegalArgumentException expected) {
            // ignored
        }
    }

    @Test
    public void microPause() {
        for (int t = 0; t < 4; t++) {
            long start = System.nanoTime();
            int count = 1000_000;
            for (int i = 0; i < count; i++)
                Jvm.nanoPause();
            long time = System.nanoTime() - start;
            long avg = time / count;
            if (t > 0)
                System.out.println("Took " + avg + " ns to nanoPause()");
        }
    }

    @Test
    public void loadSystemProperties() {
        assumeTrue(Jvm.isResourceTracing());
    }

    @Test
    public void address() {
        ByteBuffer bb = ByteBuffer.allocateDirect(64);
        assertTrue(Jvm.address(bb) != 0);
        try {
            Jvm.address(ByteBuffer.allocate(64));
            fail();
        } catch (Exception e) {
            // expected.
        }
    }

    @Test
    public void arrayByteBaseOffset() {
        byte[] bytes = {0};
        UnsafeMemory.UNSAFE.putByte(bytes, (long) Jvm.arrayByteBaseOffset(), (byte) 1);
        assertEquals(1, bytes[0]);
    }

    @Test
    public void doNotCloseOnInterrupt() throws IOException {
        try (FileChannel fc = FileChannel.open(
                Paths.get(OS.getTarget(), "doNotCloseOnInterrupt-" + Time.uniqueId() + ".tmp"),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.DELETE_ON_CLOSE)) {
            Jvm.doNotCloseOnInterrupt(getClass(), fc);
        }
    }

    /**
     * tests that the process is still running
     */
    @Test
    public void isProcessAliveTest() {
        long pid = getProcessId();
        Assert.assertTrue(Jvm.isProcessAlive(pid));
        if (OS.isLinux())
            Assert.assertTrue(Jvm.isProcessAlive(1)); // the kernel
        Assert.assertFalse(Jvm.isProcessAlive(-1));
    }

    static class ClassA {
        long l;
        int i;
        short s;
        byte b;
        boolean flag;
    }

    static class ClassB extends ClassA {
        String text;
    }

    static class ClassC extends ClassB {
        String hi;
    }

    static class ClassD extends ClassC {
        byte x;
    }

}