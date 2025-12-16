/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
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
public abstract class ReferenceCountedContractTest extends CoreTestCommon {

    /**
     * Create an instance of the {@link ReferenceCounted} under test
     *
     * @return the instance
     */
    protected abstract ReferenceCounted createReferenceCounted();

    @Override
    protected void assertReferencesReleased() {
        // this tests isn't expected to clean up it's resources
    }

    @Test
    public void reserveWillIncrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount(), "reserveWillIncrementReferenceCount: L42");

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertEquals(2, referenceCounted.refCount(), "reserveWillIncrementReferenceCount: L46");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserve(b);
        assertEquals(3, referenceCounted.refCount(), "reserveWillIncrementReferenceCount: L50");
        referenceCounted.release(b);
        referenceCounted.release(a);
        referenceCounted.releaseLast();
    }

    @Test
    public void reserveWillFailWhenResourceIsAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertThrows(IllegalStateException.class, () -> referenceCounted.reserve(a));
    }

    @Test
    public void reserveTransferWillNotChangeReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertEquals(2, referenceCounted.refCount(), "reserveTransferWillNotChangeReferenceCount: L72");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserveTransfer(a, b);
        assertEquals(2, referenceCounted.refCount(), "reserveTransferWillNotChangeReferenceCount: L76");
        referenceCounted.release(b);
        referenceCounted.releaseLast();
    }

    @Test
    public void releaseWillDecrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: L85");

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertEquals(2, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: L89");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserve(b);
        assertEquals(3, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: L93");

        referenceCounted.release(b);
        assertEquals(2, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: L96");

        referenceCounted.release(a);
        assertEquals(1, referenceCounted.refCount(), "releaseWillDecrementReferenceCount: L99");

        referenceCounted.releaseLast();
    }

    @Test
    public void releaseWillFailWhenResourceAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertThrows(IllegalStateException.class, () -> referenceCounted.release(a));
    }

    @Test
    public void releaseWillGoAllTheWayToZero() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.release(ReferenceOwner.INIT);
        assertEquals(0, referenceCounted.refCount(), "releaseWillGoAllTheWayToZero: L119");
    }

    @Test
    public void releaseLastWillDecrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount(), "releaseLastWillDecrementReferenceCount: L126");

        referenceCounted.releaseLast();
        assertEquals(0, referenceCounted.refCount(), "releaseLastWillDecrementReferenceCount: L129");
    }

    @Test
    public void releaseLastWillReleaseThenFailWhenReferenceIsNotLast() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);

        assertEquals(2, referenceCounted.refCount(), "releaseLastWillReleaseThenFailWhenReferenceIsNotLast: L139");

        // not reserved is an ISE not a CISE
        assertThrows(IllegalStateException.class, referenceCounted::releaseLast);

        assertEquals(1, referenceCounted.refCount(), "releaseLastWillReleaseThenFailWhenReferenceIsNotLast: L144");
        referenceCounted.releaseLast(a);
    }

    @Test
    public void releaseLastWillFailWhenResourceAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        assertThrows(IllegalStateException.class, referenceCounted::releaseLast);
    }

    @Test
    public void tryReserveWillReturnTrueWhenReservationWasSuccessful() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertTrue(referenceCounted.tryReserve(a), "tryReserveWillReturnTrueWhenReservationWasSuccessful: L161");
        referenceCounted.release(a);
        referenceCounted.releaseLast();
    }

    @Test
    public void tryReserveWillReturnFalseWhenResourceIsAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertFalse(referenceCounted.tryReserve(a), "tryReserveWillReturnFalseWhenResourceIsAlreadyReleased: L172");
    }

    @Test
    public void implementationsShouldBeThreadSafe() throws InterruptedException {
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
        assertTrue(counted.refCount() >= 0, "implementationsShouldBeThreadSafe: L196");
    }

    @Test
    public void shouldNotifyListenersWhenReferencesAreAddedAndRemoved() {
        ReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount(), "shouldNotifyListenersWhenReferencesAreAddedAndRemoved: L202");
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
        ReferenceOwner b = ReferenceOwner.temporary("b");

        rc.reserve(a);
        assertEquals(1, currentOwners.size(), "shouldNotifyListenersWhenReferencesAreAddedAndRemoved: L230");
        assertContains("currentOwners should include first reserved owner", currentOwners, a);

        rc.reserve(b);
        assertEquals(2, currentOwners.size(), "shouldNotifyListenersWhenReferencesAreAddedAndRemoved: L234");
        assertContains("currentOwners should include second reserved owner", currentOwners, b);

        rc.release(a);
        assertEquals(1, currentOwners.size(), "shouldNotifyListenersWhenReferencesAreAddedAndRemoved: L238");
        assertContains("currentOwners should still include second owner after releasing first", currentOwners, b);

        rc.reserveTransfer(b, a);
        assertEquals(1, currentOwners.size(), "shouldNotifyListenersWhenReferencesAreAddedAndRemoved: L242");
        assertContains("currentOwners should include transferred owner", currentOwners, a);

        rc.release(a);
        assertEquals(0, currentOwners.size(), "shouldNotifyListenersWhenReferencesAreAddedAndRemoved: L246");

        rc.releaseLast(ReferenceOwner.INIT);
        assertEquals(1, untrackedOwners.size(), "shouldNotifyListenersWhenReferencesAreAddedAndRemoved: L249");
        assertFalse(currentOwners.contains(ReferenceOwner.INIT), "INIT owner should not remain tracked");
    }

    @Test
    public void whenAReferenceIsAddedTheReferenceChangeListenerShouldFire() {
        ReferenceCounted rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        final CounterReferenceChangeListener referenceChangeListener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(referenceChangeListener);

        rc.reserve(a);
        assertEquals(1, referenceChangeListener.referenceAddedCount, "whenAReferenceIsAddedTheReferenceChangeListenerShouldFire: L262");
        rc.release(a);
        rc.releaseLast();
    }

    @Test
    public void whenAReferenceIsRemovedTheReferenceChangeListenerShouldFire() {
        ReferenceCounted rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        final CounterReferenceChangeListener referenceChangeListener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(referenceChangeListener);

        rc.reserve(a);
        rc.release(a);
        assertEquals(1, referenceChangeListener.referenceRemovedCount, "whenAReferenceIsRemovedTheReferenceChangeListenerShouldFire: L277");
        rc.releaseLast();
    }

    private static void assertContains(String message, Set<ReferenceOwner> owners, ReferenceOwner expected) {
        assertTrue(owners.contains(expected), message + " [expected=" + expected + ", owners=" + owners + "]");
    }

    @Test
    public void referenceChangeListenerShouldFireWhenAReferenceIsTransferred() {
        ReferenceCounted rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");
        final CounterReferenceChangeListener referenceChangeListener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(referenceChangeListener);

        rc.reserve(a);
        rc.reserveTransfer(a, b);
        assertEquals(1, referenceChangeListener.referenceTransferredCount, "referenceChangeListenerShouldFireWhenAReferenceIsTransferred: L296");
        rc.release(b);
        rc.releaseLast();
    }

    @Test
    public void shouldBeAbleToAddAndRemoveListeners() {
        ReferenceCounted rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");

        CounterReferenceChangeListener listener1 = new CounterReferenceChangeListener();
        CounterReferenceChangeListener listener2 = new CounterReferenceChangeListener();

        rc.addReferenceChangeListener(listener1);
        rc.reserve(a);
        assertEquals(1, listener1.referenceAddedCount, "shouldBeAbleToAddAndRemoveListeners: L312");
        assertEquals(0, listener2.referenceAddedCount, "shouldBeAbleToAddAndRemoveListeners: L313");
        rc.addReferenceChangeListener(listener2);

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals(2, listener1.referenceAddedCount, "shouldBeAbleToAddAndRemoveListeners: L318");
        assertEquals(1, listener2.referenceAddedCount, "shouldBeAbleToAddAndRemoveListeners: L319");
        rc.removeReferenceChangeListener(listener1);
        rc.release(a);
        assertEquals(0, listener1.referenceRemovedCount, "shouldBeAbleToAddAndRemoveListeners: L322");
        assertEquals(1, listener2.referenceRemovedCount, "shouldBeAbleToAddAndRemoveListeners: L323");
        rc.removeReferenceChangeListener(listener2);
        rc.release(b);
        assertEquals(0, listener1.referenceRemovedCount, "shouldBeAbleToAddAndRemoveListeners: L326");
        assertEquals(1, listener2.referenceRemovedCount, "shouldBeAbleToAddAndRemoveListeners: L327");
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
