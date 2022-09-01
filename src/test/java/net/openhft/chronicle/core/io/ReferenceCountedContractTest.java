package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

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

    @Test
    public void reserveWillIncrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount());

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertEquals(2, referenceCounted.refCount());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserve(b);
        assertEquals(3, referenceCounted.refCount());
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
        assertEquals(2, referenceCounted.refCount());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserveTransfer(a, b);
        assertEquals(2, referenceCounted.refCount());
    }

    @Test
    public void releaseWillDecrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount());

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertEquals(2, referenceCounted.refCount());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserve(b);
        assertEquals(3, referenceCounted.refCount());

        referenceCounted.release(b);
        assertEquals(2, referenceCounted.refCount());

        referenceCounted.release(a);
        assertEquals(1, referenceCounted.refCount());
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
        assertEquals(0, referenceCounted.refCount());
    }

    @Test
    public void releaseLastWillDecrementReferenceCount() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        assertEquals(1, referenceCounted.refCount());

        referenceCounted.releaseLast();
        assertEquals(0, referenceCounted.refCount());
    }

    @Test
    public void releaseLastWillReleaseThenFailWhenReferenceIsNotLast() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);

        assertEquals(2, referenceCounted.refCount());

        assertThrows(IllegalStateException.class, referenceCounted::releaseLast);

        assertEquals(1, referenceCounted.refCount());
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
        assertTrue(referenceCounted.tryReserve(a));
    }

    @Test
    public void tryReserveWillReturnFalseWhenResourceIsAlreadyReleased() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertFalse(referenceCounted.tryReserve(a));
    }

    @Test
    public void reservedByWillReturnTrueWhenOwnerHasReferenceReserved() {
        ReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        assertTrue(referenceCounted.reservedBy(a));
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
        Jvm.pause(3_000);
        running.set(false);
        futures.forEach(this::getQuietly);
        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("ExecutorService didn't shut down");
        }
        counted.releaseLast();
    }

    private void getQuietly(Future<?> future) {
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Jvm.error().on(ReferenceCountedContractTest.class, "Exception thrown by acquirer", e);
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

        public Reference(int owner, int index, ReferenceCounted resource) {
            this.owner = owner;
            this.index = index;
            this.resource = resource;
            this.resource.reserve(this);
        }

        public void release() {
            resource.release(this);
        }

        @Override
        public String referenceName() {
            return String.format("{id=%s, index=%s}", owner, index);
        }
    }
}
