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

import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.core.util.Time;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Signal;
import sun.nio.ch.DirectBuffer;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.openhft.chronicle.core.Jvm.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

public class JvmTest {

    private ThreadDump threadDump;

    @Before
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @After
    public void checkThreadDump() {
        resetExceptionHandlers();
        threadDump.assertNoNewThreads();
    }

    @Test
    public void addToClassPath() {
        final String propertyBefore = System.getProperty(JAVA_CLASS_PATH);
        Jvm.addToClassPath(JvmTest.class);
        final String propertyAfter = System.getProperty(JAVA_CLASS_PATH);

        if (JvmTest.class.getClassLoader() instanceof URLClassLoader) {
            assertNotSame(propertyBefore, propertyAfter);
        } else {
            assertSame(propertyBefore, propertyAfter);
        }
    }

    @Test(expected = ConfigurationException.class)
    public void testRethrow() {
        throw Jvm.rethrow(new ConfigurationException());
    }

    @Test
    public void shouldGetMajorVersion() {
        assertTrue(Jvm.majorVersion() > 0);
    }

    static final class ReportUnoptimised {

        private ReportUnoptimised() {
        }

        static {
            Jvm.reportUnoptimised();
        }

        static void reportOnce() {
            // Do nothing as reports are made in the static initializer
        }
    }

    @Test
    public void reportThis() {
        final Map<ExceptionKey, Integer> map = recordExceptions();
        ReportUnoptimised.reportOnce();

        final String actual = map.keySet().toString();
        assertTrue(actual, actual.contains("JvmTest.reportThis(JvmTest.java"));
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
        final AtomicBoolean failed = new AtomicBoolean();
        final ExceptionHandler handler = (c, m, t) -> failed.set(true);
        Jvm.setWarnExceptionHandler(handler);
        Jvm.setErrorExceptionHandler(handler);

        Jvm.signalHandler((Signal signal) -> System.out.println(signal + " occurred"));
        Jvm.addSignalHandler((String signal) -> System.out.println(signal + " occurred"));

        assertFalse(failed.get());
    }

    @Test
    public void classMetrics() throws IllegalArgumentException {
        assumeFalse(isArm());
        String expect = "ClassMetrics{offset=" + Jvm.objectHeaderSize() + ", length=16}";
        assertEquals(expect,
                Jvm.classMetrics(ClassA.class).toString());
        assertEquals(expect,
                Jvm.classMetrics(ClassB.class).toString());
        assertEquals(expect,
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
        UnsafeMemory.MEMORY.writeByte(bytes, (long) Jvm.arrayByteBaseOffset(), (byte) 1);
        assertEquals(1, bytes[0]);
    }

    @Test
    public void doNotCloseOnInterrupt() throws IOException {
        final AtomicBoolean failed = new AtomicBoolean();
        final ExceptionHandler handler = (c, m, t) -> failed.set(true);
        Jvm.setWarnExceptionHandler(handler);
        Jvm.setErrorExceptionHandler(handler);

        try (FileChannel fc = FileChannel.open(
                Paths.get(OS.getTarget(), "doNotCloseOnInterrupt-" + Time.uniqueId() + ".tmp"),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.DELETE_ON_CLOSE)) {
            Jvm.doNotCloseOnInterrupt(getClass(), fc);
        }
        assertFalse(failed.get());
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

    @Test
    public void testGetMethod() {
        Assert.assertNotNull(Jvm.getMethod(ClassIWDM.class, "hello", CharSequence.class));
        boolean fail = false;
        try {
            Jvm.getMethod(ClassIWDM.class, "helloDefault", CharSequence.class);
            fail = true;
        } catch (Throwable ignored) {
        }
        assertFalse(fail);
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

    interface InterfaceWithDefaultMethod {
        void hello(CharSequence ignored);

        default void helloDefault(CharSequence cs) {
            hello(cs);
        }
    }

    static class ClassIWDM implements InterfaceWithDefaultMethod {
        @Override
        public void hello(CharSequence ignored) {
        }
    }

    @Test
    public void getCpuClass() {
        final String cpuClass = Jvm.getCpuClass();
        System.out.println("cpuClass: " + cpuClass);
        if (Jvm.isMacArm()) {
            assertEquals(cpuClass, "Apple M1", cpuClass);

        } else if (Jvm.isArm()) {
            assertTrue(cpuClass, cpuClass.startsWith("ARMv"));

        } else {
            assertTrue(cpuClass, (cpuClass.startsWith("Intel") && cpuClass.contains(" CPU ") && cpuClass.contains(" @ "))
                    || (cpuClass.startsWith("AMD ")));
        }

        assertNotNull(cpuClass);
    }

    @Test
    public void removingTag() {
        final String actual = Jvm.CpuClass.removingTag().apply("tag: value");
        assertEquals("value", actual);
    }

}