/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.threads.CleaningThread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static net.openhft.chronicle.core.io.Closeable.closeQuietly;
import static org.junit.jupiter.api.Assertions.*;

public class ScopedThreadLocalTest extends CoreTestCommon {

    private static final int MAX_INSTANCES = 3;

    private ScopedThreadLocal<AtomicLong> scopedThreadLocal;

    @BeforeEach
    public void createSTL() {
        scopedThreadLocal = new ScopedThreadLocal<>(AtomicLong::new, al -> al.set(0), MAX_INSTANCES);
    }

    @Test
    public void warningWillBeDisplayedWhenWeUseMoreThanMaxInstances() {
        expectException("Pool capacity exceeded, consider increasing maxInstances, maxInstances=3");
        ArrayList<ScopedResource<AtomicLong>> allLongs = new ArrayList<>();
        for (int i = 0; i < MAX_INSTANCES + 1; i++) {
            allLongs.add(scopedThreadLocal.get());
        }
        assertEquals(MAX_INSTANCES + 1, allLongs.size(), "acquired resources count should exceed max instances limit");
        closeQuietly(allLongs);
    }

    @Test
    public void nestedCallsWillGetDifferentResources() {
        try (ScopedResource<AtomicLong> l1 = scopedThreadLocal.get()) {
            l1.get().set(123);
            try (ScopedResource<AtomicLong> l2 = scopedThreadLocal.get()) {
                l2.get().set(456);
                try (ScopedResource<AtomicLong> l3 = scopedThreadLocal.get()) {
                    l3.get().set(789);
                }
                assertEquals(456L, l2.get().get(), "second nested resource should retain its value after closing third resource");
            }
            assertEquals(123L, l1.get().get(), "first nested resource should retain its value after closing second resource");
        }
    }

    @Test
    public void differentThreadsWillGetDifferentResources() throws InterruptedException {
        Set<Integer> instanceObjectIDs = new HashSet<>();
        final int numThreads = 10;
        for (int i = 0; i < numThreads; i++) {
            final Thread thread = new CleaningThread(() -> {
                try (final ScopedResource<AtomicLong> resource = scopedThreadLocal.get()) {
                    if (resource == null) {
                        throw new IllegalStateException();
                    }
                    final int e = System.identityHashCode(resource.get());
                    instanceObjectIDs.add(e);
                } catch (Exception e) {
                    Jvm.error().on(ScopedThreadLocalTest.class, e);
                }
            });
            thread.start();
            thread.join();
        }
        assertEquals(numThreads, instanceObjectIDs.size(), "each thread should receive a unique resource instance");
    }

    @Test
    public void onAcquireIsPerformedBeforeEachAcquisition() {
        int objectId;
        try (ScopedResource<AtomicLong> l1 = scopedThreadLocal.get()) {
            l1.get().set(123);
            objectId = System.identityHashCode(l1.get());
        }
        try (ScopedResource<AtomicLong> l1 = scopedThreadLocal.get()) {
            assertEquals(0L, l1.get().get(), "resource value should be reset to 0 by onAcquire callback");
            assertEquals(objectId, System.identityHashCode(l1.get()), "same resource instance should be reused after release");
        }
    }

    @Test
    public void cleaningThreadWillCloseResources() throws InterruptedException {
        List<CloseableResource> allResources = new ArrayList<>();
        ScopedThreadLocal<CloseableResource> stl = new ScopedThreadLocal<>(() -> {
            CloseableResource cr = new CloseableResource();
            allResources.add(cr);
            return cr;
        }, 2);
        final CleaningThread cleaningThread = new CleaningThread(() -> {
            try (final ScopedResource<CloseableResource> cr1 = stl.get();
                 final ScopedResource<CloseableResource> cr2 = stl.get()) {
                // Create two resources, do nothing
                assertNotNull(cr1, "first scoped resource should be non-null");
                assertNotNull(cr2, "second scoped resource should be non-null");
            }
            // None should be closed
            assertTrue(allResources.stream().noneMatch(cr -> cr.closed), "resources should remain open while thread is active");
        });
        cleaningThread.start();
        cleaningThread.join();
        // All should be closed
        assertEquals(2, allResources.size(), "two resources should have been created");
        assertTrue(allResources.stream().allMatch(cr -> cr.closed), "all resources should be closed after thread completes");
    }

    @Test
    public void whenOverflowOccursNewestInstanceIsDiscarded() {
        expectException("Pool capacity exceeded, consider increasing maxInstances, maxInstances=3");
        AtomicInteger values = new AtomicInteger(0);
        ScopedThreadLocal<Integer> ints = new ScopedThreadLocal<>(values::getAndIncrement, i -> {
        }, MAX_INSTANCES);

        // Should get 0,1,2,3 on the first excessive acquire
        assertEquals(new HashSet<>(Arrays.asList(0, 1, 2, 3)), retrieveAndReturnNValues(MAX_INSTANCES + 1, ints), "first overflow should return pooled instances plus one new instance");

        // Should get 0,1,2,4,5 on the next excessive acquire
        assertEquals(new HashSet<>(Arrays.asList(0, 1, 2, 4, 5)), retrieveAndReturnNValues(MAX_INSTANCES + 2, ints), "second overflow should discard newest instances and create new ones");

        // Should get 0,1,2,6,7 on the next excessive acquire
        assertEquals(new HashSet<>(Arrays.asList(0, 1, 2, 6, 7)), retrieveAndReturnNValues(MAX_INSTANCES + 2, ints), "third overflow should continue discarding newest instances");
    }

    private Set<Integer> retrieveAndReturnNValues(int numberToRetrieve, ScopedThreadLocal<Integer> scopedInts) {
        Set<Integer> values = new HashSet<>();
        retrieveAndRecord(values, scopedInts, numberToRetrieve);
        return values;
    }

    private void retrieveAndRecord(Set<Integer> values, ScopedThreadLocal<Integer> scopedInts, int depth) {
        if (depth > 0) {
            try (final ScopedResource<Integer> isr = scopedInts.get()) {
                values.add(isr.get());
                retrieveAndRecord(values, scopedInts, depth - 1);
            }
        }
    }

    private static class CloseableResource implements Closeable {

        private boolean closed = false;

        @Override
        public void close() {
            closed = true;
        }
    }
}
