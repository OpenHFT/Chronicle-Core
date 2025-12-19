/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VanillaReferenceCountedEdgeTest {

    private VanillaReferenceCounted newRef(AtomicInteger released) {
        return new VanillaReferenceCounted(released::incrementAndGet, VanillaReferenceCounted.class);
    }

    @Test
    void doubleReleaseThrows() {
        AtomicInteger released = new AtomicInteger();
        VanillaReferenceCounted ref = newRef(released);
        ref.release(ReferenceOwner.INIT);
        assertEquals(1, released.get(), "release callback should be invoked once after first release");
        ClosedIllegalStateException ex = assertThrows(ClosedIllegalStateException.class, () -> ref.release(ReferenceOwner.INIT));
        assertTrue(ex.getMessage().contains("released"), "exception message should indicate resource already released");
    }

    @Test
    void reserveAfterReleasedThrows() {
        AtomicInteger released = new AtomicInteger();
        VanillaReferenceCounted ref = newRef(released);
        ref.release(ReferenceOwner.INIT);
        assertThrows(ClosedIllegalStateException.class, () -> ref.reserve(ReferenceOwner.INIT));
    }

    @Test
    void notLastReleaseIsDetected() {
        AtomicInteger released = new AtomicInteger();
        VanillaReferenceCounted ref = newRef(released);
        ref.reserve(ReferenceOwner.INIT);
        // release once (leaving one outstanding)
        ref.release(ReferenceOwner.INIT);
        // still reserved -> throwExceptionIfNotReleased should throw
        assertThrows(IllegalStateException.class, ref::throwExceptionIfNotReleased);
        // cleanup
        ref.release(ReferenceOwner.INIT);
        assertEquals(1, released.get(), "release callback should be invoked once after final release");
    }

    @Test
    void listenersAreCalledOnAddRemove() {
        AtomicInteger added = new AtomicInteger();
        AtomicInteger removed = new AtomicInteger();
        VanillaReferenceCounted ref = newRef(new AtomicInteger());
        ReferenceChangeListener listener = new ReferenceChangeListener() {
            @Override
            public void onReferenceAdded(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
                added.incrementAndGet();
            }

            @Override
            public void onReferenceRemoved(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
                removed.incrementAndGet();
            }

            @Override
            public void onReferenceTransferred(ReferenceCounted referenceCounted,
                                               ReferenceOwner from,
                                               ReferenceOwner to) {
                // ignore
            }
        };
        ref.addReferenceChangeListener(listener);
        ref.reserve(ReferenceOwner.INIT);
        ref.release(ReferenceOwner.INIT);
        ref.removeReferenceChangeListener(listener);
        assertEquals(1, added.get(), "listener should be notified once on reserve");
        assertEquals(1, removed.get(), "listener should be notified once on release");
    }
}
