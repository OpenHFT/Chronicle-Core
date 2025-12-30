/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.onoes.NullExceptionHandler;
import net.openhft.chronicle.core.onoes.ThreadLocalisedExceptionHandler;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.core.internal.util.DirectBufferUtil;
import net.openhft.chronicle.core.util.Time;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

@SuppressWarnings("deprecation")
class JvmTest extends CoreTestCommon {

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

    @DisplayName("addToClassPath updates class path property values")
    @Test
    void addToClassPath() {
        final String propertyBefore = System.getProperty(JAVA_CLASS_PATH);
        Jvm.addToClassPath(JvmTest.class);
        final String propertyAfter = System.getProperty(JAVA_CLASS_PATH);

        if (JvmTest.class.getClassLoader() instanceof URLClassLoader) {
            assertNotSame(propertyBefore, propertyAfter, "class path property should change for URLClassLoader");
        } else {
            assertSame(propertyBefore, propertyAfter, "class path property should retain the same instance for non-URLClassLoader");
        }
    }

    @DisplayName("rethrow surfaces configuration exceptions from helpers")
    @Test
    void testRethrow() {
        assertThrows(ConfigurationException.class,
                () -> {
                    throw Jvm.rethrow(new ConfigurationException());
                },
                "rethrow should surface ConfigurationException");
    }

    @DisplayName("Major version returns positive value for JVM")
    @Test
    void shouldGetMajorVersion() {
        int majorVersion = Jvm.majorVersion();
        assertTrue(majorVersion > 0, "JVM major version should be greater than zero: majorVersion=" + majorVersion);
    }

