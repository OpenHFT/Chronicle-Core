/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class CleaningThreadExplicitCleanupTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void explicitCleanupDoesNotRepeatWhenRemoved(boolean tracking) {
        AtomicInteger cleaned = new AtomicInteger();
        CleaningThreadLocal<Object> local = new CleaningThreadLocal<>(Object::new,
                value -> cleaned.incrementAndGet(), UnaryOperator.identity(), tracking);
        try {
            Object first = local.get();
            CleaningThread.performCleanup(Thread.currentThread(), local);
            assertEquals(1, cleaned.get());
            assertEquals(0, trackedEntries(local));
            local.remove();
            assertEquals(1, cleaned.get(), "remove must not clean an already detached value");
            assertNotSame(first, local.get());
            CleaningThread.performCleanup(Thread.currentThread(), local);
            assertEquals(2, cleaned.get());
            assertEquals(0, trackedEntries(local));
        } finally {
            local.remove();
        }
    }

    @Test
    void nullValueIsRemovedFromBothRegistries() {
        AtomicInteger cleaned = new AtomicInteger();
        CleaningThreadLocal<Object> local = new CleaningThreadLocal<>(() -> null,
                value -> cleaned.incrementAndGet(), UnaryOperator.identity(), true);
        try {
            assertNull(local.get());
            assertEquals(1, trackedEntries(local));
            CleaningThread.performCleanup(Thread.currentThread(), local);
            assertEquals(0, trackedEntries(local));
            assertEquals(0, cleaned.get());
        } finally {
            local.remove();
        }
    }

    @Test
    void cleanupOfQuiescentPeerPreservesCallersValue() throws Exception {
        AtomicInteger cleaned = new AtomicInteger();
        CleaningThreadLocal<Object> local = new CleaningThreadLocal<>(Object::new,
                value -> cleaned.incrementAndGet(), UnaryOperator.identity(), true);
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread peer = new Thread(() -> {
            try {
                local.get();
                ready.countDown();
                if (!finish.await(10, TimeUnit.SECONDS))
                    throw new AssertionError("owner did not release peer");
                local.remove();
            } catch (Throwable t) {
                failure.set(t);
            }
        }, "quiescent-ctl-owner");
        try {
            Object own = local.get();
            peer.start();
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            CleaningThread.performCleanup(peer);
            assertEquals(1, cleaned.get());
            assertEquals(1, trackedEntries(local), "only the caller's entry remains");
            assertSame(own, local.get());
            CleaningThread.performCleanup(peer);
            finish.countDown();
            peer.join(10_000);
            assertFalse(peer.isAlive());
            assertNull(failure.get());
            CleaningThreadLocal.cleanupNonCleaningThreads();
            assertEquals(1, cleaned.get(), "neither peer removal nor orphan sweeping repeats cleanup");
        } finally {
            finish.countDown();
            peer.join(10_000);
            local.remove();
        }
    }

    @Test
    void repeatedExplicitCleanupDoesNotRetainFixtureSuppliers() {
        AtomicInteger cleaned = new AtomicInteger();
        for (int i = 0; i < 100; i++) {
            byte[] fixture = new byte[1024];
            CleaningThreadLocal<byte[]> local = new CleaningThreadLocal<>(() -> fixture,
                    value -> cleaned.incrementAndGet(), UnaryOperator.identity(), true);
            try {
                assertSame(fixture, local.get());
                CleaningThread.performCleanup(Thread.currentThread(), local);
                assertEquals(0, trackedEntries(local));
            } finally {
                local.remove();
            }
        }
        assertEquals(100, cleaned.get());
    }

    private static int trackedEntries(CleaningThreadLocal<?> local) {
        try {
            Map<?, ?> values = (Map<?, ?>) Jvm.getField(CleaningThreadLocal.class,
                    "nonCleaningThreadValues").get(local);
            return values == null ? 0 : values.size();
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}
