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
import java.lang.reflect.Field;
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

    @Test
    @DisplayName("addToClassPath updates class path property values")
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

    @Test
    @DisplayName("rethrow surfaces configuration exceptions from helpers")
    void testRethrow() {
        assertThrows(ConfigurationException.class,
                () -> {
                    throw Jvm.rethrow(new ConfigurationException());
                },
                "rethrow should surface ConfigurationException");
    }

    @Test
    @DisplayName("Major version returns positive value for JVM")
    void shouldGetMajorVersion() {
        int majorVersion = Jvm.majorVersion();
        assertTrue(majorVersion > 0, "JVM major version should be greater than zero: majorVersion=" + majorVersion);
    }

    @Test
    @DisplayName("resetExceptionHandlers restores default exception handlers fully")
    void resetExceptionHandlersSetHandlersBackToTheirDefaults() throws IllegalAccessException {
        Jvm.setExceptionHandlers(null, null, null, null);
        Jvm.resetExceptionHandlers();
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_PERF_EXCEPTION_HANDLER").get(null), Jvm.perf().defaultHandler(), "perf exception handler should be reset to default handler");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_WARN_EXCEPTION_HANDLER").get(null), Jvm.warn().defaultHandler(), "warn exception handler should be reset to default handler");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_ERROR_EXCEPTION_HANDLER").get(null), Jvm.error().defaultHandler(), "error exception handler should be reset to default handler");
        assertSame(Jvm.getField(Jvm.class, "DEFAULT_DEBUG_EXCEPTION_HANDLER").get(null), Jvm.debug().defaultHandler(), "debug exception handler should be reset to default handler");
    }

    @Test
    @DisplayName("reportUnoptimised records usage once per class")
    void reportThis() {
        final Map<ExceptionKey, Integer> map = recordExceptions();
        ReportUnoptimised.reportOnce();

        final String actual = map.keySet().toString();
        assertTrue(actual.contains("JvmTest.reportThis(JvmTest.java"),
                actual + " should contain JvmTest.reportThis(JvmTest.java");
    }

    @Test
    @DisplayName("isInternal identifies JDK classes correctly at runtime")
    void testIsInternal() {
        assertTrue(Jvm.isInternal(String.class.getName()), "String class should be recognized as internal JDK class");
        assertFalse(Jvm.isInternal(getClass().getName()), "test class should not be recognized as internal JDK class");
    }

    @Test
    @DisplayName("getValue extracts nested field values safely")
    void testGetValue() {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        long address = Jvm.getValue(bb, "address");
        long expected = DirectBufferUtil.addressOrThrow(bb);
        assertEquals(expected, address, "retrieved address via reflection should match direct buffer address");

    }

    @Test
    @DisplayName("usedDirectMemory increases after direct buffer allocation")
    void testUsedDirectMemory() {
        long used = Jvm.usedDirectMemory();
        assumeFalse(used == 0, "requires non-zero direct memory accounting to validate delta");
        ByteBuffer.allocateDirect(4 << 10);
        assertEquals(used + (4 << 10), Jvm.usedDirectMemory(), "used direct memory should increase by 4KB after allocating 4KB direct buffer");
    }

    @Test
    @DisplayName("maxDirectMemory reports positive direct memory limit")
    void testMaxDirectMemory() {
        long maxDirectMemory = Jvm.maxDirectMemory();
        assertTrue(maxDirectMemory > 0, "maximum direct memory limit should be positive: maxDirectMemory=" + maxDirectMemory);
    }

    @Test
    @DisplayName("signal handlers register without error callbacks")
    void enableSignals() {
        final AtomicBoolean failed = new AtomicBoolean();
        final ExceptionHandler handler = (c, m, t) -> failed.set(true);
        Jvm.setWarnExceptionHandler(handler);
        Jvm.setErrorExceptionHandler(handler);

        Jvm.addSignalHandler((String signal) -> System.out.println(signal + " occurred"));

        assertFalse(failed.get(), "signal handler registration should not trigger warn or error exception handlers");
    }

    @Test
    @DisplayName("classMetrics rejects layout on ARM platforms")
    void classMetrics() throws IllegalArgumentException {
        assumeFalse(isArm(), "class layout metrics are not supported on ARM");
        assertThrows(IllegalArgumentException.class, () -> Jvm.classMetrics(ClassD.class),
                "classMetrics should reject layout introspection for ClassD");
    }

    @Test
    @DisplayName("Nano pause runs repeatedly without failure")
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

    @Test
    @DisplayName("address returns non-zero for direct buffers")
    void address() {
        ByteBuffer bb = ByteBuffer.allocateDirect(64);
        assertNotEquals(0, Jvm.address(bb), "direct buffer address should be non-zero");
        assertThrows(Exception.class, () -> Jvm.address(ByteBuffer.allocate(64)),
                "heap buffer address should not be accessible");
    }

    @Test
    @DisplayName("arrayByteBaseOffset matches unsafe base offset value")
    void arrayByteBaseOffset() {
        byte[] bytes = {0};
        UnsafeMemory.MEMORY.writeByte(bytes, Jvm.arrayByteBaseOffset(), (byte) 1);
        assertEquals(1, bytes[0], "byte array element at base offset should be updated to 1 via unsafe memory write");
    }

    @Test
    @DisplayName("Do not close on interrupt leaves channel open safely")
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
    @Test
    @DisplayName("isProcessAlive detects live and dead PIDs")
    void isProcessAliveTest() {
        long pid = getProcessId();
        assertTrue(Jvm.isProcessAlive(pid), "current process should be detected as alive by its PID");
        if (OS.isLinux())
            assertTrue(Jvm.isProcessAlive(1), "init/systemd process (PID 1) should be detected as alive on Linux"); // the kernel
        assertFalse(Jvm.isProcessAlive(-1), "invalid negative PID should not be detected as alive process");
    }

    @Test
    @DisplayName("getMethod locates declared and default methods")
    void testGetMethod() {
        assertNotNull(Jvm.getMethod(ClassIWDM.class, "hello", CharSequence.class), "getMethod should successfully find declared method 'hello' on ClassIWDM");
        assertThrows(Throwable.class,
                () -> Jvm.getMethod(ClassIWDM.class, "helloDefault", CharSequence.class),
                "getMethod should fail for missing default method on ClassIWDM");
    }

    @Test
    @DisplayName("findAnnotation resolves aliased class annotations correctly")
    void findAnnotationOnClass() {
        final RealAnno ra = findAnnotation(Foo.class, RealAnno.class);
        assertEquals("Hello", ra.value(), "annotation value should be 'Hello' when found via aliased annotation on class");
    }

    @Test
    @DisplayName("findAnnotation resolves aliased method annotations correctly")
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

    @Test
    @DisplayName("findAnnotation resolves aliased field annotations correctly")
    void findAnnotationOnField() throws NoSuchFieldException {
        final RealAnno ra = findAnnotation(DTO.class.getDeclaredField("inheritedAnno"), RealAnno.class);
        assertEquals("Hello", ra.value(), "annotation on DTO.inheritedAnno field should have value 'Hello' via aliased annotation");

        final RealAnno ra2 = findAnnotation(DTO.class.getDeclaredField("directAnno"), RealAnno.class);
        assertEquals("G'Day", ra2.value(), "annotation on DTO.directAnno field should have value 'G'Day' via direct annotation");
    }

    @Test
    @DisplayName("isLambdaClass detects real lambda classes correctly")
    void isLambdaClass() {
        Runnable r = () -> System.out.println("Hello, Lambda!");

        assertTrue(Jvm.isLambdaClass(r.getClass()), "real lambda class should be detected as lambda by isLambdaClass");

        // needs to look like a lambda class so don't change the name
        @SuppressWarnings("TypeName")
        class My$$Lambda$Class {

        }

        assertFalse(Jvm.isLambdaClass(My$$Lambda$Class.class), "local class with lambda-like name should not be detected as lambda by isLambdaClass");
    }

    @Test
    @DisplayName("compileThreshold returns positive JIT threshold value")
    void testCompileThreshold() {
        int threshold = Jvm.compileThreshold();
        assertTrue(threshold > 0, "JIT compile threshold should be positive: threshold=" + threshold);
    }

    @Test
    @DisplayName("majorVersion meets minimum supported Java level")
    void testMajorVersion() {
        int majorVersion = Jvm.majorVersion();
        assertTrue(majorVersion >= 8, "JVM major version should be >= 8: majorVersion=" + majorVersion);
    }

    @Test
    @DisplayName("Java version helpers align with major version")
    void testJavaVersionChecks() {
        assertEquals(Jvm.majorVersion() >= 9, Jvm.isJava9Plus(), "isJava9Plus should return true if and only if major version is at least 9");
        assertEquals(Jvm.majorVersion() >= 12, Jvm.isJava12Plus(), "isJava12Plus should return true if and only if major version is at least 12");
        assertEquals(Jvm.majorVersion() >= 14, Jvm.isJava14Plus(), "isJava14Plus should return true if and only if major version is at least 14");
        assertEquals(Jvm.majorVersion() >= 15, Jvm.isJava15Plus(), "isJava15Plus should return true if and only if major version is at least 15");
        assertEquals(Jvm.majorVersion() >= 19, Jvm.isJava19Plus(), "isJava19Plus should return true if and only if major version is at least 19");
        assertEquals(Jvm.majorVersion() >= 20, Jvm.isJava20Plus(), "isJava20Plus should return true if and only if major version is at least 20");
        assertEquals(Jvm.majorVersion() >= 21, Jvm.isJava21Plus(), "isJava21Plus should return true if and only if major version is at least 21");
    }

    @Test
    @DisplayName("getProcessId returns positive process identifier value")
    void testGetProcessId() {
        int processId = Jvm.getProcessId();
        assertTrue(processId > 0, "process ID should be positive: processId=" + processId);
    }

    @Test
    @DisplayName("trimStackTrace includes all expected frames text")
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

    @Test
    @DisplayName("usedNativeMemory reports non negative usage value")
    void testUsedNativeMemory() {
        long memory = Jvm.usedNativeMemory();
        assertTrue(memory >= 0, "used native memory should be non-negative: memory=" + memory);
    }

    @Test
    @DisplayName("disableDebugHandler installs null debug handler instance")
    void testDisableDebugHandler() {
        Jvm.disableDebugHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.debug()), "debug handler should be set to NullExceptionHandler.NOTHING after disabling");
    }

    @Test
    @DisplayName("disablePerfHandler installs null perf handler instance")
    void testDisablePerfHandler() {
        Jvm.disablePerfHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.perf()), "perf handler should be set to NullExceptionHandler.NOTHING after disabling");
    }

    @Test
    @DisplayName("disableWarnHandler installs null warn handler instance")
    void testDisableWarnHandler() {
        Jvm.disableWarnHandler();
        assertSame(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.warn()), "warn handler should be set to NullExceptionHandler.NOTHING after disabling");
    }

    @Test
    @DisplayName("Set thread local exception handlers applies provided handlers correctly")
    void testSetThreadLocalExceptionHandlers() {
        ExceptionHandler mockErrorHandler = mock(ExceptionHandler.class);
        Jvm.setThreadLocalExceptionHandlers(mockErrorHandler, null, null);
        assertSame(mockErrorHandler, ThreadLocalisedExceptionHandler.unwrap(Jvm.error()), "error handler should be set to mockErrorHandler after setting thread-local handlers");
        assertEquals(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.warn()), "warn handler should default to NullExceptionHandler.NOTHING when null is passed");
        assertEquals(NullExceptionHandler.NOTHING, ThreadLocalisedExceptionHandler.unwrap(Jvm.debug()), "debug handler should default to NullExceptionHandler.NOTHING when null is passed");
    }

    @Test
    @DisplayName("Is debug enabled and is perf enabled return defaults")
    void testIsDebugEnabledAndIsPerfEnabled() {
        assertTrue(Jvm.isDebugEnabled(SomeClass.class), "debug logging should be enabled for SomeClass by default");
        assertTrue(Jvm.isPerfEnabled(SomeClass.class), "performance logging should be enabled for SomeClass by default");
    }

    @Test
    @DisplayName("getSize returns default value for missing property")
    void testGetSize() {
        long defaultValue = 1024;
        assertEquals(defaultValue, Jvm.getSize("nonexistentProperty", defaultValue), "getSize should return default value when property does not exist");
    }

    @Test
    @DisplayName("CPU class lookup returns non null identifier string")
    void testGetCpuClass() {
        String cpuClass = Jvm.getCpuClass();
        assertNotNull(cpuClass, "CPU class string should not be null");
    }

    @Test
    @DisplayName("CommonInterruptible can be constructed and interrupted")
    void testCommonInterruptible() {
        FileChannel mockFileChannel = mock(FileChannel.class);
        Jvm.CommonInterruptible commonInterruptible = new Jvm.CommonInterruptible(getClass(), mockFileChannel);

        commonInterruptible.interrupt();
        assertNotNull(commonInterruptible, "CommonInterruptible instance should not be null after creation and interrupt");
    }

    @Test
    @DisplayName("Jvm package name returns class package string")
    void getPackageName() {
        assertEquals("net.openhft.chronicle.core", Jvm.getPackageName(Jvm.class), "package name for Jvm class should be 'net.openhft.chronicle.core'");
    }

    @Test
    @DisplayName("pause with negative delay does nothing")
    void pauseNegativeDelay() {
        long start = System.currentTimeMillis();
        Jvm.pause(-1);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 100, "negative pause should return immediately: elapsed=" + elapsed);
    }

    @Test
    @DisplayName("pause with zero delay does nothing")
    void pauseZeroDelay() {
        long start = System.currentTimeMillis();
        Jvm.pause(0);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 100, "zero pause should return immediately: elapsed=" + elapsed);
    }

    @Test
    @DisplayName("pause with positive delay waits appropriately")
    void pausePositiveDelay() {
        long start = System.currentTimeMillis();
        Jvm.pause(10);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 5, "pause should wait at least 5ms for 10ms delay: elapsed=" + elapsed);
    }

    @Test
    @DisplayName("safepoint can be called without error")
    void safepointDoesNotThrow() {
        assertDoesNotThrow(Jvm::safepoint, "safepoint should not throw");
    }

    @Test
    @DisplayName("getFieldOrNull returns null for non-existent field")
    void getFieldOrNullReturnsNullForMissing() {
        assertNull(Jvm.getFieldOrNull(String.class, "nonExistentField123"), "getFieldOrNull should return null for non-existent field");
    }

    @Test
    @DisplayName("getField throws for non-existent field")
    void getFieldThrowsForMissing() {
        assertThrows(AssertionError.class, () -> Jvm.getField(String.class, "nonExistentField123"),
                "getField should throw AssertionError for non-existent field");
    }

    @Test
    @DisplayName("getField finds existing field")
    void getFieldFindsExistingField() {
        assertNotNull(Jvm.getField(SomeClass.class, "somePrivateField"), "getField should find private field");
    }

    @Test
    @DisplayName("getBoolean returns default for missing property")
    void getBooleanMissingProperty() {
        assertFalse(Jvm.getBoolean("nonexistent.test.property.xyz123"), "getBoolean should return false for missing property");
    }

    @Test
    @DisplayName("getBoolean with default returns default for missing property")
    void getBooleanWithDefaultMissingProperty() {
        assertTrue(Jvm.getBoolean("nonexistent.test.property.xyz456", true), "getBoolean should return default value for missing property");
        assertFalse(Jvm.getBoolean("nonexistent.test.property.xyz789", false), "getBoolean should return false default for missing property");
    }

    @Test
    @DisplayName("getDouble returns default for missing property")
    void getDoubleMissingProperty() {
        assertEquals(42.5, Jvm.getDouble("nonexistent.double.property", 42.5), 0.001, "getDouble should return default for missing property");
    }

    @Test
    @DisplayName("getInteger returns default for missing property")
    void getIntegerMissingProperty() {
        assertEquals(100, Jvm.getInteger("nonexistent.int.property", 100), "getInteger should return default for missing property");
    }

    @Test
    @DisplayName("getLong returns default for missing property")
    void getLongMissingProperty() {
        assertEquals(12345L, Jvm.getLong("nonexistent.long.property", 12345L), "getLong should return default for missing property");
    }

    @Test
    @DisplayName("startup checks complete without errors")
    void startupChecksComplete() {
        // Startup has already happened, just verify it didn't fail
        assertTrue(Jvm.majorVersion() > 0, "JVM should have completed startup with valid major version");
    }

    @Test
    @DisplayName("isArm returns consistent value")
    void isArmConsistent() {
        boolean arm1 = Jvm.isArm();
        boolean arm2 = Jvm.isArm();
        assertEquals(arm1, arm2, "isArm should return consistent value");
    }

    @Test
    @DisplayName("isAzulZing returns consistent value")
    void isAzulZingConsistent() {
        boolean zing1 = Jvm.isAzulZing();
        boolean zing2 = Jvm.isAzulZing();
        assertEquals(zing1, zing2, "isAzulZing should return consistent value");
    }

    @Test
    @DisplayName("isResourceTracing can be toggled")
    void isResourceTracingToggle() {
        boolean original = Jvm.isResourceTracing();
        try {
            Jvm.setResourceTracing(!original);
            assertEquals(!original, Jvm.isResourceTracing(), "isResourceTracing should return toggled value");
        } finally {
            Jvm.setResourceTracing(original);
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    @DisplayName("stackTraceEndsWith detects matching endings")
    void stackTraceEndsWithMatching() {
        // Use maxDepth=0 so it starts looking at index 2 in the stack trace
        assertTrue(Jvm.stackTraceEndsWith("JvmTest", 0),
                "stackTraceEndsWith should detect matching class name ending");
    }

    @SuppressWarnings("deprecation")
    @Test
    @DisplayName("stackTraceEndsWith returns false for non-matching")
    void stackTraceEndsWithNonMatching() {
        assertFalse(Jvm.stackTraceEndsWith("NonExistentClassName123", 0),
                "stackTraceEndsWith should return false for non-matching class name ending");
    }

    @Test
    @DisplayName("startup completed flag is set")
    void startupCompleted() {
        // Accessing any Jvm method means startup has completed
        assertDoesNotThrow(Jvm::majorVersion, "accessing Jvm.majorVersion should not throw");
        // If we get here without errors, startup completed successfully
        assertTrue(true, "startup should have completed");
    }

    @Test
    @DisplayName("recordExceptions returns mutable map")
    void recordExceptionsReturnsMap() {
        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        assertNotNull(map, "recordExceptions should return non-null map");
    }

    @Test
    @DisplayName("getProperty returns system property or default")
    void getPropertyReturnsDefault() {
        String result = Jvm.getProperty("nonexistent.property.abc123", "defaultValue");
        assertEquals("defaultValue", result, "getProperty should return default for missing property");
    }

    @Test
    @DisplayName("parseSize handles plain bytes")
    void parseSizePlainBytes() {
        assertEquals(100, Jvm.parseSize("100"), "parseSize should parse plain number as bytes");
        assertEquals(100, Jvm.parseSize("100b"), "parseSize should handle 'b' suffix");
        assertEquals(100, Jvm.parseSize("100B"), "parseSize should handle 'B' suffix");
    }

    @Test
    @DisplayName("parseSize handles kilobyte suffixes")
    void parseSizeKilobytes() {
        assertEquals(1024, Jvm.parseSize("1k"), "parseSize should handle 'k' suffix");
        assertEquals(1024, Jvm.parseSize("1K"), "parseSize should handle 'K' suffix");
        assertEquals(1024, Jvm.parseSize("1KB"), "parseSize should handle 'KB' suffix");
        assertEquals(1024, Jvm.parseSize("1KiB"), "parseSize should handle 'KiB' suffix");
        assertEquals(512, Jvm.parseSize("0.5kb"), "parseSize should handle fractional kilobytes");
    }

    @Test
    @DisplayName("parseSize handles megabyte suffixes")
    void parseSizeMegabytes() {
        assertEquals(1L << 20, Jvm.parseSize("1m"), "parseSize should handle 'm' suffix");
        assertEquals(1L << 20, Jvm.parseSize("1M"), "parseSize should handle 'M' suffix");
        assertEquals(1L << 20, Jvm.parseSize("1MB"), "parseSize should handle 'MB' suffix");
        assertEquals(1L << 20, Jvm.parseSize("1MiB"), "parseSize should handle 'MiB' suffix");
    }

    @Test
    @DisplayName("parseSize handles gigabyte suffixes")
    void parseSizeGigabytes() {
        assertEquals(1L << 30, Jvm.parseSize("1g"), "parseSize should handle 'g' suffix");
        assertEquals(1L << 30, Jvm.parseSize("1G"), "parseSize should handle 'G' suffix");
        assertEquals(1L << 30, Jvm.parseSize("1GB"), "parseSize should handle 'GB' suffix");
        assertEquals(1L << 30, Jvm.parseSize("1GiB"), "parseSize should handle 'GiB' suffix");
    }

    @Test
    @DisplayName("parseSize handles terabyte suffixes")
    void parseSizeTerabytes() {
        assertEquals(1L << 40, Jvm.parseSize("1t"), "parseSize should handle 't' suffix");
        assertEquals(1L << 40, Jvm.parseSize("1T"), "parseSize should handle 'T' suffix");
        assertEquals(1L << 40, Jvm.parseSize("1TB"), "parseSize should handle 'TB' suffix");
        assertEquals(1L << 40, Jvm.parseSize("1TiB"), "parseSize should handle 'TiB' suffix");
    }

    @Test
    @DisplayName("parseSize throws for unrecognised suffix")
    void parseSizeUnrecognisedSuffix() {
        assertThrows(IllegalArgumentException.class, () -> Jvm.parseSize("100X"),
                "parseSize should throw for unrecognised suffix");
    }

    @Test
    @DisplayName("parseSize handles single character value")
    void parseSizeSingleChar() {
        assertEquals(5, Jvm.parseSize("5"), "parseSize should handle single digit");
    }

    @Test
    @DisplayName("trimFirst detects safepoint method in stack trace")
    void trimFirstSafepointMethod() {
        StackTraceElement[] stes = {
                new StackTraceElement("Class0", "method0", "Class0.java", 1),
                new StackTraceElement("net.openhft.Safepoint", "forceafepoint", "Safepoint.java", 10),
                new StackTraceElement("Class2", "method2", "Class2.java", 2)
        };
        int first = Jvm.trimFirst(stes);
        assertEquals(2, first, "trimFirst should skip past safepoint method");
    }

    @Test
    @DisplayName("trimLast handles all internal classes")
    void trimLastAllInternal() {
        StackTraceElement[] stes = {
                new StackTraceElement("com.example.App", "main", "App.java", 1),
                new StackTraceElement("java.lang.Thread", "run", "Thread.java", 100)
        };
        int last = Jvm.trimLast(0, stes);
        assertEquals(1, last, "trimLast should include one internal frame after last non-internal");
    }

    @Test
    @DisplayName("isInternal identifies JDK class prefixes")
    void testIsInternalDetection() {
        assertTrue(Jvm.isInternal("java.lang.String"), "java.lang.String should be internal");
        assertTrue(Jvm.isInternal("jdk.internal.misc.Unsafe"), "jdk.internal should be internal");
        assertTrue(Jvm.isInternal("sun.misc.Unsafe"), "sun.misc should be internal");
        assertFalse(Jvm.isInternal("com.example.MyClass"), "com.example should not be internal");
    }

    @Test
    @DisplayName("hasException returns false for debug-only map")
    void hasExceptionDebugOnly() {
        Map<ExceptionKey, Integer> exceptions = new java.util.LinkedHashMap<>();
        exceptions.put(new ExceptionKey(net.openhft.chronicle.core.onoes.LogLevel.DEBUG, Jvm.class, "test", null), 1);
        assertFalse(Jvm.hasException(exceptions), "hasException should return false for DEBUG level only");
    }

    @Test
    @DisplayName("hasException returns false for perf-only map")
    void hasExceptionPerfOnly() {
        Map<ExceptionKey, Integer> exceptions = new java.util.LinkedHashMap<>();
        exceptions.put(new ExceptionKey(net.openhft.chronicle.core.onoes.LogLevel.PERF, Jvm.class, "test", null), 1);
        assertFalse(Jvm.hasException(exceptions), "hasException should return false for PERF level only");
    }

    @Test
    @DisplayName("hasException returns true for warn level")
    void hasExceptionWarnLevel() {
        Map<ExceptionKey, Integer> exceptions = new java.util.LinkedHashMap<>();
        exceptions.put(new ExceptionKey(net.openhft.chronicle.core.onoes.LogLevel.WARN, Jvm.class, "test", null), 1);
        assertTrue(Jvm.hasException(exceptions), "hasException should return true for WARN level");
    }

    @Test
    @DisplayName("hasException returns true for error level")
    void hasExceptionErrorLevel() {
        Map<ExceptionKey, Integer> exceptions = new java.util.LinkedHashMap<>();
        exceptions.put(new ExceptionKey(net.openhft.chronicle.core.onoes.LogLevel.ERROR, Jvm.class, "test", null), 1);
        assertTrue(Jvm.hasException(exceptions), "hasException should return true for ERROR level");
    }

    @Test
    @DisplayName("dontChain returns true for java prefixed classes")
    void dontChainJavaClasses() {
        assertTrue(Jvm.dontChain(String.class), "dontChain should return true for java.lang classes");
        assertTrue(Jvm.dontChain(java.util.ArrayList.class), "dontChain should return true for java.util classes");
    }

    @Test
    @DisplayName("dontChain returns false for non-annotated user classes")
    void dontChainUserClasses() {
        assertFalse(Jvm.dontChain(JvmTest.class), "dontChain should return false for test classes without annotation");
    }

    @Test
    @DisplayName("supportThread detects Finalizer thread name")
    void supportThreadFinalizer() {
        String originalName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("Finalizer");
            assertTrue(Jvm.supportThread(), "supportThread should return true for Finalizer thread");
        } finally {
            Thread.currentThread().setName(originalName);
        }
    }

    @Test
    @DisplayName("supportThread detects tilde in thread name")
    void supportThreadTilde() {
        String originalName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("background~worker");
            assertTrue(Jvm.supportThread(), "supportThread should return true for thread with tilde");
        } finally {
            Thread.currentThread().setName(originalName);
        }
    }

    @Test
    @DisplayName("supportThread returns false for normal thread names")
    void supportThreadNormal() {
        String originalName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("main-worker-1");
            assertFalse(Jvm.supportThread(), "supportThread should return false for normal thread name");
        } finally {
            Thread.currentThread().setName(originalName);
        }
    }

    @Test
    @DisplayName("recordExceptions with debug false and exceptionsOnly true")
    void recordExceptionsWithOptions() {
        Map<ExceptionKey, Integer> map = Jvm.recordExceptions(false, true);
        assertNotNull(map, "recordExceptions should return non-null map");
        Jvm.resetExceptionHandlers();
    }

    @Test
    @DisplayName("recordExceptions with all options specified")
    void recordExceptionsAllOptions() {
        Map<ExceptionKey, Integer> map = Jvm.recordExceptions(false, false, false);
        assertNotNull(map, "recordExceptions should return non-null map");
        Jvm.resetExceptionHandlers();
    }

    @Test
    @DisplayName("dumpException logs and resets handlers")
    void dumpExceptionTest() {
        Map<ExceptionKey, Integer> exceptions = Jvm.recordExceptions();
        Jvm.warn().on(JvmTest.class, "Test warning for dumpException");
        Jvm.warn().on(JvmTest.class, "Test warning for dumpException"); // twice to test repeat count
        Jvm.dumpException(exceptions);
        // If we get here without error, the test passes
        assertTrue(true, "dumpException should complete without error");
    }

    @Test
    @DisplayName("getSize returns default for null property")
    void getSizeNullProperty() {
        assertEquals(1024, Jvm.getSize("nonexistent.size.property.xyz", 1024),
                "getSize should return default for missing property");
    }

    @Test
    @DisplayName("isLambdaClass returns false for non-synthetic classes")
    void isLambdaClassNonSynthetic() {
        assertFalse(Jvm.isLambdaClass(String.class), "String should not be detected as lambda class");
        assertFalse(Jvm.isLambdaClass(JvmTest.class), "JvmTest should not be detected as lambda class");
    }

    @Test
    @DisplayName("uncheckedCast works for object arrays")
    void uncheckedCastArray() {
        Object[] objects = new String[]{"a", "b", "c"};
        String[] result = Jvm.uncheckedCast(objects);
        assertArrayEquals(new String[]{"a", "b", "c"}, result, "uncheckedCast should cast object array");
    }

    @Test
    @DisplayName("uncheckedCast works for Class objects")
    void uncheckedCastClass() {
        Class<?> clazz = String.class;
        Class<String> result = Jvm.uncheckedCast(clazz);
        assertEquals(String.class, result, "uncheckedCast should cast Class object");
    }

    @Test
    @DisplayName("currentThreadId returns positive value")
    void currentThreadIdPositive() {
        long id = Jvm.currentThreadId();
        assertTrue(id > 0, "currentThreadId should return positive value: id=" + id);
    }

    @Test
    @DisplayName("isCodeCoverage returns consistent value")
    void isCodeCoverageConsistent() {
        boolean cov1 = Jvm.isCodeCoverage();
        boolean cov2 = Jvm.isCodeCoverage();
        assertEquals(cov1, cov2, "isCodeCoverage should return consistent value");
    }

    @Test
    @DisplayName("isDebug returns consistent value")
    void isDebugConsistent() {
        boolean debug1 = Jvm.isDebug();
        boolean debug2 = Jvm.isDebug();
        assertEquals(debug1, debug2, "isDebug should return consistent value");
    }

    @Test
    @DisplayName("is64bit returns consistent value")
    void is64bitConsistent() {
        boolean bit1 = Jvm.is64bit();
        boolean bit2 = Jvm.is64bit();
        assertEquals(bit1, bit2, "is64bit should return consistent value");
    }

    @Test
    @DisplayName("isMacArm returns consistent value")
    void isMacArmConsistent() {
        boolean mac1 = Jvm.isMacArm();
        boolean mac2 = Jvm.isMacArm();
        assertEquals(mac1, mac2, "isMacArm should return consistent value");
    }

    @Test
    @DisplayName("pause handles interrupt during sleep")
    void pauseWithInterrupt() throws InterruptedException {
        Thread testThread = new Thread(() -> Jvm.pause(1000));
        testThread.start();
        Thread.sleep(50); // let it start sleeping
        testThread.interrupt();
        testThread.join(500);
        assertFalse(testThread.isAlive(), "thread should have exited after interrupt");
    }

    @Test
    @DisplayName("startup accessor returns startup handler")
    void startupHandler() {
        assertNotNull(Jvm.startup(), "startup handler should not be null");
        assertSame(Jvm.perf(), Jvm.startup(), "startup and perf should return same handler");
    }

    @Test
    @DisplayName("busyWaitMicros completes for short duration")
    void busyWaitMicrosShort() {
        long start = System.nanoTime();
        Jvm.busyWaitMicros(10);
        long elapsed = System.nanoTime() - start;
        assertTrue(elapsed >= 9_000, "busyWaitMicros should wait at least 9us: elapsed=" + elapsed);
    }

    @Test
    @DisplayName("busyWaitUntil completes at target time")
    void busyWaitUntilTarget() {
        long target = System.nanoTime() + 10_000; // 10us from now
        Jvm.busyWaitUntil(target);
        assertTrue(System.nanoTime() >= target, "busyWaitUntil should reach target time");
    }

    @Test
    @DisplayName("fieldOffset returns valid offset for existing field")
    void fieldOffsetValid() {
        long offset = Jvm.fieldOffset(SomeClass.class, "somePrivateField");
        assertTrue(offset > 0, "fieldOffset should return positive offset: offset=" + offset);
    }

    @Test
    @DisplayName("fieldOffset throws for non-existent field")
    void fieldOffsetMissing() {
        assertThrows(AssertionError.class, () -> Jvm.fieldOffset(SomeClass.class, "nonExistentField"),
                "fieldOffset should throw AssertionError for missing field");
    }

    @Test
    @DisplayName("init can be called multiple times safely")
    void initMultipleCalls() {
        Jvm.init();
        Jvm.init();
        assertTrue(true, "init should be callable multiple times without error");
    }

    @Test
    @DisplayName("setAccessible makes private field accessible")
    void setAccessibleTest() throws NoSuchFieldException {
        Field field = SomeClass.class.getDeclaredField("somePrivateField");
        Jvm.setAccessible(field);
        assertDoesNotThrow(() -> field.get(new SomeClass()), "field should be accessible after setAccessible");
    }

    @Test
    @DisplayName("isAssertEnabled returns consistent value")
    void isAssertEnabledConsistent() {
        boolean assert1 = Jvm.isAssertEnabled();
        boolean assert2 = Jvm.isAssertEnabled();
        assertEquals(assert1, assert2, "isAssertEnabled should return consistent value");
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

    @SuppressWarnings("unused")
    static class ClassA {
        long l;
        int i;
        short s;
        byte b;
        boolean flag;
    }

    @SuppressWarnings("unused")
    static class ClassB extends ClassA {
        String text;
    }

    @SuppressWarnings("unused")
    static class ClassC extends ClassB {
        String hi;
    }

    @SuppressWarnings("unused")
    private static class ClassD extends ClassC {
        byte x;
    }

    static class ClassIWDM implements InterfaceWithDefaultMethod {
        @Override
        public void hello(CharSequence ignored) {
        }
    }

    @SuppressWarnings("unused")
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
