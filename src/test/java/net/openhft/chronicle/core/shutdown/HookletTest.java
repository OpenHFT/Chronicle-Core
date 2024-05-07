package net.openhft.chronicle.core.shutdown;

import org.junit.Test;
import static org.junit.Assert.*;
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
        assertTrue(called.get());
    }

    @Test
    public void testPriority() {
        Hooklet hooklet = new Hooklet() {
            @Override
            public void onShutdown() {}
            @Override
            public int priority() {
                return 10;
            }
        };
        assertEquals(10, hooklet.priority());
    }

    @Test
    public void testOf() {
        AtomicBoolean called = new AtomicBoolean(false);
        Runnable hook = () -> called.set(true);
        Hooklet hooklet = Hooklet.of(20, hook);
        assertEquals(20, hooklet.priority());
        hooklet.onShutdown();
        assertTrue(called.get());
    }

    @Test
    public void testCompareTo() {
        Hooklet hooklet1 = Hooklet.of(10, () -> {});
        Hooklet hooklet2 = Hooklet.of(20, () -> {});
        assertTrue(hooklet1.compareTo(hooklet2) < 0);
    }

    static class TestRunnable implements Runnable {
        @Override
        public void run() {}
    }

    @Test
    public void testEqualsAndHashCode() {
        Runnable runnable = new TestRunnable();
        Hooklet hooklet1 = Hooklet.of(10, runnable);
        Hooklet hooklet2 = Hooklet.of(10, runnable);

        assertEquals(hooklet1, hooklet2);
        assertEquals(hooklet1.hashCode(), hooklet2.hashCode());
    }

    @Test
    public void testToString() {
        Hooklet hooklet = Hooklet.of(10, () -> {});
        String toStringResult = hooklet.toString();
        assertTrue(toStringResult.startsWith("Hooklet{ priority: 10, identity: "));
        assertTrue(toStringResult.contains("HookletTest"));
    }
}
