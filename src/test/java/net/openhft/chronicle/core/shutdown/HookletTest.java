/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.shutdown;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicBoolean;

class HookletTest {
    @Test
    @DisplayName("onShutdown callback is invoked on shutdown")
    void testOnShutdown() {
        AtomicBoolean called = new AtomicBoolean(false);
        Hooklet hooklet = new Hooklet() {
            @Override
            public void onShutdown() {
                called.set(true);
            }
            @Override
            public int priority() {
                return 0;
            }
        };
        hooklet.onShutdown();
        assertTrue(called.get(), "onShutdown callback should be invoked when hooklet is triggered");
    }

    @Test
    @DisplayName("Priority returns configured hooklet setting value")
    void testPriority() {
        Hooklet hooklet = new Hooklet() {
            @Override
            public void onShutdown() {
                // Intentionally left empty: this variant exercises priority path only
            }
            @Override
            public int priority() {
                return 10;
            }
        };
        assertEquals(10, hooklet.priority(), "hooklet priority should return the configured value");
    }

    @Test
    @DisplayName("Hooklet.of creates hooklet with priority and runnable")
    void testOf() {
        AtomicBoolean called = new AtomicBoolean(false);
        Runnable hook = () -> called.set(true);
        Hooklet hooklet = Hooklet.of(20, hook);
        assertEquals(20, hooklet.priority(), "hooklet created with of() should use specified priority");
        hooklet.onShutdown();
        assertTrue(called.get(), "hooklet created with of() should execute the provided runnable on shutdown");
    }

    @Test
    @DisplayName("Compare to orders hooklets by priority")
    void testCompareTo() {
        Hooklet hooklet1 = Hooklet.of(10, () -> {});
        Hooklet hooklet2 = Hooklet.of(20, () -> {});
        int comparison = hooklet1.compareTo(hooklet2);
        assertTrue(comparison < 0, "compareTo should be negative for lower priority: comparison=" + comparison);
    }

    static class NoOpRunnable implements Runnable {
        @Override
        public void run() {
            // Intentionally empty: used to verify equality/hashCode/toString behaviours
        }
    }

    @Test
    @DisplayName("equals and hashCode reflect priority and runnable")
    void testEqualsAndHashCode() {
        Runnable runnable = new NoOpRunnable();
        Hooklet hooklet1 = Hooklet.of(10, runnable);
        Hooklet hooklet2 = Hooklet.of(10, runnable);

        assertEquals(hooklet1, hooklet2, "hooklets with identical priority and runnable should be equal");
        assertEquals(hooklet1.hashCode(), hooklet2.hashCode(), "equal hooklets should have identical hash codes");
    }

    @Test
    @DisplayName("toString includes priority and identity information")
    void testToString() {
        Hooklet hooklet = Hooklet.of(10, () -> {});
        String toStringResult = hooklet.toString();
        assertTrue(toStringResult.startsWith("Hooklet{ priority: 10, identity: "),
                "toString should start with priority and identity prefix: " + toStringResult);
        assertTrue(toStringResult.contains("HookletTest"),
                "toString should contain \"HookletTest\": " + toStringResult);
    }
}