    @DisplayName("resetExceptionHandlers restores default exception handlers fully")
    @Test
    void resetExceptionHandlersSetHandlersBackToTheirDefaults() throws IllegalAccessException {
        Jvm.setExceptionHandlers(null, null, null, null);
        Jvm.resetExceptionHandlers();
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_PERF_EXCEPTION_HANDLER").get(null), Jvm.perf().defaultHandler(), "perf exception handler should be reset to default handler");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_WARN_EXCEPTION_HANDLER").get(null), Jvm.warn().defaultHandler(), "warn exception handler should be reset to default handler");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_ERROR_EXCEPTION_HANDLER").get(null), Jvm.error().defaultHandler(), "error exception handler should be reset to default handler");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_DEBUG_EXCEPTION_HANDLER").get(null), Jvm.debug().defaultHandler(), "debug exception handler should be reset to default handler");
    }

    @DisplayName("reportUnoptimised records usage once per class")
    @Test
    void reportThis() {
        final Map<ExceptionKey, Integer> map = recordExceptions();
        ReportUnoptimised.reportOnce();

        final String actual = map.keySet().toString();
        assertTrue(actual.contains("JvmTest.reportThis(JvmTest.java"),
                actual + " should contain JvmTest.reportThis(JvmTest.java");
    }

    @DisplayName("isInternal identifies JDK classes correctly at runtime")
    @Test
    void testIsInternal() {
        assertTrue(Jvm.isInternal(String.class.getName()), "String class should be recognized as internal JDK class");
        assertFalse(Jvm.isInternal(getClass().getName()), "test class should not be recognized as internal JDK class");
    }

    @DisplayName("getValue extracts nested field values safely")
    @Test
    void testGetValue() {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        long address = Jvm.getValue(bb, "address");
        long expected = DirectBufferUtil.addressOrThrow(bb);
        assertEquals(expected, address, "retrieved address via reflection should match direct buffer address");

    }

    @DisplayName("usedDirectMemory increases after direct buffer allocation")
    @Test
    void testUsedDirectMemory() {
        long used = Jvm.usedDirectMemory();
        assumeFalse(used == 0);
        ByteBuffer.allocateDirect(4 << 10);
        assertEquals(used + (4 << 10), Jvm.usedDirectMemory(), "used direct memory should increase by 4KB after allocating 4KB direct buffer");
    }

    @DisplayName("maxDirectMemory reports positive direct memory limit")
    @Test
    void testMaxDirectMemory() {
        long maxDirectMemory = Jvm.maxDirectMemory();
        assertTrue(maxDirectMemory > 0, "maximum direct memory limit should be positive: maxDirectMemory=" + maxDirectMemory);
    }

    @DisplayName("signal handlers register without error callbacks")
    @Test
    void enableSignals() {
        final AtomicBoolean failed = new AtomicBoolean();
        final ExceptionHandler handler = (c, m, t) -> failed.set(true);
        Jvm.setWarnExceptionHandler(handler);
        Jvm.setErrorExceptionHandler(handler);

        Jvm.addSignalHandler((String signal) -> System.out.println(signal + " occurred"));

        assertFalse(failed.get(), "signal handler registration should not trigger warn or error exception handlers");
    }

    @DisplayName("classMetrics rejects layout on ARM platforms")
    @Test
    void classMetrics() throws IllegalArgumentException {
        assumeFalse(isArm());
        assertThrows(IllegalArgumentException.class, () -> Jvm.classMetrics(ClassD.class),
                "classMetrics should reject layout introspection for ClassD");
    }

    @DisplayName("Nano pause runs repeatedly without failure")
    @Test
    void microPause() {
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
        assertTrue(true, "nanoPause() should complete one million iterations without throwing exception"); // If we reach here, the test passes
    }

    @DisplayName("address returns non-zero for direct buffers")
    @Test
    void address() {
        ByteBuffer bb = ByteBuffer.allocateDirect(64);
        assertNotEquals(0, Jvm.address(bb), "direct buffer address should be non-zero");
        assertThrows(Exception.class, () -> Jvm.address(ByteBuffer.allocate(64)),
                "heap buffer address should not be accessible");
    }

    @DisplayName("arrayByteBaseOffset matches unsafe base offset value")
    @Test
    void arrayByteBaseOffset() {
        byte[] bytes = {0};
        UnsafeMemory.MEMORY.writeByte(bytes, Jvm.arrayByteBaseOffset(), (byte) 1);
        assertEquals(1, bytes[0], "byte array element at base offset should be updated to 1 via unsafe memory write");
    }

    @DisplayName("Do not close on interrupt leaves channel open safely")
    @Test
    void doNotCloseOnInterrupt() throws IOException {
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
        assertFalse(failed.get(), "configuring file channel to not close on interrupt should not trigger warn or error handlers");
    }

    /**
     * tests that the process is still running
     */
    @DisplayName("isProcessAlive detects live and dead PIDs")
    @Test
    void isProcessAliveTest() {
        long pid = getProcessId();
        assertTrue(Jvm.isProcessAlive(pid), "current process should be detected as alive by its PID");
        if (OS.isLinux())
            assertTrue(Jvm.isProcessAlive(1), "init/systemd process (PID 1) should be detected as alive on Linux"); // the kernel
        assertFalse(Jvm.isProcessAlive(-1), "invalid negative PID should not be detected as alive process");
    }

    @DisplayName("getMethod locates declared and default methods")
    @Test
    void testGetMethod() {
        assertNotNull(Jvm.getMethod(ClassIWDM.class, "hello", CharSequence.class), "getMethod should successfully find declared method 'hello' on ClassIWDM");
        assertThrows(Throwable.class,
                () -> Jvm.getMethod(ClassIWDM.class, "helloDefault", CharSequence.class),
                "getMethod should fail for missing default method on ClassIWDM");
    }

    @DisplayName("findAnnotation resolves aliased class annotations correctly")
    @Test
    void findAnnotationOnClass() {
        final RealAnno ra = findAnnotation(Foo.class, RealAnno.class);
        assertEquals("Hello", ra.value(), "annotation value should be 'Hello' when found via aliased annotation on class");
    }

    @DisplayName("findAnnotation resolves aliased method annotations correctly")
    @Test
    void findAnnotationOnMethod() throws NoSuchMethodException {
        final RealAnno ra = findAnnotation(Foo.class.getDeclaredMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", ra.value(), "annotation on Foo.inheritedAnno should have value 'Hello' via aliased annotation");

        final RealAnno ra2 = findAnnotation(Foo.class.getDeclaredMethod("directAnno"), RealAnno.class);
        assertEquals("G'Day", ra2.value(), "annotation on Foo.directAnno should have value 'G'Day' via direct annotation");

        final RealAnno rab = findAnnotation(Bar.class.getMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", rab.value(), "annotation on Bar.inheritedAnno should have value 'Hello' via inherited aliased annotation");

        final RealAnno rab2 = findAnnotation(Bar.class.getMethod("directAnno"), RealAnno.class);
        assertEquals("G'Day", rab2.value(), "annotation on Bar.directAnno should have value 'G'Day' via inherited direct annotation");

        final RealAnno raz = findAnnotation(Baz.class.getMethod("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", raz.value(), "annotation on Baz.inheritedAnno should have value 'Hello' via overridden method with inherited aliased annotation");

        // This case still fails
        final RealAnno raz2 = findAnnotation(Baz.class.getMethod("directAnno"), RealAnno.class);
        assertEquals("G'Day", raz2.value(), "annotation on Baz.directAnno should have value 'G'Day' via overridden method with inherited direct annotation");
    }

    @DisplayName("findAnnotation resolves aliased field annotations correctly")
    @Test
    void findAnnotationOnField() throws NoSuchFieldException {
        final RealAnno ra = findAnnotation(DTO.class.getDeclaredField("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", ra.value(), "annotation on DTO.inheritedAnno field should have value 'Hello' via aliased annotation");

        final RealAnno ra2 = findAnnotation(DTO.class.getDeclaredField("directAnno"), RealAnno.class);
        assertEquals("G'Day", ra2.value(), "annotation on DTO.directAnno field should have value 'G'Day' via direct annotation");
    }

    @DisplayName("isLambdaClass detects real lambda classes correctly")
    @Test
    void isLambdaClass() {
        Runnable r = () -> System.out.println("Hello, Lambda!");

        assertTrue(Jvm.isLambdaClass(r.getClass()), "real lambda class should be detected as lambda by isLambdaClass");

        // needs to look like a lambda class so don't change the name
        @SuppressWarnings("TypeName")
        class My$$Lambda$Class {

        }

        assertFalse(Jvm.isLambdaClass(My$$Lambda$Class.class), "local class with lambda-like name should not be detected as lambda by isLambdaClass");
    }

    @DisplayName("compileThreshold returns positive JIT threshold value")
    @Test
    void testCompileThreshold() {
        int threshold = Jvm.compileThreshold();
        assertTrue(threshold > 0, "JIT compile threshold should be positive: threshold=" + threshold);
    }

    @DisplayName("majorVersion meets minimum supported Java level")
    @Test
    void testMajorVersion() {
        int majorVersion = Jvm.majorVersion();
        assertTrue(majorVersion >= 8, "JVM major version should be >= 8: majorVersion=" + majorVersion);
    }

    @DisplayName("Java version helpers align with major version")
    @Test
    void testJavaVersionChecks() {
        assertEquals(Jvm.majorVersion() >= 9, Jvm.isJava9Plus(), "isJava9Plus should return true if and only if major version is at least 9");
        assertEquals(Jvm.majorVersion() >= 12, Jvm.isJava12Plus(), "isJava12Plus should return true if and only if major version is at least 12");
        assertEquals(Jvm.majorVersion() >= 14, Jvm.isJava14Plus(), "isJava14Plus should return true if and only if major version is at least 14");
        assertEquals(Jvm.majorVersion() >= 15, Jvm.isJava15Plus(), "isJava15Plus should return true if and only if major version is at least 15");
        assertEquals(Jvm.majorVersion() >= 19, Jvm.isJava19Plus(), "isJava19Plus should return true if and only if major version is at least 19");
        assertEquals(Jvm.majorVersion() >= 20, Jvm.isJava20Plus(), "isJava20Plus should return true if and only if major version is at least 20");
        assertEquals(Jvm.majorVersion() >= 21, Jvm.isJava21Plus(), "isJava21Plus should return true if and only if major version is at least 21");
    }

    @DisplayName("getProcessId returns positive process identifier value")
    @Test
    void testGetProcessId() {
        int processId = Jvm.getProcessId();
        assertTrue(processId > 0, "process ID should be positive: processId=" + processId);
    }

    @DisplayName("trimStackTrace includes all expected frames text")
    @Test
    void testTrimStackTrace() {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stes = {
                new StackTraceElement("Class1", "method1", "Class1.java", 1),
                new StackTraceElement("Class2", "method2", "Class2.java", 2)
        };
        Jvm.trimStackTrace(sb, stes);
        String trimmed = sb.toString();
        assertTrue(trimmed.contains("Class1.method1"), "trimmed stack trace should contain \"Class1.method1\": " + trimmed);
        assertTrue(trimmed.contains("Class2.method2"), "trimmed stack trace should contain \"Class2.method2\": " + trimmed);
    }

    @DisplayName("usedNativeMemory reports non negative usage value")
    @Test
    void testUsedNativeMemory() {
        long memory = Jvm.usedNativeMemory();
        assertTrue(memory >= 0, "used native memory should be non-negative: memory=" + memory);
    }

    @DisplayName("disableDebugHandler installs null debug handler instance")
    @Test
    void testDisableDebugHandler() {
        Jvm.disableDebugHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.debug()), "debug handler should be set to NullExceptionHandler.NOTHING after disabling");
    }

    @DisplayName("disablePerfHandler installs null perf handler instance")
    @Test
    void testDisablePerfHandler() {
        Jvm.disablePerfHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.perf()), "perf handler should be set to NullExceptionHandler.NOTHING after disabling");
    }

    @DisplayName("disableWarnHandler installs null warn handler instance")
    @Test
    void testDisableWarnHandler() {
        Jvm.disableWarnHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.warn()), "warn handler should be set to NullExceptionHandler.NOTHING after disabling");
    }

    @DisplayName("Set thread local exception handlers applies provided handlers correctly")
    @Test
    void testSetThreadLocalExceptionHandlers() {
        ExceptionHandler mockErrorHandler = mock(ExceptionHandler.class);
        Jvm.setThreadLocalExceptionHandlers(mockErrorHandler, null, null);
        assertSame(mockErrorHandler, ThreadLocalisedExceptionHandler.unwrap(Jvm.error()), "error handler should be set to mockErrorHandler after setting thread-local handlers");
        assertEquals(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.warn()), "warn handler should default to NullExceptionHandler.NOTHING when null is passed");
        assertEquals(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.debug()), "debug handler should default to NullExceptionHandler.NOTHING when null is passed");
    }

    @DisplayName("Is debug enabled and is perf enabled return defaults")
    @Test
    void testIsDebugEnabledAndIsPerfEnabled() {
        assertTrue(Jvm.isDebugEnabled(SomeClass.class), "debug logging should be enabled for SomeClass by default");
        assertTrue(Jvm.isPerfEnabled(SomeClass.class), "performance logging should be enabled for SomeClass by default");
    }

    @DisplayName("getSize returns default value for missing property")
    @Test
    void testGetSize() {
        long defaultValue = 1024;
        assertEquals(defaultValue, Jvm.getSize("nonexistentProperty", defaultValue), "getSize should return default value when property does not exist");
    }

    @DisplayName("CPU class lookup returns non null identifier string")
    @Test
    void testGetCpuClass() {
        String cpuClass = Jvm.getCpuClass();
        assertNotNull(cpuClass, "CPU class string should not be null");
    }

    @DisplayName("CommonInterruptible can be constructed and interrupted")
    @Test
    void testCommonInterruptible() {
        FileChannel mockFileChannel = mock(FileChannel.class);
        Jvm.CommonInterruptible commonInterruptible = new Jvm.CommonInterruptible(getClass(), mockFileChannel);

        commonInterruptible.interrupt();
        assertNotNull(commonInterruptible, "CommonInterruptible instance should not be null after creation and interrupt");
    }

    @DisplayName("Jvm package name returns class package string")
    @Test
    void getPackageName() {
        assertEquals("net.openhft.chronicle.core", Jvm.getPackageName(Jvm.class), "package name for Jvm class should be 'net.openhft.chronicle.core'");
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
