/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner;
import net.openhft.chronicle.core.cleaner.testimpl.SomeImpactCleaner;
import net.openhft.chronicle.core.cleaner.testimpl.WorkingCleaner;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import net.openhft.chronicle.core.onoes.LogLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CleanerServiceLocatorTest {

    private static void resetLocator() throws Exception {
        Field init = CleanerServiceLocator.class.getDeclaredField("initialised");
        init.setAccessible(true);
        init.setBoolean(null, false);
        Field inst = CleanerServiceLocator.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, null);
    }

    /** Counts recorded {@link CleanerServiceLocator} log lines at {@code level} whose message contains {@code needle}. */
    private static int countFrom(Map<ExceptionKey, Integer> recorded, LogLevel level, String needle) {
        return recorded.keySet().stream()
                .filter(k -> k.level == level)
                .filter(k -> k.clazz == CleanerServiceLocator.class)
                .filter(k -> k.message != null && k.message.contains(needle))
                .mapToInt(k -> 1)
                .sum();
    }

    @AfterEach
    void tearDown() throws Exception {
        Jvm.resetExceptionHandlers();
        resetLocator();
    }

    @Test
    void picksAllowedServiceWithLowestImpact() throws Exception {
        resetLocator();
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        ByteBufferCleanerService svc = CleanerServiceLocator.cleanerService();
        assertNotNull(svc);
        // Should be our test implementation from META-INF/services
        assertEquals("net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner", svc.getClass().getName());
        assertEquals(ByteBufferCleanerService.Impact.NO_IMPACT, svc.impact());
        assertTrue(countFrom(recorded, LogLevel.WARN, "leak") >= 1,
                "expected a leak warning for the no-op AllowedCleaner; recorded=" + recorded.keySet());
    }

    @Test
    void noOpCleanerSelectedEmitsLeakWarning() throws Exception {
        resetLocator();
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        ByteBufferCleanerService svc = CleanerServiceLocator.cleanerService();
        assertNotNull(svc);
        assertTrue(countFrom(recorded, LogLevel.WARN, "leak") >= 1,
                "expected a leak warning naming the no-op cleaner; recorded=" + recorded.keySet());
        assertTrue(recorded.keySet().stream()
                        .anyMatch(k -> k.level == LogLevel.WARN && k.message != null
                                && k.message.contains(svc.getClass().getName())),
                "leak warning should name the selected cleaner class");
    }

    @Test
    void verifiedWorkingCleanerLogsAtDebugAndDoesNotWarn() {
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        CleanerServiceLocator.verifySelectedCleaner(new WorkingCleaner(), Jvm::usedDirectMemory);
        assertEquals(0, countFrom(recorded, LogLevel.WARN, "leak"),
                "a cleaner that genuinely frees memory must not warn; recorded=" + recorded.keySet());
        assertTrue(countFrom(recorded, LogLevel.DEBUG, "Selected") >= 1,
                "expected a debug line naming the verified cleaner; recorded=" + recorded.keySet());
    }

    @Test
    void cleanerReportingUnavailableWarnsWithoutProbe() {
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        CleanerServiceLocator.verifySelectedCleaner(new UnavailableCleaner());
        assertTrue(countFrom(recorded, LogLevel.WARN, "leak") >= 1,
                "an UNAVAILABLE cleaner must warn via the short-circuit; recorded=" + recorded.keySet());
    }

    @Test
    void probeExceptionDoesNotBreakSelection() {
        assertDoesNotThrow(() ->
                CleanerServiceLocator.verifySelectedCleaner(new ThrowingCleaner(), Jvm::usedDirectMemory));
    }

    @Test
    void noFalseWarningWhenAccountingUnavailable() {
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        final LongSupplier accountingOff = () -> 0L; // 0 == direct-memory accounting unavailable
        CleanerServiceLocator.verifySelectedCleaner(new AllowedCleaner(), accountingOff);
        assertEquals(0, countFrom(recorded, LogLevel.WARN, "leak"),
                "must not warn when accounting is unavailable; recorded=" + recorded.keySet());
    }

    @Test
    void allowedCleanerProbedDirectlyWarns() {
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        CleanerServiceLocator.verifySelectedCleaner(new AllowedCleaner(), Jvm::usedDirectMemory);
        assertTrue(countFrom(recorded, LogLevel.WARN, "leak") >= 1,
                "AllowedCleaner (NO_IMPACT, no-op) must be detected as leaking; recorded=" + recorded.keySet());
    }

    @Test
    void someImpactNoOpCleanerStillWarns() {
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        // the probe ignores self-reported impact, so a SOME_IMPACT no-op is still caught
        CleanerServiceLocator.verifySelectedCleaner(new SomeImpactCleaner(), Jvm::usedDirectMemory);
        assertTrue(countFrom(recorded, LogLevel.WARN, "leak") >= 1,
                "a SOME_IMPACT cleaner that frees nothing must still warn; recorded=" + recorded.keySet());
    }

    @Test
    void someImpactWorkingCleanerDoesNotWarn() {
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        CleanerServiceLocator.verifySelectedCleaner(new SomeImpactWorkingCleaner(), Jvm::usedDirectMemory);
        assertEquals(0, countFrom(recorded, LogLevel.WARN, "leak"),
                "a working SOME_IMPACT cleaner must not warn; recorded=" + recorded.keySet());
        assertTrue(countFrom(recorded, LogLevel.DEBUG, "Selected") >= 1,
                "expected a debug line naming the verified cleaner; recorded=" + recorded.keySet());
    }

    @Test
    void realJdk9CleanerVerifiedSilent() {
        assumeTrue(Jvm.isJava9Plus()); // Jdk9 cleaner only frees on Java 9+
        final Map<ExceptionKey, Integer> recorded = Jvm.recordExceptions();
        CleanerServiceLocator.verifySelectedCleaner(new Jdk9ByteBufferCleanerService(), Jvm::usedDirectMemory);
        assertEquals(0, countFrom(recorded, LogLevel.WARN, "leak"),
                "the real JDK9 cleaner must not warn; recorded=" + recorded.keySet());
        assertTrue(countFrom(recorded, LogLevel.DEBUG, "Selected") >= 1,
                "expected a debug line for the verified JDK9 cleaner; recorded=" + recorded.keySet());
    }

    /** Reports SOME_IMPACT but genuinely frees (delegates to WorkingCleaner). */
    private static final class SomeImpactWorkingCleaner implements ByteBufferCleanerService {
        private static final WorkingCleaner DELEGATE = new WorkingCleaner();

        @Override
        public Impact impact() {
            return Impact.SOME_IMPACT;
        }

        @Override
        public void clean(ByteBuffer buffer) {
            DELEGATE.clean(buffer);
        }
    }

    /** Reports UNAVAILABLE; clean() does nothing. */
    private static final class UnavailableCleaner implements ByteBufferCleanerService {
        @Override
        public Impact impact() {
            return Impact.UNAVAILABLE;
        }

        @Override
        public void clean(ByteBuffer buffer) {
            // no-op
        }
    }

    /** Reports NO_IMPACT (so it reaches the probe) but throws on clean(). */
    private static final class ThrowingCleaner implements ByteBufferCleanerService {
        @Override
        public Impact impact() {
            return Impact.NO_IMPACT;
        }

        @Override
        public void clean(ByteBuffer buffer) {
            throw new RuntimeException("probe should swallow this");
        }
    }

    @Test
    void fallsBackToReflectionCleanerWhenNoProviders() throws Exception {
        resetLocator();
        final String serviceName = "META-INF/services/" + ByteBufferCleanerService.class.getName();
        ClassLoader parent = CleanerServiceLocator.class.getClassLoader();
        ClassLoader noServiceCL = new ClassLoader(parent) {
            @Override
            public java.net.URL getResource(String name) {
                if (serviceName.equals(name)) return null;
                return super.getResource(name);
            }

            @Override
            public java.util.Enumeration<java.net.URL> getResources(String name) {
                if (serviceName.equals(name)) return java.util.Collections.emptyEnumeration();
                try {
                    return super.getResources(name);
                } catch (java.io.IOException e) {
                    return java.util.Collections.emptyEnumeration();
                }
            }

            @Override
            public java.io.InputStream getResourceAsStream(String name) {
                if (serviceName.equals(name)) return null;
                return super.getResourceAsStream(name);
            }
        };
        Thread current = Thread.currentThread();
        ClassLoader prev = current.getContextClassLoader();
        try {
            current.setContextClassLoader(noServiceCL);
            ByteBufferCleanerService svc = CleanerServiceLocator.cleanerService();
            assertNotNull(svc);
            assertTrue(svc.getClass().getName().contains("internal.cleaner"));
        } finally {
            current.setContextClassLoader(prev);
        }
    }
}
