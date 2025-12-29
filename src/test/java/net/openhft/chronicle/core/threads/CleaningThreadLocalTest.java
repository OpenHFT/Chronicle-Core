/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.util.ThrowingConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CleaningThreadLocalTest {

    @DisplayName("testConstructor behaviour under expected input and output conditions")
    @Test
    void testConstructor() {
        Supplier<String> supplier = () -> "test";
        ThrowingConsumer<String, Exception> cleanup = value -> { /* cleanup logic */ };

        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCleanup(supplier, cleanup);

        assertNotNull(ctl, "withCleanup should return non-null CleaningThreadLocal");
    }

    @DisplayName("testWithCloseQuietly behaviour under expected input and output conditions")
    @Test
    void testWithCloseQuietly() {
        Supplier<String> supplier = () -> "test";
        CleaningThreadLocal<String> ctl = CleaningThreadLocal.withCloseQuietly(supplier);

        assertNotNull(ctl, "withCloseQuietly should return non-null CleaningThreadLocal");
    }

    @DisplayName("testResourceCleanup behaviour under expected input and output conditions")
    @Test
    void testResourceCleanup() {
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

    @DisplayName("testThreadSafety behaviour under expected input and output conditions")
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
        assertEquals("test", ctl.get(), "main thread should retain its own thread-local value independent of other threads");
    }

    @DisplayName("testExceptionInCleanup behaviour under expected input and output conditions")
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
        assertDoesNotThrow(ctl::remove, "remove should not throw even when cleanup handler fails");
        // After remove, next get() should re-initialize using supplier
        assertEquals("test", ctl.get(), "get after remove should reinitialize value using supplier despite cleanup exception");
        ctl.remove();
        assertTrue(ran.get(), "cleanup action should have executed despite throwing exception");
    }

    @DisplayName("testThreadSpecificValue behaviour under expected input and output conditions")
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
        assertEquals(0, ctl.get(), "main thread value should remain as initial supplier value unaffected by other threads");
    }

    private void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread join interrupted during test", e);
        }
    }
}
