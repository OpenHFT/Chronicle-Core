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
import org.mockito.verification.VerificationMode;
import sun.nio.ch.DirectBuffer;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
import static org.mockito.Mockito.mock;

public class JvmTest extends CoreTestCommon {

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

    @Test
    public void resetExceptionHandlersSetHandlersBackToTheirDefaults() throws IllegalAccessException {
        Jvm.setExceptionHandlers(null, null, null, null);
        Jvm.resetExceptionHandlers();
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_PERF_EXCEPTION_HANDLER").get(null), Jvm.perf().defaultHandler());
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_WARN_EXCEPTION_HANDLER").get(null), Jvm.warn().defaultHandler());
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_ERROR_EXCEPTION_HANDLER").get(null), Jvm.error().defaultHandler());
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_DEBUG_EXCEPTION_HANDLER").get(null), Jvm.debug().defaultHandler());
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

        Jvm.addSignalHandler((String signal) -> System.out.println(signal + " occurred"));

        assertFalse(failed.get());
    }

    @Test
    public void classMetrics() throws IllegalArgumentException {
        assumeFalse(isArm());
        assumeFalse(Jvm.isOpenJ9());
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
        UnsafeMemory.MEMORY.writeByte(bytes, Jvm.arrayByteBaseOffset(), (byte) 1);
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
    public void findAnnotationOnClass() {
        final RealAnno ra = findAnnotation(Foo.class, RealAnno.class);
        assertEquals("Hello", ra.value());
    }

    @Test
    public void findAnnotationOnMethod() throws NoSuchMethodException {
        final RealAnno ra = findAnnotation(Foo.class.getDeclaredMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", ra.value());

        final RealAnno ra2 = findAnnotation(Foo.class.getDeclaredMethod("directAnno"), RealAnno.class);
        assertEquals("G'Day", ra2.value());

        final RealAnno rab = findAnnotation(Bar.class.getMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", rab.value());

        final RealAnno rab2 = findAnnotation(Bar.class.getMethod("directAnno"), RealAnno.class);
        assertEquals("G'Day", rab2.value());

        final RealAnno raz = findAnnotation(Baz.class.getMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", raz.value());

        // This case still fails
         final RealAnno raz2 = findAnnotation(Baz.class.getMethod("directAnno"), RealAnno.class);
         assertEquals("G'Day", raz2.value());
    }

    @Test
    public void findAnnotationOnField() throws NoSuchFieldException {
        final RealAnno ra = findAnnotation(DTO.class.getDeclaredField("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", ra.value());

        final RealAnno ra2 = findAnnotation(DTO.class.getDeclaredField("directAnno"), RealAnno.class);
        assertEquals("G'Day", ra2.value());
    }

    @Test
    public void isLambdaClass() {
        Runnable r = () -> System.out.println("Hello, Lambda!");

        assertTrue(Jvm.isLambdaClass(r.getClass()));

        class My$$Lambda$Class {

        }

        assertFalse(Jvm.isLambdaClass(My$$Lambda$Class.class));
    }

    @Test
    public void testCompileThreshold() {
        int threshold = Jvm.compileThreshold();
        assertTrue(threshold > 0);
    }

    @Test
    public void testMajorVersion() {
        int majorVersion = Jvm.majorVersion();
        assertTrue(majorVersion >= 8);
    }

    @Test
    public void testJavaVersionChecks() {
        assertEquals(Jvm.majorVersion() >= 9, Jvm.isJava9Plus());
        assertEquals(Jvm.majorVersion() >= 12, Jvm.isJava12Plus());
        assertEquals(Jvm.majorVersion() >= 14, Jvm.isJava14Plus());
        assertEquals(Jvm.majorVersion() >= 15, Jvm.isJava15Plus());
        assertEquals(Jvm.majorVersion() >= 19, Jvm.isJava19Plus());
        assertEquals(Jvm.majorVersion() >= 20, Jvm.isJava20Plus());
        assertEquals(Jvm.majorVersion() >= 21, Jvm.isJava21Plus());
    }

    @Test
    public void testGetProcessId() {
        int processId = Jvm.getProcessId();
        assertTrue(processId > 0);
    }

    @Test
    public void testTrimStackTrace() {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stes = new StackTraceElement[] {
                new StackTraceElement("Class1", "method1", "Class1.java", 1),
                new StackTraceElement("Class2", "method2", "Class2.java", 2)
        };
        Jvm.trimStackTrace(sb, stes);
        assertTrue(sb.toString().contains("Class1.method1"));
        assertTrue(sb.toString().contains("Class2.method2"));
    }

    @Test
    public void testUsedNativeMemory() {
        long memory = Jvm.usedNativeMemory();
        assertTrue(memory >= 0);
    }

    @Test
    public void testDisableDebugHandler() {
        Jvm.disableDebugHandler();
    }

    @Test
    public void testDisablePerfHandler() {
        Jvm.disablePerfHandler();
    }

    @Test
    public void testDisableWarnHandler() {
        Jvm.disableWarnHandler();
    }

    @Test
    public void testSetThreadLocalExceptionHandlers() {
        ExceptionHandler mockErrorHandler = mock(ExceptionHandler.class);
        Jvm.setThreadLocalExceptionHandlers(mockErrorHandler, null, null);
    }

    @Test
    public void testIsDebugEnabledAndIsPerfEnabled() {
        assertTrue(Jvm.isDebugEnabled(SomeClass.class));
        assertTrue(Jvm.isPerfEnabled(SomeClass.class));
    }

    @Test
    public void testGetSize() {
        long defaultValue = 1024;
        assertEquals(defaultValue, Jvm.getSize("nonexistentProperty", defaultValue));
    }

    @Test
    public void testGetCpuClass() {
        String cpuClass = Jvm.getCpuClass();
        assertNotNull(cpuClass);
    }

    @Test
    public void testCommonInterruptible() {
        FileChannel mockFileChannel = mock(FileChannel.class);
        Jvm.CommonInterruptible commonInterruptible = new Jvm.CommonInterruptible(getClass(), mockFileChannel);

        commonInterruptible.interrupt();
    }

    static class SomeClass {
        private int somePrivateField;
    }

    @Target(value = {ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RealAnno {

        String value();
    }
    @Target(value = {ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @RealAnno("Hello")
    public @interface AnnoAlias {

    }
    static class DTO {
        @AnnoAlias
        long inheritedAnno;

        @RealAnno("G'Day")
        double directAnno;
    }

    @AnnoAlias
    interface Foo {

        @AnnoAlias
        void inheritedAnno();

        @RealAnno("G'Day")
        void directAnno();
    }

    interface Bar extends Foo {

    }

    static class Baz implements Bar {
        @Override
        public void inheritedAnno() {

        }

        @Override
        public void directAnno() {

        }
    }
}
