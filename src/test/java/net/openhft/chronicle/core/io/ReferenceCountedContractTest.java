/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Any implementor of {@link ReferenceCounted} should implement a test class
 * that extends this or one of its more specific children
 */
abstract class ReferenceCountedContractTest extends CoreTestCommon {

    /**
     * Create and return a fresh {@link ReferenceCounted} instance for contract testing.
     *
     * @return the instance
     */
    protected abstract ReferenceCounted createReferenceCounted();

    @Override
    protected void assertReferencesReleased() {
        // this tests isn't expected to clean up it's resources
    }

    @Test
    @DisplayName("reserve increments reference count for each owner")
    void reserveWillIncrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount(), "reserveWillIncrementReferenceCount: refCount should be 1 initially");

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertEquals(2, referenceCounted.refCount(), "reserveWillIncrementReferenceCount: refCount should be 2 after first reserve");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserve(b);
        assertEquals(3, referenceCounted.refCount(), "reserveWillIncrementReferenceCount: refCount should be 3 after second reserve");
        referenceCounted.release(b);
        referenceCounted.release(a);
        referenceCounted.releaseLast();
    }

    @Test
    @DisplayName("reserve fails after resource is released")
    void reserveWillFailWhenResourceIsAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertThrows(IllegalStateException.class, () -> referenceCounted.reserve(a),
                "reserve should fail after resource is released");
    }

    @Test
    @DisplayName("reserveTransfer keeps reference count unchanged here")
    void reserveTransferWillNotChangeReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertEquals(2, referenceCounted.refCount(), "reserveTransfer: refCount should be 2 before transfer");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserveTransfer(a, b);
        assertEquals(2, referenceCounted.refCount(), "reserveTransfer: refCount should remain 2 after transfer");
        referenceCounted.release(b);
        referenceCounted.releaseLast();
    }

    @Test
    @DisplayName("release decrements reference count for owners")
    void releaseWillDecrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: refCount should be 1 initially");

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertEquals(2, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: refCount should be 2 after first reserve");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserve(b);
        assertEquals(3, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: refCount should be 3 after second reserve");

        referenceCounted.release(b);
        assertEquals(2, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: refCount should be 2 after first release");

        referenceCounted.release(a);
        assertEquals(1, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: refCount should be 1 after second release");

        referenceCounted.releaseLast();
    }

    @Test
    @DisplayName("release fails when resource already released")
    void releaseWillFailWhenResourceAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertThrows(IllegalStateException.class, () -> referenceCounted.release(a),
                "release should fail after resource is already released");
    }

    @Test
    @DisplayName("release reaches zero when INIT released")
    void releaseWillGoAllTheWayToZero() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.release(ReferenceOwner.INIT);
        assertEquals(0, referenceCounted.refCount(), "reference count should be 0 after releasing INIT owner");
    }

    @Test
    @DisplayName("releaseLast decrements reference count to zero")
    void releaseLastWillDecrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount(), "releaseLastWillDecrementReferenceCount: refCount should be 1 initially");

        referenceCounted.releaseLast();
        assertEquals(0, referenceCounted.refCount(), "releaseLastWillDecrementReferenceCount: refCount should be 0 after releaseLast");
    }

    @Test
    @DisplayName("releaseLast fails when reference is not last")
    void releaseLastWillReleaseThenFailWhenReferenceIsNotLast() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);

        assertEquals(2, referenceCounted.refCount(), "releaseLastWillReleaseThenFail: refCount should be 2 after reserve");

        // not reserved is an ISE not a CISE
        assertThrows(IllegalStateException.class, referenceCounted::releaseLast,
                "releaseLast should fail when reference is not last");

        assertEquals(1, referenceCounted.refCount(), "releaseLastWillReleaseThenFail: refCount should be 1 after failed releaseLast attempt");
        referenceCounted.releaseLast(a);
    }

    @Test
    @DisplayName("releaseLast fails after resource already released")
    void releaseLastWillFailWhenResourceAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        assertThrows(IllegalStateException.class, referenceCounted::releaseLast,
                "releaseLast should fail when resource is already released");
    }

    @Test
    @DisplayName("Try reserve returns true when reservation succeeds")
    void tryReserveWillReturnTrueWhenReservationWasSuccessful() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertTrue(referenceCounted.tryReserve(a), "tryReserve should return true when resource is available");
        referenceCounted.release(a);
        referenceCounted.releaseLast();
    }

    @Test
    @DisplayName("Try reserve returns false when resource released")
    void tryReserveWillReturnFalseWhenResourceIsAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertFalse(referenceCounted.tryReserve(a), "tryReserve should return false when resource is already released");
    }

    @Test
    @DisplayName("reference counted implementations remain thread safe")
    void implementationsShouldBeThreadSafe() throws InterruptedException {
        int numThreads = Math.max(3, Math.min(6, Runtime.getRuntime().availableProcessors()));
        int numReferences = 10;
        AtomicBoolean running = new AtomicBoolean(true);
        ReferenceCounted counted = createReferenceCounted();
        if (counted instanceof SingleThreadedChecked) {
            ((SingleThreadedChecked) counted).singleThreadedCheckDisabled(true);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        final List<? extends Future<?>> futures = IntStream.range(0, numThreads)
                .mapToObj(i -> executorService.submit(new ResourceGetter(i, numReferences, running, counted)))
                .collect(Collectors.toList());
        Jvm.pause(500);
        running.set(false);
        futures.forEach(this::getQuietly);
        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("ExecutorService didn't shut down");
        }
        counted.releaseLast();
        assertTrue(counted.refCount() >= 0, "reference count should remain non-negative after concurrent operations");
    }

    @Test
    @DisplayName("listeners are notified on reference add and remove")
    void shouldNotifyListenersWhenReferencesAreAddedAndRemoved() {
        ReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount(), "shouldNotifyListeners: refCount should be 1 initially");
        Set<ReferenceOwner> currentOwners = new HashSet<>();
        Set<ReferenceOwner> untrackedOwners = new HashSet<>();
        rc.addReferenceChangeListener(new ReferenceChangeListener() {

            @Override
            public void onReferenceAdded(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
                currentOwners.add(referenceOwner);
            }

            @Override
            public void onReferenceRemoved(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
                if (!currentOwners.remove(referenceOwner)) {
                    untrackedOwners.add(referenceOwner);
                }
            }

            @Override
            public void onReferenceTransferred(ReferenceCounted referenceCounted, ReferenceOwner fromOwner, ReferenceOwner toOwner) {
                currentOwners.remove(fromOwner);
                currentOwners.add(toOwner);
            }
        });

        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(1, currentOwners.size(), "currentOwners size should be 1 after first reserve");
        verifyContains("currentOwners should include first reserved owner", currentOwners, a);

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals(2, currentOwners.size(), "currentOwners size should be 2 after second reserve");
        verifyContains("currentOwners should include second reserved owner", currentOwners, b);

        rc.release(a);
        assertEquals(1, currentOwners.size(), "currentOwners size should be 1 after first release");
        verifyContains("currentOwners should still include second owner after releasing first", currentOwners, b);

        rc.reserveTransfer(b, a);
        assertEquals(1, currentOwners.size(), "currentOwners size should remain 1 after transfer");
        verifyContains("currentOwners should include transferred owner", currentOwners, a);

        rc.release(a);
        assertEquals(0, currentOwners.size(), "currentOwners size should be 0 after releasing all tracked owners");

        rc.releaseLast(ReferenceOwner.INIT);
        assertEquals(1, untrackedOwners.size(), "untrackedOwners size should be 1 after releasing INIT");
        assertFalse(currentOwners.contains(ReferenceOwner.INIT),
                "INIT owner should not remain tracked: owners=" + currentOwners);
    }

    @Test
    @DisplayName("listener fires when reference is added")
    void whenAReferenceIsAddedTheReferenceChangeListenerShouldFire() {
        ReferenceCounted rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        final CounterReferenceChangeListener referenceChangeListener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(referenceChangeListener);

        rc.reserve(a);
        assertEquals(1, referenceChangeListener.referenceAddedCount, "onReferenceAdded should be called once after reserve");
        rc.release(a);
        rc.releaseLast();
    }

    @Test
    @DisplayName("listener fires when reference is removed")
    void whenAReferenceIsRemovedTheReferenceChangeListenerShouldFire() {
        ReferenceCounted rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        final CounterReferenceChangeListener referenceChangeListener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(referenceChangeListener);

        rc.reserve(a);
        rc.release(a);
        assertEquals(1, referenceChangeListener.referenceRemovedCount, "onReferenceRemoved should be called once after release");
        rc.releaseLast();
    }

    private static void verifyContains(String message, Set<ReferenceOwner> owners, ReferenceOwner expected) {
        assertTrue(owners.contains(expected), message + " [expected=" + expected + ", owners=" + owners + "]");
    }

    @Test
    @DisplayName("listener fires when reference is transferred")
    void referenceChangeListenerShouldFireWhenAReferenceIsTransferred() {
        ReferenceCounted rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");
        final CounterReferenceChangeListener referenceChangeListener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(referenceChangeListener);

        rc.reserve(a);
        rc.reserveTransfer(a, b);
        assertEquals(1, referenceChangeListener.referenceTransferredCount, "reference transfer events should be tracked");
        rc.release(b);
        rc.releaseLast();
    }

    @Test
    @DisplayName("listeners can be added and removed")
    void shouldBeAbleToAddAndRemoveListeners() {
        ReferenceCounted rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");

        CounterReferenceChangeListener listener1 = new CounterReferenceChangeListener();

        rc.addReferenceChangeListener(listener1);
        rc.reserve(a);
        assertEquals(1, listener1.referenceAddedCount, "listener1 should track first reference addition");
        CounterReferenceChangeListener listener2 = new CounterReferenceChangeListener();
        assertEquals(0, listener2.referenceAddedCount, "listener2 should not track events before registration");
        rc.addReferenceChangeListener(listener2);

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals(2, listener1.referenceAddedCount, "listener1 should track both reference additions");
        assertEquals(1, listener2.referenceAddedCount, "listener2 should track second reference addition");
        rc.removeReferenceChangeListener(listener1);
        rc.release(a);
        assertEquals(0, listener1.referenceRemovedCount, "listener1 should not track events after removal");
        assertEquals(1, listener2.referenceRemovedCount, "listener2 should track first reference removal");
        rc.removeReferenceChangeListener(listener2);
        rc.release(b);
        assertEquals(0, listener1.referenceRemovedCount, "listener1 should still not track events after removal");
        assertEquals(1, listener2.referenceRemovedCount, "listener2 should not track second removal after being removed");
        rc.releaseLast();
    }

    private void getQuietly(Future<?> future) {
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Jvm.error().on(ReferenceCountedContractTest.class, "Exception thrown by acquirer", e);
        }
    }

    static class CounterReferenceChangeListener implements ReferenceChangeListener {
        int referenceAddedCount = 0;
        int referenceRemovedCount = 0;
        int referenceTransferredCount = 0;

        @Override
        public void onReferenceAdded(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
            referenceAddedCount++;
        }

        @Override
        public void onReferenceRemoved(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
            referenceRemovedCount++;
        }

        @Override
        public void onReferenceTransferred(ReferenceCounted referenceCounted, ReferenceOwner fromOwner, ReferenceOwner toOwner) {
            referenceTransferredCount++;
        }
    }

    private static class ResourceGetter implements Runnable {

        private final int id;
        private final AtomicBoolean running;
        private final ReferenceCounted resource;
        private final Reference[] references;

        private ResourceGetter(int id, int numReferences, AtomicBoolean running, ReferenceCounted resource) {
            this.id = id;
            this.references = new Reference[numReferences];
            this.running = running;
            this.resource = resource;
        }

        @Override
        public void run() {
            int acquired = 0, released = 0;
            while (running.get()) {
                final int i = ThreadLocalRandom.current().nextInt(references.length);
                if (references[i] == null) {
                    references[i] = new Reference(id, acquired, resource);
                    acquired++;
                } else {
                    references[i].release();
                    references[i] = null;
                    released++;
                }
            }
            for (Reference reference : references) {
                if (reference != null) {
                    reference.release();
                    released++;
                }
            }
            Jvm.startup().on(ResourceGetter.class, "Acquired " + acquired + ", released " + released);
        }
    }

    private static class Reference implements ReferenceOwner {

        private final int owner;
        private final int index;
        private final ReferenceCounted resource;

        Reference(int owner, int index, ReferenceCounted resource) {
            this.owner = owner;
            this.index = index;
            this.resource = resource;
            this.resource.reserve(this);
        }

        void release() {
            resource.release(this);
        }

        @Override
        public String referenceName() {
            return String.format("{id=%s, index=%s}", owner, index);
        }
    }
}
