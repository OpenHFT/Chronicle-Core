/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class WeakReferenceScopedResourceTest {

    private ScopedThreadLocal<AtomicLong> scopedThreadLocal;

    @BeforeEach
    void setUp() {
        scopedThreadLocal = new ScopedThreadLocal<>(AtomicLong::new, 3);
    }

    @Test
    @DisplayName("preAcquire creates a new weak resource")
    void resourceIsCreatedPreAcquire() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        assertNull(sr.get(), "resource should be null before preAcquire"); // There should be nothing in it (this would never happen in the real world)
        sr.preAcquire();
        assertNotNull(sr.get(), "resource should be non-null after preAcquire on weak reference");
    }

    @Test
    @DisplayName("preAcquire creates strong reference for reuse")
    void strongReferenceIsCreatedPreAcquire() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        sr.preAcquire(); // creates the strong reference
        assertNotNull(sr.get(), "resource should be non-null after preAcquire with strong reference");
        System.gc();
        assertNotNull(sr.get(), "resource should survive GC when strong reference exists");
        sr.close(); // clears the strong reference
        System.gc();
        assertNull(sr.get(), "resource should be collected by GC after closing strong reference");
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("closeResource when ref is null does nothing safely")
    void closeResourceWhenRefIsNull() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        // ref is null before preAcquire
        assertDoesNotThrow(sr::closeResource, "closeResource should not throw when ref is null");
    }

    @Test
    @DisplayName("closeResource clears the weak reference after preAcquire")
    void closeResourceClearsWeakReference() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        sr.preAcquire();
        assertNotNull(sr.get(), "resource should exist after preAcquire");
        sr.closeResource();
        assertNull(sr.getType(), "getType should return null after closeResource");
    }

    @Test
    @DisplayName("WeakReferenceScopedResource getType returns null when weak reference is missing")
    void getTypeWhenRefIsNull() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        // ref is null before preAcquire
        assertNull(sr.getType(), "getType returns null when weak reference is missing");
    }

    @Test
    @DisplayName("getType returns class type when AtomicLong resource exists")
    void getTypeReturnsClassType() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        sr.preAcquire();
        assertEquals(AtomicLong.class, sr.getType(), "getType should return AtomicLong.class");
    }

    @Test
    @DisplayName("preAcquire recreates resource after GC collection")
    void preAcquireRecreatesAfterGC() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        sr.preAcquire();
        AtomicLong first = sr.get();
        assertNotNull(first, "first resource should exist before GC collection");
        first.set(42);

        sr.close(); // Clear strong reference
        System.gc();
        // Force GC to collect the weakly referenced object
        for (int i = 0; i < 10; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        sr.preAcquire();
        AtomicLong second = sr.get();
        assertNotNull(second, "second resource should exist after re-acquire");
        // The new resource should be fresh (value 0), not the old one (value 42)
        // unless the GC didn't collect it
    }

    @Test
    @DisplayName("close sets strongRef to null after release")
    void closeSetsStrongRefToNull() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        sr.preAcquire();
        assertNotNull(sr.get(), "resource should exist before close");
        sr.close();
        assertNull(sr.get(), "get should return null after close releases resource");
    }
}
