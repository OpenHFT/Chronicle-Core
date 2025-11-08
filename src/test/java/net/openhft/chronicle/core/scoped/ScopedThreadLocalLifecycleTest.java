/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.util.ThreadConfinementAsserter;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ScopedThreadLocalLifecycleTest {

    @Test
    void zeroCapacityIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ScopedThreadLocal<>(() -> new CloseableProbe(1, new AtomicInteger(), new CopyOnWriteArrayList<>()),
                        CloseableProbe::reset,
                        0)
        );
        assertTrue(ex.getMessage().contains("maxInstances"));
    }

    @Test
    void newestResourceIsClosedWhenCapacityExceeded() {
        AtomicInteger idSeq = new AtomicInteger();
        AtomicInteger closedCount = new AtomicInteger();
        List<Integer> closedIds = new CopyOnWriteArrayList<>();

        ScopedThreadLocal<CloseableProbe> pool = new ScopedThreadLocal<>(
                () -> new CloseableProbe(idSeq.incrementAndGet(), closedCount, closedIds),
                CloseableProbe::reset,
                2
        );

        List<Integer> acquired = new ArrayList<>();
        try (ScopedResource<CloseableProbe> r1 = pool.get();
             ScopedResource<CloseableProbe> r2 = pool.get();
             ScopedResource<CloseableProbe> r3 = pool.get()) {
            acquired.add(r1.get().id());
            acquired.add(r2.get().id());
            acquired.add(r3.get().id());
        }

        assertEquals(1, closedCount.get(), "Only one resource should be discarded");
        assertEquals(Collections.singletonList(acquired.get(2)), closedIds, "The most recently created resource must be closed");
    }

    @Test
    void weakReferencePoolRehydratesAfterGc() {
        AtomicInteger idSeq = new AtomicInteger();

        ScopedThreadLocal<CloseableProbe> pool = new ScopedThreadLocal<>(
                () -> new CloseableProbe(idSeq.incrementAndGet(), new AtomicInteger(), new CopyOnWriteArrayList<>()),
                CloseableProbe::reset,
                1,
                true
        );

        WeakReference<CloseableProbe> weakRef;
        int firstId;

        try (ScopedResource<CloseableProbe> resource = pool.get()) {
            firstId = resource.get().id();
            weakRef = new WeakReference<>(resource.get());
        }

        forceGc(weakRef);

        try (ScopedResource<CloseableProbe> resource = pool.get()) {
            assertNotNull(resource.get(), "Resource should be re-created when weak reference was cleared");
            assertNotEquals(firstId, resource.get().id(), "New instance should have a different identifier after GC");
        }
    }

    @Test
    void threadConfinementViolationIsSurfaced() throws Exception {
        ScopedThreadLocal<ConfinementAwareResource> pool = new ScopedThreadLocal<>(
                ConfinementAwareResource::new,
                ConfinementAwareResource::reset,
                2
        );

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (ScopedResource<ConfinementAwareResource> resource = pool.get()) {
            resource.get().touch(); // establishes owning thread

            Future<?> future = executor.submit(resource.get()::touch);
            ExecutionException exception = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
            assertTrue(exception.getCause() instanceof IllegalStateException);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static void acquireAndClose(ScopedThreadLocal<CloseableProbe> pool) {
        try (ScopedResource<CloseableProbe> ignored = pool.get()) {
            // scope closes immediately
        }
    }

    private static void forceGc(WeakReference<?> ref) {
        for (int i = 0; i < 50 && ref.get() != null; i++) {
            System.gc();
            Jvm.pause(50);
        }
        assertNull(ref.get(), "Reference should be cleared after GC");
    }

    private static final class CloseableProbe implements Closeable {
        private final int id;
        private final AtomicInteger closedCount;
        private final List<Integer> closedOrder;
        private volatile boolean closed;

        private CloseableProbe(int id, AtomicInteger closedCount, List<Integer> closedOrder) {
            this.id = id;
            this.closedCount = closedCount;
            this.closedOrder = closedOrder;
        }

        int id() {
            return id;
        }

        void reset() {
            closed = false;
        }

        @Override
        public void close() {
            closedOrder.add(id);
            closedCount.incrementAndGet();
            closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }

    private static final class ConfinementAwareResource implements Closeable {
        private final ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();
        private volatile boolean closed;

        void touch() {
            asserter.assertThreadConfined();
        }

        void reset() {
            // nothing to reset
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }
}
