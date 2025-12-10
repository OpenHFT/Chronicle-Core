/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.Test;

import java.util.function.IntSupplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Any implementor of {@link ReferenceCountedTracer} should implement a test class
 * that extends this or one of its more specific children
 */
public abstract class ReferenceCountedTracerContractTest extends ReferenceCountedContractTest {

    @Override
    protected abstract ReferenceCountedTracer createReferenceCounted();

    @Test
    public void throwIfReleasedWillThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        assertThrows(ClosedIllegalStateException.class, referenceCounted::throwExceptionIfReleased);
    }

    @Test
    public void throwIfReleasedWillNotThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();
        try {
            referenceCounted.throwExceptionIfReleased();
            assertEquals("refCount should remain at 1 after throwExceptionIfReleased", 1, referenceCounted.refCount());
        } finally {
            referenceCounted.releaseLast();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void throwIfNotReleasedWillThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();
        try {
            referenceCounted.throwExceptionIfNotReleased();
        } finally {
            referenceCounted.releaseLast();
        }
    }

    @Test
    public void throwIfNotReleasedWillNotThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        referenceCounted.throwExceptionIfNotReleased();
        assertEquals(0, referenceCounted.refCount());
    }

    @Test
    public void listenersShouldNotBeNotifiedOnWarnAndReleaseIfNotReleased() {
        ReferenceCountedTracer rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");

        CounterReferenceChangeListener listener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(listener);
        rc.reserve(a);
        rc.reserve(b);

        expectException("Discarded without being released");
        rc.warnAndReleaseIfNotReleased();
        assertEquals(0, listener.referenceRemovedCount);
    }

    protected void exerciseReserveLifecycle(ReferenceCountedTracer rc, IntSupplier performReleaseSupplier) {
        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals("refCount after first reserve", 2, rc.refCount());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals("refCount after second reserve", 3, rc.refCount());

        assertThrows(IllegalStateException.class, () -> rc.reserve(a));
        assertEquals("refCount should remain after failed duplicate reserve", 3, rc.refCount());

        rc.release(b);
        assertEquals("refCount after releasing second owner", 2, rc.refCount());

        rc.release(a);
        assertEquals("refCount after releasing first owner", 1, rc.refCount());
        if (performReleaseSupplier != null) {
            assertEquals("performRelease should not have run before releaseLast", 0, performReleaseSupplier.getAsInt());
        }

        rc.releaseLast();
        assertEquals("refCount after releaseLast", 0, rc.refCount());
        if (performReleaseSupplier != null) {
            assertEquals("performRelease should have run once after releaseLast", 1, performReleaseSupplier.getAsInt());
        }
    }
}
