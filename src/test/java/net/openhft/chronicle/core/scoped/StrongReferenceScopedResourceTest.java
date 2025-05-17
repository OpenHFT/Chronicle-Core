package net.openhft.chronicle.core.scoped;

import net.openhft.chronicle.core.threads.CleaningThread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

class StrongReferenceScopedResourceTest {

    private ScopedThreadLocal<AtomicLong> scopedThreadLocal;

    @BeforeEach
    void setUp() {
        scopedThreadLocal = new ScopedThreadLocal<>(AtomicLong::new, 2);
    }

    @Test
    void resourcesRemainStronglyReferencedUntilClosed() {
        WeakReference<AtomicLong> ref;
        // Acquire a resource and keep only a weak reference once returned
        try (ScopedResource<AtomicLong> sr = scopedThreadLocal.get()) {
            ref = new WeakReference<>(sr.get());
        }

        // Garbage collect - resource should still be strongly referenced by the pool
        System.gc();
        assertNotNull("Resource should be retained while in pool", ref.get());

        // Re-acquire, should return the same instance
        try (ScopedResource<AtomicLong> sr2 = scopedThreadLocal.get()) {
            assertSame(ref.get(), sr2.get());
        }

        // Cleanup thread locals which closes and clears the strong reference
        CleaningThread.performCleanup(Thread.currentThread());
        System.gc();

        // After cleanup the resource should be eligible for GC
        assertNull("Resource should be released after cleanup", ref.get());
    }
}
