/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.shutdown;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HookletTest {
    @Test
    public void testOnShutdown() {
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
    public void testPriority() {
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
    public void testOf() {
        AtomicBoolean called = new AtomicBoolean(false);
        Runnable hook = () -> called.set(true);
        Hooklet hooklet = Hooklet.of(20, hook);
        assertEquals(20, hooklet.priority(), "hooklet created with of() should use specified priority");
        hooklet.onShutdown();
        assertTrue(called.get(), "hooklet created with of() should execute the provided runnable on shutdown");
    }

    @Test
    public void testCompareTo() {
        Hooklet hooklet1 = Hooklet.of(10, () -> {});
        Hooklet hooklet2 = Hooklet.of(20, () -> {});
        assertTrue(hooklet1.compareTo(hooklet2) < 0, "hooklet with lower priority should compare as less than hooklet with higher priority");
    }

    static class NoOpRunnable implements Runnable {
        @Override
        public void run() {
            // Intentionally empty: used to verify equality/hashCode/toString behaviours
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        Runnable runnable = new NoOpRunnable();
        Hooklet hooklet1 = Hooklet.of(10, runnable);
        Hooklet hooklet2 = Hooklet.of(10, runnable);

        assertEquals(hooklet1, hooklet2, "operation result should equal expected value");
        assertEquals(hooklet1.hashCode(), hooklet2.hashCode(), "equal hooklets should have identical hash codes");
    }

    @Test
    public void testToString() {
        Hooklet hooklet = Hooklet.of(10, () -> {});
        String toStringResult = hooklet.toString();
        assertTrue(toStringResult.startsWith("Hooklet{ priority: 10, identity: "), "toString should start with priority and identity prefix");
        assertTrue(toStringResult.contains("HookletTest"), "toString should contain the test class name in the identity");
    }
}
