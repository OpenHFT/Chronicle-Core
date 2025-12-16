/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import java.util.function.IntSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            assertEquals(1, referenceCounted.refCount(), "refCount should remain at 1 after throwExceptionIfReleased");
        } finally {
            referenceCounted.releaseLast();
        }
    }

    @Test
    public void throwIfNotReleasedWillThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();
        try {
            assertThrows(IllegalStateException.class,
                    referenceCounted::throwExceptionIfNotReleased,
                    "throwIfNotReleasedWillThrowIfResourceIsNotReleased");
        } finally {
            referenceCounted.releaseLast();
        }
    }

    @Test
    public void throwIfNotReleasedWillNotThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        referenceCounted.throwExceptionIfNotReleased();
        assertEquals(0, referenceCounted.refCount(), "throwIfNotReleasedWillNotThrowIfResourceIsReleased: L57");
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
        assertEquals(0, listener.referenceRemovedCount, "listenersShouldNotBeNotifiedOnWarnAndReleaseIfNotReleased: L74");
    }

    protected void exerciseReserveLifecycle(ReferenceCountedTracer rc, IntSupplier performReleaseSupplier) {
        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount(), "refCount after first reserve");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals(3, rc.refCount(), "refCount after second reserve");

        assertThrows(IllegalStateException.class, () -> rc.reserve(a));
        assertEquals(3, rc.refCount(), "refCount should remain after failed duplicate reserve");

        rc.release(b);
        assertEquals(2, rc.refCount(), "refCount after releasing second owner");

        rc.release(a);
        assertEquals(1, rc.refCount(), "refCount after releasing first owner");
        if (performReleaseSupplier != null) {
            assertEquals(0, performReleaseSupplier.getAsInt(), "performRelease should not have run before releaseLast");
        }

        rc.releaseLast();
        assertEquals(0, rc.refCount(), "refCount after releaseLast");
        if (performReleaseSupplier != null) {
            assertEquals(1, performReleaseSupplier.getAsInt(), "performRelease should have run once after releaseLast");
        }
    }
}
