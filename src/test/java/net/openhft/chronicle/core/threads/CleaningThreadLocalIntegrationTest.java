/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.io.AbstractReferenceCounted;
import net.openhft.chronicle.core.io.BackgroundResourceReleaser;
import net.openhft.chronicle.core.io.ReferenceOwner;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleaningThreadLocalIntegrationTest {

    @Test
    void cleanupNonCleaningThreadsHandlesConcurrentCallers() throws Exception {
        int ctls = 4;
        int orphansPerCtl = 64;
        AtomicInteger cleaned = new AtomicInteger();
        List<CleaningThreadLocal<TrackedResource>> locals = new ArrayList<>();
        int expectedOrphans = 0;
        for (int i = 0; i < ctls; i++) {
            CleaningThreadLocal<TrackedResource> ctl = new CleaningThreadLocal<>(TrackedResource::new, resource -> {
                resource.clean();
                cleaned.incrementAndGet();
            }, UnaryOperator.identity(), Boolean.TRUE);
            locals.add(ctl);
            createOrphans(ctl, orphansPerCtl);
            expectedOrphans += trackedEntryCount(ctl);
        }
        assertEquals(ctls * orphansPerCtl, expectedOrphans, "all threads should register as orphans");

        int cleaners = 6;
        ExecutorService executor = Executors.newFixedThreadPool(cleaners);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        for (int i = 0; i < cleaners; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < 32; j++) {
                        CleaningThreadLocal.cleanupNonCleaningThreads();
                    }
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            });
        }

        Thread registrar = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < 32; i++) {
                    new CleaningThreadLocal<>(TrackedResource::new, TrackedResource::clean,
                            UnaryOperator.identity(), Boolean.TRUE);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ctl-registrar");

        registrar.start();
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "cleanup threads timed out");
        registrar.join();

        assertNull(failure.get(), () -> "cleanup should not throw " + failure.get());

        int remaining;
        int attempts = 0;
        do {
            CleaningThreadLocal.cleanupNonCleaningThreads();
            remaining = 0;
            for (CleaningThreadLocal<TrackedResource> ctl : locals) {
                remaining += trackedEntryCount(ctl);
            }
        } while (remaining > 0 && attempts++ < 10);

        assertEquals(0, remaining, "no tracked entries should remain");
        assertEquals(expectedOrphans, cleaned.get(), "every orphan should be cleaned exactly once");
    }

    @Test
    void cleanupTriggersBackgroundReferenceRelease() throws Exception {
        AtomicInteger releases = new AtomicInteger();
        CleaningThreadLocal<CountingReference> ctl = new CleaningThreadLocal<>(
                () -> new CountingReference(releases),
                ref -> ref.releaseLast(ReferenceOwner.INIT),
                UnaryOperator.identity(),
                Boolean.TRUE);

        int orphans = 32;
        createOrphans(ctl, orphans);
        CleaningThreadLocal.cleanupNonCleaningThreads();
        BackgroundResourceReleaser.releasePendingResources();

        assertEquals(orphans, releases.get(), "all reference-counted values should be released once");
    }

    private static void createOrphans(CleaningThreadLocal<?> ctl, int count) throws InterruptedException {
        Thread[] threads = new Thread[count];
        for (int i = 0; i < count; i++) {
            threads[i] = new Thread(ctl::get, "ctl-orphan-" + i);
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }

    private static int trackedEntryCount(CleaningThreadLocal<?> ctl) {
        try {
            Field field = CleaningThreadLocal.class.getDeclaredField("nonCleaningThreadValues");
            field.setAccessible(true);
            Map<?, ?> map = (Map<?, ?>) field.get(ctl);
            return map == null ? 0 : map.size();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static final class TrackedResource {
        private final AtomicBoolean cleaned = new AtomicBoolean();

        void clean() {
            if (!cleaned.compareAndSet(false, true)) {
                throw new AssertionError("Resource cleaned more than once");
            }
        }
    }

    private static final class CountingReference extends AbstractReferenceCounted {
        private final AtomicInteger releaseCounter;
        private final AtomicBoolean released = new AtomicBoolean();

        CountingReference(AtomicInteger releaseCounter) {
            this.releaseCounter = releaseCounter;
        }

        @Override
        protected boolean canReleaseInBackground() {
            return true;
        }

        @Override
        protected void performRelease() {
            if (!released.compareAndSet(false, true)) {
                throw new AssertionError("Reference released more than once");
            }
            releaseCounter.incrementAndGet();
        }
    }
}
