/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.ManagedCloseable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

class CloseableAssertionInterruptionTest {
    @BeforeEach
    void enableTracing() {
        assertFalse(Thread.currentThread().isInterrupted(), "test entered interrupted");
        CloseableUtils.enableCloseableTracing();
    }

    @AfterEach
    void restoreFixture() {
        Thread.interrupted();
        CloseableUtils.disableCloseableTracing();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesEntryStatusWhenNothingRemains(boolean initiallyInterrupted) {
        if (initiallyInterrupted)
            Thread.currentThread().interrupt();
        CloseableUtils.assertCloseablesClosed();
        assertEquals(initiallyInterrupted, Thread.currentThread().isInterrupted());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void restoresEntryStatusWhenStateQueryFails(boolean initiallyInterrupted) {
        IllegalStateException failure = new IllegalStateException("state query failed");
        ManagedCloseable resource = new ManagedCloseable() {
            @Override public void close() { }
            @Override public boolean isClosed() { return false; }
            @Override public boolean isClosing() { throw failure; }
        };
        CloseableUtils.add(resource);
        if (initiallyInterrupted)
            Thread.currentThread().interrupt();
        assertSame(failure, assertThrows(IllegalStateException.class, CloseableUtils::assertCloseablesClosed));
        assertEquals(initiallyInterrupted, Thread.currentThread().isInterrupted());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void restoresEntryStatusWhenReportingAnUnclosedResource(boolean initiallyInterrupted) {
        ProbeCloseable resource = new ProbeCloseable();
        CloseableUtils.add(resource);
        if (initiallyInterrupted)
            Thread.currentThread().interrupt();
        AssertionError failure = assertThrows(AssertionError.class, CloseableUtils::assertCloseablesClosed);
        assertEquals("Closeables still open", failure.getMessage());
        assertEquals(1, failure.getSuppressed().length);
        assertTrue(resource.isClosed(), "the existing diagnostic cleanup must still run");
        assertEquals(initiallyInterrupted, Thread.currentThread().isInterrupted());
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void retainsPollingInterruptThroughDiagnosticCleanup(boolean initiallyInterrupted, boolean cleanupThrows) throws Exception {
        CountDownLatch queried = new CountDownLatch(1);
        AtomicBoolean closed = new AtomicBoolean();
        AtomicInteger closeCalls = new AtomicInteger();
        IllegalStateException cleanupFailure = new IllegalStateException("diagnostic cleanup failed");
        ManagedCloseable resource = new ManagedCloseable() {
            @Override public void close() {
                closeCalls.incrementAndGet();
                Thread.interrupted();
                closed.set(true);
                if (cleanupThrows)
                    throw cleanupFailure;
            }
            @Override public boolean isClosed() { return closed.get(); }
            @Override public boolean isClosing() {
                queried.countDown();
                return closed.get();
            }
        };
        CloseableUtils.add(resource);
        Thread waiter = Thread.currentThread();
        CountDownLatch ready = new CountDownLatch(1);
        AtomicBoolean stop = new AtomicBoolean();
        AtomicBoolean interruptedDuringPolling = new AtomicBoolean();
        AtomicReference<Throwable> workerFailure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            ready.countDown();
            try {
                assertTrue(queried.await(5, TimeUnit.SECONDS), "resource was never queried");
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (!stop.get() && System.nanoTime() < deadline) {
                    if (waiter.getState() == Thread.State.TIMED_WAITING) {
                        interruptedDuringPolling.set(true);
                        waiter.interrupt();
                        return;
                    }
                    LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(100));
                }
            } catch (Throwable failure) {
                workerFailure.set(failure);
            }
        }, "assertion-diagnostic-interrupter");
        worker.setDaemon(true);
        worker.start();
        try {
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            if (initiallyInterrupted)
                waiter.interrupt();
            // Keep the resource open throughout polling so the diagnostic close, not the worker, consumes the flag.
            if (cleanupThrows) {
                assertSame(cleanupFailure, assertThrows(IllegalStateException.class, CloseableUtils::assertCloseablesClosed));
            } else {
                AssertionError failure = assertThrows(AssertionError.class, CloseableUtils::assertCloseablesClosed);
                assertEquals("Closeables still open", failure.getMessage());
                assertEquals(1, failure.getSuppressed().length);
            }
            assertTrue(interruptedDuringPolling.get(), "no polling sleep was interrupted");
            assertEquals(1, closeCalls.get(), "diagnostic cleanup must run exactly once");
            assertTrue(closed.get());
            assertTrue(waiter.isInterrupted(), "diagnostic cleanup consumed the remembered polling interrupt");
        } finally {
            stop.set(true);
            queried.countDown();
            Thread.interrupted();
            try {
                worker.join(5000);
            } finally {
                closed.set(true);
                CloseableUtils.unmonitor(resource);
            }
        }
        assertFalse(worker.isAlive(), "interrupter did not stop");
        assertNull(workerFailure.get(), () -> String.valueOf(workerFailure.get()));
    }

    @ParameterizedTest
    @CsvSource({"false, false", "true, false", "false, true", "true, true"})
    void givesBackgroundClosureARealPause(boolean initiallyInterrupted, boolean interruptDuringPause) throws Exception {
        ProbeCloseable resource = new ProbeCloseable();
        CloseableUtils.add(resource);
        Thread waiter = Thread.currentThread();
        CountDownLatch ready = new CountDownLatch(1);
        AtomicBoolean stop = new AtomicBoolean();
        AtomicBoolean observedPause = new AtomicBoolean();
        AtomicReference<Throwable> workerFailure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            ready.countDown();
            try {
                assertTrue(resource.queried.await(5, TimeUnit.SECONDS), "resource was never queried");
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                int minimumQueries = 0;
                boolean interruptionSent = false;
                while (!stop.get() && System.nanoTime() < deadline) {
                    if (resource.queryCount.get() >= minimumQueries && waiter.getState() == Thread.State.TIMED_WAITING) {
                        if (interruptDuringPause && !interruptionSent) {
                            interruptionSent = true;
                            waiter.interrupt();
                            minimumQueries = resource.queryCount.get() + 1;
                            continue;
                        }
                        // After interruption, require another query and another blocking sleep before closing.
                        observedPause.set(true);
                        resource.close();
                        return;
                    }
                    LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(100));
                }
            } catch (Throwable failure) {
                workerFailure.set(failure);
            }
        }, "assertion-grace-observer");
        worker.setDaemon(true);
        worker.start();
        try {
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            if (initiallyInterrupted)
                Thread.currentThread().interrupt();
            assertDoesNotThrow(CloseableUtils::assertCloseablesClosed);
            assertEquals(initiallyInterrupted || interruptDuringPause, Thread.currentThread().isInterrupted());
            assertTrue(observedPause.get(), "state queries ran but the grace-period sleep never blocked");
        } finally {
            Thread.interrupted();
            stop.set(true);
            resource.queried.countDown();
            try {
                worker.join(5000);
            } finally {
                resource.close();
            }
        }
        assertFalse(worker.isAlive(), "observer did not stop");
        assertNull(workerFailure.get(), () -> String.valueOf(workerFailure.get()));
    }

    private static final class ProbeCloseable implements ManagedCloseable {
        private final CountDownLatch queried = new CountDownLatch(1);
        private final AtomicInteger queryCount = new AtomicInteger();
        private volatile boolean closed;

        @Override public void close() { closed = true; }
        @Override public boolean isClosed() { return closed; }
        @Override public boolean isClosing() {
            queryCount.incrementAndGet();
            queried.countDown();
            return closed;
        }
    }
}
