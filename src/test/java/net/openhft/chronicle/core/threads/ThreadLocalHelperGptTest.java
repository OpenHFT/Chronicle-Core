package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ThreadLocalHelperGptTest {

    @Test
    public void testGetTLWithSupplier() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        String value = ThreadLocalHelper.getTL(threadLocal, () -> "new-value");

        assertNotNull(value);
        assertEquals("new-value", value);
    }

    @Test
    public void testGetSTLWithSupplier() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        String value = ThreadLocalHelper.getSTL(threadLocal, () -> "new-value");

        assertNotNull(value);
        assertEquals("new-value", value);
    }

    @Test
    public void testGetTLWithFunction() {
        ThreadLocal<WeakReference<Integer>> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);
        Integer value = ThreadLocalHelper.getTL(threadLocal, counter, AtomicInteger::incrementAndGet);

        assertNotNull(value);
        assertEquals(1, value);
    }

    @Test
    public void testThreadLocalIsolation() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        String mainThreadValue = ThreadLocalHelper.getTL(threadLocal, () -> "main-thread-value");

        Thread newThread = new Thread(() -> {
            String newThreadValue = ThreadLocalHelper.getTL(threadLocal, () -> "new-thread-value");
            assertEquals("new-thread-value", newThreadValue);
            assertNotSame(mainThreadValue, newThreadValue);
        });

        newThread.start();
        try {
            newThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
