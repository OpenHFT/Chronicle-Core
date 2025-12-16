/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.onoes.NullExceptionHandler;
import net.openhft.chronicle.core.onoes.ThreadLocalisedExceptionHandler;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.core.util.Time;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;

public class JvmTest extends CoreTestCommon {

    private ThreadDump threadDump;

    @Override
    @BeforeEach
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @Override
    @AfterEach
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
            assertNotSame(propertyBefore, propertyAfter, "addToClassPath: L61");
        } else {
            assertSame(propertyBefore, propertyAfter, "addToClassPath: L63");
        }
    }

    @Test
    public void testRethrow() {
        assertThrows(ConfigurationException.class,
                () -> {
                    throw Jvm.rethrow(new ConfigurationException());
                },
                "testRethrow");
    }

    @Test
    public void shouldGetMajorVersion() {
        assertTrue(Jvm.majorVersion() > 0, "shouldGetMajorVersion: L74");
    }

    @Test
    public void resetExceptionHandlersSetHandlersBackToTheirDefaults() throws IllegalAccessException {
        Jvm.setExceptionHandlers(null, null, null, null);
        Jvm.resetExceptionHandlers();
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_PERF_EXCEPTION_HANDLER").get(null), Jvm.perf().defaultHandler(), "resetExceptionHandlersSetHandlersBackToTheirDefaults: L81");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_WARN_EXCEPTION_HANDLER").get(null), Jvm.warn().defaultHandler(), "resetExceptionHandlersSetHandlersBackToTheirDefaults: L82");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_ERROR_EXCEPTION_HANDLER").get(null), Jvm.error().defaultHandler(), "resetExceptionHandlersSetHandlersBackToTheirDefaults: L83");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_DEBUG_EXCEPTION_HANDLER").get(null), Jvm.debug().defaultHandler(), "resetExceptionHandlersSetHandlersBackToTheirDefaults: L84");
    }

    @Test
    public void reportThis() {
        final Map<ExceptionKey, Integer> map = recordExceptions();
        ReportUnoptimised.reportOnce();

        final String actual = map.keySet().toString();
        assertTrue(actual.contains("JvmTest.reportThis(JvmTest.java"), actual);
    }

    @Test
    public void testIsInternal() {
        assertTrue(Jvm.isInternal(String.class.getName()), "testIsInternal: L98");
        assertFalse(Jvm.isInternal(getClass().getName()), "testIsInternal: L99");
    }

    @Test
    public void testGetValue() {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        long address = Jvm.getValue(bb, "address");
        assertEquals(((DirectBuffer) bb).address(), address, "testGetValue: L106");

    }

    @Test
    public void testUsedDirectMemory() {
        long used = Jvm.usedDirectMemory();
        assumeFalse(used == 0);
        ByteBuffer.allocateDirect(4 << 10);
        assertEquals(used + (4 << 10), Jvm.usedDirectMemory(), "testUsedDirectMemory: L115");
    }

    @Test
    public void testMaxDirectMemory() {
        long maxDirectMemory = Jvm.maxDirectMemory();
        assertTrue(maxDirectMemory > 0, "testMaxDirectMemory: L121");
    }

    @Test
    public void enableSignals() {
        final AtomicBoolean failed = new AtomicBoolean();
        final ExceptionHandler handler = (c, m, t) -> failed.set(true);
        Jvm.setWarnExceptionHandler(handler);
        Jvm.setErrorExceptionHandler(handler);

        Jvm.addSignalHandler((String signal) -> System.out.println(signal + " occurred"));

        assertFalse(failed.get(), "enableSignals: L133");
    }

    @Test
    public void classMetrics() throws IllegalArgumentException {
        assumeFalse(isArm());
        assertThrows(IllegalArgumentException.class, () -> Jvm.classMetrics(ClassD.class));
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
        assertTrue(true, "microPause: L154"); // If we reach here, the test passes
    }

    @Test
    public void address() {
        ByteBuffer bb = ByteBuffer.allocateDirect(64);
        assertNotEquals(0, Jvm.address(bb));
        assertThrows(Exception.class, () -> Jvm.address(ByteBuffer.allocate(64)));
    }

    @Test
    public void arrayByteBaseOffset() {
        byte[] bytes = {0};
        UnsafeMemory.MEMORY.writeByte(bytes, Jvm.arrayByteBaseOffset(), (byte) 1);
        assertEquals(1, bytes[0], "arrayByteBaseOffset: L168");
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
        assertFalse(failed.get(), "doNotCloseOnInterrupt: L185");
    }

    /**
     * tests that the process is still running
     */
    @Test
    public void isProcessAliveTest() {
        long pid = getProcessId();
        assertTrue(Jvm.isProcessAlive(pid), "isProcessAliveTest: L194");
        if (OS.isLinux())
            assertTrue(Jvm.isProcessAlive(1), "isProcessAliveTest: L196"); // the kernel
        assertFalse(Jvm.isProcessAlive(-1), "isProcessAliveTest: L197");
    }

    @Test
    public void testGetMethod() {
        assertNotNull(Jvm.getMethod(ClassIWDM.class, "hello", CharSequence.class), "testGetMethod: L202");
        assertThrows(Throwable.class,
                () -> Jvm.getMethod(ClassIWDM.class, "helloDefault", CharSequence.class));
    }

    @Test
    public void findAnnotationOnClass() {
        final RealAnno ra = findAnnotation(Foo.class, RealAnno.class);
        assertEquals("Hello", ra.value(), "findAnnotationOnClass: L210");
    }

    @Test
    public void findAnnotationOnMethod() throws NoSuchMethodException {
        final RealAnno ra = findAnnotation(Foo.class.getDeclaredMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", ra.value(), "findAnnotationOnMethod: L216");

        final RealAnno ra2 = findAnnotation(Foo.class.getDeclaredMethod("directAnno"), RealAnno.class);
        assertEquals("G'Day", ra2.value(), "findAnnotationOnMethod: L219");

        final RealAnno rab = findAnnotation(Bar.class.getMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", rab.value(), "findAnnotationOnMethod: L222");

        final RealAnno rab2 = findAnnotation(Bar.class.getMethod("directAnno"), RealAnno.class);
        assertEquals("G'Day", rab2.value(), "findAnnotationOnMethod: L225");

        final RealAnno raz = findAnnotation(Baz.class.getMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", raz.value(), "findAnnotationOnMethod: L228");

        // This case still fails
        final RealAnno raz2 = findAnnotation(Baz.class.getMethod("directAnno"), RealAnno.class);
        assertEquals("G'Day", raz2.value(), "findAnnotationOnMethod: L232");
    }

    @Test
    public void findAnnotationOnField() throws NoSuchFieldException {
        final RealAnno ra = findAnnotation(DTO.class.getDeclaredField("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", ra.value(), "findAnnotationOnField: L238");

        final RealAnno ra2 = findAnnotation(DTO.class.getDeclaredField("directAnno"), RealAnno.class);
        assertEquals("G'Day", ra2.value(), "findAnnotationOnField: L241");
    }

    @Test
    public void isLambdaClass() {
        Runnable r = () -> System.out.println("Hello, Lambda!");

        assertTrue(Jvm.isLambdaClass(r.getClass()), "isLambdaClass: L248");

        // needs to look like a lambda class so don't change the name
        class My$$Lambda$Class {

        }

        assertFalse(Jvm.isLambdaClass(My$$Lambda$Class.class), "isLambdaClass: L255");
    }

    @Test
    public void testCompileThreshold() {
        int threshold = Jvm.compileThreshold();
        assertTrue(threshold > 0, "testCompileThreshold: L261");
    }

    @Test
    public void testMajorVersion() {
        int majorVersion = Jvm.majorVersion();
        assertTrue(majorVersion >= 8, "testMajorVersion: L267");
    }

    @Test
    public void testJavaVersionChecks() {
        assertEquals(Jvm.majorVersion() >= 9, Jvm.isJava9Plus(), "testJavaVersionChecks: L272");
        assertEquals(Jvm.majorVersion() >= 12, Jvm.isJava12Plus(), "testJavaVersionChecks: L273");
        assertEquals(Jvm.majorVersion() >= 14, Jvm.isJava14Plus(), "testJavaVersionChecks: L274");
        assertEquals(Jvm.majorVersion() >= 15, Jvm.isJava15Plus(), "testJavaVersionChecks: L275");
        assertEquals(Jvm.majorVersion() >= 19, Jvm.isJava19Plus(), "testJavaVersionChecks: L276");
        assertEquals(Jvm.majorVersion() >= 20, Jvm.isJava20Plus(), "testJavaVersionChecks: L277");
        assertEquals(Jvm.majorVersion() >= 21, Jvm.isJava21Plus(), "testJavaVersionChecks: L278");
    }

    @Test
    public void testGetProcessId() {
        int processId = Jvm.getProcessId();
        assertTrue(processId > 0, "testGetProcessId: L284");
    }

    @Test
    public void testTrimStackTrace() {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stes = {
                new StackTraceElement("Class1", "method1", "Class1.java", 1),
                new StackTraceElement("Class2", "method2", "Class2.java", 2)
        };
        Jvm.trimStackTrace(sb, stes);
        assertTrue(sb.toString().contains("Class1.method1"), "testTrimStackTrace: L295");
        assertTrue(sb.toString().contains("Class2.method2"), "testTrimStackTrace: L296");
    }

    @Test
    public void testUsedNativeMemory() {
        long memory = Jvm.usedNativeMemory();
        assertTrue(memory >= 0, "testUsedNativeMemory: L302");
    }

    @Test
    public void testDisableDebugHandler() {
        Jvm.disableDebugHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.debug()), "testDisableDebugHandler: L308");
    }

    @Test
    public void testDisablePerfHandler() {
        Jvm.disablePerfHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.perf()), "testDisablePerfHandler: L314");
    }

    @Test
    public void testDisableWarnHandler() {
        Jvm.disableWarnHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.warn()), "testDisableWarnHandler: L320");
    }

    @Test
    public void testSetThreadLocalExceptionHandlers() {
        ExceptionHandler mockErrorHandler = mock(ExceptionHandler.class);
        Jvm.setThreadLocalExceptionHandlers(mockErrorHandler, null, null);
        assertSame(mockErrorHandler, ThreadLocalisedExceptionHandler.unwrap(Jvm.error()), "testSetThreadLocalExceptionHandlers: L327");
        assertEquals(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.warn()), "testSetThreadLocalExceptionHandlers: L328");
        assertEquals(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.debug()), "testSetThreadLocalExceptionHandlers: L329");
    }

    @Test
    public void testIsDebugEnabledAndIsPerfEnabled() {
        assertTrue(Jvm.isDebugEnabled(SomeClass.class), "testIsDebugEnabledAndIsPerfEnabled: L334");
        assertTrue(Jvm.isPerfEnabled(SomeClass.class), "testIsDebugEnabledAndIsPerfEnabled: L335");
    }

    @Test
    public void testGetSize() {
        long defaultValue = 1024;
        assertEquals(defaultValue, Jvm.getSize("nonexistentProperty", defaultValue), "testGetSize: L341");
    }

    @Test
    public void testGetCpuClass() {
        String cpuClass = Jvm.getCpuClass();
        assertNotNull(cpuClass, "testGetCpuClass: L347");
    }

    @Test
    public void testCommonInterruptible() {
        FileChannel mockFileChannel = mock(FileChannel.class);
        Jvm.CommonInterruptible commonInterruptible = new Jvm.CommonInterruptible(getClass(), mockFileChannel);

        commonInterruptible.interrupt();
        assertNotNull(commonInterruptible, "testCommonInterruptible: L356");
    }

    @Test
    public void getPackageName() {
        assertEquals("net.openhft.chronicle.core", Jvm.getPackageName(Jvm.class), "getPackageName: L361");
    }

    interface InterfaceWithDefaultMethod {
        @SuppressWarnings("EmptyMethod")
        void hello(CharSequence ignored);

        default void helloDefault(CharSequence cs) {
            hello(cs);
        }
    }

    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RealAnno {

        String value();
    }

    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @RealAnno("Hello")
    @interface AnnoAlias {

    }

    @AnnoAlias
    @SuppressWarnings("EmptyMethod")
    interface Foo {

        @AnnoAlias
        void inheritedAnno();

        @RealAnno("G'Day")
        void directAnno();
    }

    interface Bar extends Foo {

    }

    static final class ReportUnoptimised {

        static {
            Jvm.reportUnoptimised();
        }

        private ReportUnoptimised() {
        }

        @SuppressWarnings("EmptyMethod")
        static void reportOnce() {
            // Do nothing as reports are made in the static initializer
        }
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

    private static class ClassD extends ClassC {
        byte x;
    }

    static class ClassIWDM implements InterfaceWithDefaultMethod {
        @Override
        public void hello(CharSequence ignored) {
        }
    }

    private static class SomeClass {
        private int somePrivateField;
    }

    static class DTO {
        @AnnoAlias
        long inheritedAnno;

        @RealAnno("G'Day")
        double directAnno;
    }

    static class Baz implements Bar {
        @Override
        public void inheritedAnno() {
            // No-op: used to test annotation resolution on overridden methods
        }

        @Override
        public void directAnno() {
            // No-op: used to test annotation resolution on overridden methods
        }
    }
}
