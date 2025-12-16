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
        assertTrue(called.get(), "testOnShutdown: L25");
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
        assertEquals(10, hooklet.priority(), "testPriority: L40");
    }

    @Test
    public void testOf() {
        AtomicBoolean called = new AtomicBoolean(false);
        Runnable hook = () -> called.set(true);
        Hooklet hooklet = Hooklet.of(20, hook);
        assertEquals(20, hooklet.priority(), "testOf: L48");
        hooklet.onShutdown();
        assertTrue(called.get(), "testOf: L50");
    }

    @Test
    public void testCompareTo() {
        Hooklet hooklet1 = Hooklet.of(10, () -> {});
        Hooklet hooklet2 = Hooklet.of(20, () -> {});
        assertTrue(hooklet1.compareTo(hooklet2) < 0, "testCompareTo: L57");
    }

    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    static class TestRunnable implements Runnable {
        @Override
        public void run() {
            // Intentionally empty: used to verify equality/hashCode/toString behaviours
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        Runnable runnable = new TestRunnable();
        Hooklet hooklet1 = Hooklet.of(10, runnable);
        Hooklet hooklet2 = Hooklet.of(10, runnable);

        assertEquals(hooklet1, hooklet2, "testEqualsAndHashCode: L74");
        assertEquals(hooklet1.hashCode(), hooklet2.hashCode(), "testEqualsAndHashCode: L75");
    }

    @Test
    public void testToString() {
        Hooklet hooklet = Hooklet.of(10, () -> {});
        String toStringResult = hooklet.toString();
        assertTrue(toStringResult.startsWith("Hooklet{ priority: 10, identity: "), "testToString: L82");
        assertTrue(toStringResult.contains("HookletTest"), "testToString: L83");
    }
}
