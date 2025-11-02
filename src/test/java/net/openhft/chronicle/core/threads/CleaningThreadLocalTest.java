/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.util.ThrowingConsumer;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CleaningThreadLocalTest {

    @Test
    public void testConstructor() {
        Supplier<String> supplier = () -> "test";
        ThrowingConsumer<String, Exception> cleanup = value -> { /* cleanup logic */ };

        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCleanup(supplier, cleanup);

        assertNotNull(ctl);
    }

    @Test
    public void testWithCloseQuietly() {
        Supplier<String> supplier = () -> "test";
        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCloseQuietly(supplier);

        assertNotNull(ctl);
    }

    @Test
    public void testResourceCleanup() {
        Runnable cleanupAction = mock(Runnable.class);
        CleaningThreadLocal<Runnable> ctl = CleaningThreadLocal.withCleanup(() -> cleanupAction, Runnable::run);

        Thread thread = new Thread(() -> {
            ctl.set(cleanupAction);
            ctl.remove();
        });
        thread.start();
        joinThread(thread);

        verify(cleanupAction).run();
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        Supplier<String> supplier = () -> "test";
        ThrowingConsumer<String, Exception> cleanup = value -> { /* cleanup logic */ };
        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCleanup(supplier, cleanup);

        Thread t1 = new Thread(() -> ctl.set("Thread 1"));
        Thread t2 = new Thread(() -> ctl.set("Thread 2"));

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        // Main thread value should remain as supplied
        assertEquals("test", ctl.get());
    }

    @Test
    public void testExceptionInCleanup() {
        Supplier<String> supplier = () -> "test";
        ThrowingConsumer<String, Exception> cleanup = value -> {
            throw new RuntimeException("Cleanup failed");
        };
        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCleanup(supplier, cleanup);
        ctl.set("temp");
        assertDoesNotThrow(ctl::remove);
        // After remove, next get() should re-initialize using supplier
        assertEquals("test", ctl.get());
    }

    @Test
    public void testThreadSpecificValue() {
        CleaningThreadLocal<Integer> ctl = CleaningThreadLocal.withCleanup(() -> 0, (value) -> {
        });

        Thread thread1 = new Thread(() -> ctl.set(1));
        Thread thread2 = new Thread(() -> ctl.set(2));
        thread1.start();
        thread2.start();
        joinThread(thread1);
        joinThread(thread2);

        // Call remove in case of non-cleaning threads
        ctl.remove();

        // Assert that the value in main thread is not affected
        assertEquals(0, ctl.get());
    }

    private void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
