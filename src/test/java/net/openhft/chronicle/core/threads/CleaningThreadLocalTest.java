/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.test.RecordingRunnable;
import net.openhft.chronicle.core.util.ThrowingConsumer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class CleaningThreadLocalTest {

    @Test
    void testConstructor() {
        Supplier<String> supplier = () -> "test";
        ThrowingConsumer<String, Exception> cleanup = value -> { /* cleanup logic */ };

        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCleanup(supplier, cleanup);

        assertNotNull(ctl);
    }

    @Test
    void testWithCloseQuietly() {
        Supplier<String> supplier = () -> "test";
        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCloseQuietly(supplier);

        assertNotNull(ctl);
    }

    @Test
    void testResourceCleanup() {
        RecordingRunnable cleanupAction = new RecordingRunnable();
        CleaningThreadLocal<Runnable> ctl = CleaningThreadLocal.withCleanup(() -> cleanupAction, Runnable::run);

        Thread thread = new Thread(() -> {
            ctl.set(cleanupAction);
            ctl.remove();
        });
        thread.start();
        joinThread(thread);

        assertEquals(1, cleanupAction.runCount());
    }

    @Test
    void testThreadSafety() throws InterruptedException {
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
    void testExceptionInCleanup() {
        Supplier<String> supplier = () -> "test";
        AtomicBoolean ran = new AtomicBoolean(false);
        ThrowingConsumer<String, Exception> cleanup = value -> {
            if (ran.get()) return;
            ran.set(true);
            throw new RuntimeException("Cleanup failed");
        };
        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCleanup(supplier, cleanup);
        ctl.set("temp");
        assertDoesNotThrow(ctl::remove);
        // After remove, next get() should re-initialize using supplier
        assertEquals("test", ctl.get());
        ctl.remove();
        assertTrue(ran.get());
    }

    @Test
    void testThreadSpecificValue() {
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
