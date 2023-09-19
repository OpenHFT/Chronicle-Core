package net.openhft.chronicle.core.scoped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

class WeakReferenceScopedResourceTest {

    private ScopedThreadLocal<AtomicLong> scopedThreadLocal;

    @BeforeEach
    void setUp() {
        scopedThreadLocal = new ScopedThreadLocal<>(AtomicLong::new, 3);
    }

    @Test
    void resourceIsCreatedPreAcquire() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        assertNull(sr.get()); // There should be nothing in it (this would never happen in the real world)
        sr.preAcquire();
        assertNotNull(sr.get());
    }

    @Test
    void strongReferenceIsCreatedPreAcquire() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        sr.preAcquire(); // creates the strong reference
        assertNotNull(sr.get());
        System.gc();
        assertNotNull(sr.get());
        sr.close(); // clears the strong reference
        System.gc();
        assertNull(sr.get());
    }
}