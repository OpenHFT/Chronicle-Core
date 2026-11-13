/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService.Impact;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionBasedByteBufferCleanerServiceTest {

    private final ReflectionBasedByteBufferCleanerService cleanerService = new ReflectionBasedByteBufferCleanerService();

    @Test
    void cleanShouldWorkOnCurrentJvm() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        String javaFamily = Jvm.isJava9Plus() ? "Java 9+" : "Java 8";
        assertDoesNotThrow(() -> cleanerService.clean(buffer), "Cleaning a direct buffer should not throw on " + javaFamily);
    }

    @Test
    void impactShouldReturnValidImpact() {
        Impact impact = cleanerService.impact();
        assertTrue(impact == Impact.SOME_IMPACT || impact == Impact.UNAVAILABLE, "Impact should be either SOME_IMPACT or UNAVAILABLE");
    }

    @Test
    void adviceDistinguishesJava8AndMissingJdkMethodsFromModuleAccess() {
        String java8 = ReflectionBasedByteBufferCleanerService.accessAdvice(false, new IllegalAccessException());
        assertTrue(java8.contains("Java 8"));
        assertFalse(java8.contains("--add-exports"));
        String missing = ReflectionBasedByteBufferCleanerService.accessAdvice(true, new NoSuchMethodException());
        assertTrue(missing.contains("compatibility"));
        assertFalse(missing.contains("--add-exports"));
        for (Throwable denied : new Throwable[]{new IllegalAccessException(), new IllegalAccessError()}) {
            String advice = ReflectionBasedByteBufferCleanerService.accessAdvice(true, denied);
            assertTrue(advice.contains("--add-exports java.base/jdk.internal.ref=ALL-UNNAMED"));
            assertTrue(advice.contains("--add-exports java.base/sun.nio.ch=ALL-UNNAMED"));
        }
    }
}
