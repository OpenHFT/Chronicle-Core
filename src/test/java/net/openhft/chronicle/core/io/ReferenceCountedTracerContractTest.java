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
    void throwIfReleasedWillThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        assertThrows(ClosedIllegalStateException.class, referenceCounted::throwExceptionIfReleased,
                "throwExceptionIfReleased should throw after release");
    }

    @Test
    void throwIfReleasedWillNotThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();
        try {
            referenceCounted.throwExceptionIfReleased();
            assertEquals(1, referenceCounted.refCount(), "checking release state should not modify reference count");
        } finally {
            referenceCounted.releaseLast();
        }
    }

    @Test
    void throwIfNotReleasedWillThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();
        try {
            assertThrows(IllegalStateException.class,
                    referenceCounted::throwExceptionIfNotReleased,
                    "unreleased resource should fail release check invariant");
        } finally {
            referenceCounted.releaseLast();
        }
    }

    @Test
    void throwIfNotReleasedWillNotThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        referenceCounted.throwExceptionIfNotReleased();
        assertEquals(0, referenceCounted.refCount(), "reference count should be zero after final release");
    }

    @Test
    void listenersShouldNotBeNotifiedOnWarnAndReleaseIfNotReleased() {
        ReferenceCountedTracer rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");

        CounterReferenceChangeListener listener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(listener);
        rc.reserve(a);
        rc.reserve(b);

        expectException("Discarded without being released");
        rc.warnAndReleaseIfNotReleased();
        assertEquals(0, listener.referenceRemovedCount, "warning should not trigger listener notifications for unreleased resources");
    }

    protected void exerciseReserveLifecycle(ReferenceCountedTracer rc, IntSupplier performReleaseSupplier) {
        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount(), "first reserve should increment count from 1 to 2");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals(3, rc.refCount(), "second reserve should increment count from 2 to 3");

        assertThrows(IllegalStateException.class, () -> rc.reserve(a),
                "reserve should reject duplicate owner");
        assertEquals(3, rc.refCount(), "duplicate reserve attempt should not modify reference count");

        rc.release(b);
        assertEquals(2, rc.refCount(), "releasing second owner should decrement count from 3 to 2");

        rc.release(a);
        assertEquals(1, rc.refCount(), "releasing first owner should decrement count from 2 to 1");
        if (performReleaseSupplier != null) {
            assertEquals(0, performReleaseSupplier.getAsInt(), "resource cleanup should be deferred until final release");
        }

        rc.releaseLast();
        assertEquals(0, rc.refCount(), "final release should decrement count from 1 to 0");
        if (performReleaseSupplier != null) {
            assertEquals(1, performReleaseSupplier.getAsInt(), "resource cleanup should execute exactly once at final release");
        }
    }
}
