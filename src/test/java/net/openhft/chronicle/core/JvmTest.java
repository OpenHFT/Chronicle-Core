/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
import sun.nio.ch.DirectBuffer;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.openhft.chronicle.core.Jvm.JAVA_CLASS_PATH;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

public class JvmTest extends CoreTestCommon {

    private ThreadDump threadDump;

    @Before
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @After
    public void checkThreadDump() {
        Jvm.resetExceptionHandlers();
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

    @Test
    public void resetExceptionHandlersSetHandlersBackToTheirDefaults() throws IllegalAccessException {
        Jvm.setExceptionHandlers(null, null, null, null);
        Jvm.resetExceptionHandlers();
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_PERF_EXCEPTION_HANDLER").get(null), Jvm.perf().defaultHandler());
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_WARN_EXCEPTION_HANDLER").get(null), Jvm.warn().defaultHandler());
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_ERROR_EXCEPTION_HANDLER").get(null), Jvm.error().defaultHandler());
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_DEBUG_EXCEPTION_HANDLER").get(null), Jvm.debug().defaultHandler());
    }

    static void assertBetween(double d, double min, double max) {
        assertEquals(d, (min + max) / 2, Math.abs(max - min) / 2);
    }

    @Test
    public void compileThreshold() {
        assertBetween(Jvm.compileThreshold(), 1000, 10000);
    }

    @Test
    public void majorVersion() {
        assertBetween(Jvm.majorVersion(), 8, 21);
    }

    @Test
    public void isJava9Plus() {
        assertEquals(Jvm.majorVersion()>= 9, Jvm.isJava9Plus());
    }

    @Test
    public void isJava12Plus() {
        assertEquals(Jvm.majorVersion()>= 12, Jvm.isJava12Plus());
    }

    @Test
    public void isJava14Plus() {
        assertEquals(Jvm.majorVersion()>= 14, Jvm.isJava14Plus());
    }

    @Test
    public void isJava15Plus() {
        assertEquals(Jvm.majorVersion()>= 15, Jvm.isJava15Plus());
    }

    @Test
    public void isJava19Plus() {
        assertEquals(Jvm.majorVersion()>= 19, Jvm.isJava19Plus());
    }

    @Test
    public void isJava20Plus() {
        assertEquals(Jvm.majorVersion()>= 20, Jvm.isJava20Plus());
    }

    @Test
    public void getProcessId() {
        assertNotEquals(0, Jvm.getProcessId());
    }

    @Test
    public void rethrow() {
        try {
            Jvm.rethrow(new StackTrace());
            fail();
        } catch (Throwable se) {
            if (se.getClass() != StackTrace.class)
                throw se;
        }
    }

    @Test
    public void trimStackTrace() {
        StringBuilder sb = new StringBuilder();
        Jvm.trimStackTrace(sb, new StackTrace().getStackTrace());
        assertTrue(sb.toString().contains("trimStackTrace"));
    }

    @Test
    public void isInternal() {
        assertTrue(Jvm.isInternal("jdk."));
        assertTrue(Jvm.isInternal("sun."));
        assertTrue(Jvm.isInternal("java."));
        assertFalse(Jvm.isInternal("net."));
    }

    @Test
    public void isDebug() {
        assertNotNull(Jvm.isDebug());
    }

    @Test
    public void isFlightRecorder() {
        assertNotNull(Jvm.isFlightRecorder());
    }

    @Test
    public void isCodeCoverage() {
        assertNotNull(Jvm.isCodeCoverage());
    }

    @Test
    public void getField() {
        Field field = Jvm.getField(Jvm.class, "JAVA_CLASS_PATH");
        assertEquals("JAVA_CLASS_PATH", field.getName());
        boolean ok = true;
        try {
            Jvm.getField(Jvm.class, "NULL");
            ok = false;
        } catch (AssertionError e) {
            // expected
        }
        assertTrue(ok);
    }

    @Test
    public void getFieldOrNull() {
        Field field = Jvm.getFieldOrNull(Jvm.class, "NULL");
        assertNull(field);
    }

    @Test
    public void getMethod() {
        Method getMethod = Jvm.getMethod(getClass(), "getMethod");
        assertEquals("getMethod", getMethod.getName());
    }

    @Test
    public void getValue() {
        assertEquals(128, (int) Jvm.getValue(128, "value"));
    }

    @Test
    public void lockWithStack() {
    }

    @Test
    public void fieldOffset() {
        assertNotEquals(0, Jvm.fieldOffset(Integer.class, "value"));
    }

    @Test
    public void usedDirectMemory() {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        assertNotEquals(0, Jvm.usedDirectMemory());
    }

    @Test
    public void usedNativeMemory() {
        long addr = UnsafeMemory.MEMORY.allocate(128);
        assertNotEquals(0, Jvm.usedNativeMemory());
        UnsafeMemory.MEMORY.freeMemory(addr, 128);
    }

    @Test
    public void maxDirectMemory() {
        assertNotEquals(0, Jvm.maxDirectMemory());
    }

    @Test
    public void is64bit() {
        assertTrue(Jvm.is64bit());
    }

    @Test
    public void dontChain() {
        assertTrue(Jvm.dontChain(Integer.class));
        assertFalse(Jvm.dontChain(Jvm.class));
    }

    @Test
    public void isResourceTracing() {
        assertNotNull(Jvm.isResourceTracing());
    }

    @Test
    public void getLong() {
        System.setProperty("-num-", "128");
        assertEquals(128, (long) Jvm.getLong("-num-", Long.MAX_VALUE));
    }

    @Test
    public void getInteger() {
        System.setProperty("-num-", "128");
        assertEquals(128, (int) Jvm.getInteger("-num-", 1));
    }

    @Test
    public void testGetBoolean() {
        System.setProperty("-flag-", "yes");
        System.setProperty("-flag2-", "");
        System.setProperty("-flag3-", "true");
        assertFalse(Jvm.getBoolean("-none-"));
        assertFalse(Jvm.getBoolean("-none-", false));
        assertTrue(Jvm.getBoolean("-flag-"));
        assertTrue(Jvm.getBoolean("-flag2-"));
        assertTrue(Jvm.getBoolean("-flag3-"));
    }

    @Test
    public void parseSize() {
        assertEquals(64 << 10, Jvm.parseSize("64k"));
    }

    @Test
    public void getSize() {
        String key = "-key-";
        System.setProperty(key, "128M");
        assertEquals(128 << 20, Jvm.getSize(key, 1));
        assertEquals(1, Jvm.getSize("-none-", 1));
    }

    @Test
    public void getDouble() {
        String key = "-dummy-";
        System.setProperty(key, "128");
        assertEquals(128, Jvm.getDouble(key, 1), 0);
        assertEquals(1, Jvm.getDouble("-none-", 1), 0);
    }

    @Test
    public void isProcessAlive() {
        assertTrue(Jvm.isProcessAlive(Jvm.getProcessId()));
    }

    @Test
    public void isAzulZulu() {
        assertNotNull(Jvm.isAzulZulu());
    }

    @Test
    public void objectHeaderSize() {
        assertBetween(Jvm.objectHeaderSize(), 8 , 16);
    }

    @Test
    public void isAssertEnabled() {
        boolean flag = false;
        assert flag = true;
        assertEquals(flag, Jvm.isAssertEnabled());
    }

    @Test
    public void supportThread() {
        assertFalse(Jvm.supportThread());
    }

    @Test
    public void findAnnotation() {
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
        final Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
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

        Jvm.addSignalHandler((String signal) -> System.out.println(signal + " occurred"));

        assertFalse(failed.get());
    }

    @Test
    public void classMetrics() throws IllegalArgumentException {
        assumeFalse(Jvm.isArm());
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
        assertNotEquals(0, Jvm.address(bb));
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
        long pid = Jvm.getProcessId();
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
        System.out.println("cpuClass: " + cpuClass + ", os.name: " + System.getProperty("os.name") + ", os.arch: " + System.getProperty("os.arch"));
        if (Jvm.isMacArm()) {
            assertEquals(cpuClass, "Apple M1", cpuClass);

        } else if (Jvm.isArm()) {
            assertTrue(cpuClass, cpuClass.startsWith("ARMv"));

        } else {
            if (cpuClass.contains("th Gen Intel"))
                return;
            assertTrue(cpuClass, (cpuClass.startsWith("Intel") && cpuClass.contains(" CPU ") && cpuClass.contains(" @ "))
                    || (cpuClass.startsWith("AMD ")));
        }

        assertNotNull(cpuClass);
    }

    @Test
    public void findAnnotationOnClass() {
        final RealAnno ra = Jvm.findAnnotation(Foo.class, RealAnno.class);
        assertEquals("Hello", ra.value());
    }

    @Target(value = {ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RealAnno {
        String value();
    }

    @Target(value = {ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @RealAnno("Hello")
    public @interface AnnoAlias {
    }

    @AnnoAlias
    class Foo {
    }
}