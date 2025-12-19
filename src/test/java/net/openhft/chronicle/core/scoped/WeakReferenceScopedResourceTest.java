/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import org.junit.jupiter.api.BeforeEach;
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
    void resourceIsCreatedPreAcquire() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        assertNull(sr.get(), "resource should be null before preAcquire"); // There should be nothing in it (this would never happen in the real world)
        sr.preAcquire();
        assertNotNull(sr.get(), "resource should be non-null after preAcquire");
    }

    @Test
    void strongReferenceIsCreatedPreAcquire() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        sr.preAcquire(); // creates the strong reference
        assertNotNull(sr.get(), "resource should be non-null after preAcquire");
        System.gc();
        assertNotNull(sr.get(), "resource should survive GC when strong reference exists");
        sr.close(); // clears the strong reference
        System.gc();
        assertNull(sr.get(), "resource should be collected by GC after closing strong reference");
    }
}
