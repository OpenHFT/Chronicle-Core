/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceCountedWarningsTest {

    private VanillaReferenceCounted newRef(AtomicInteger releasedCount) {
        return new VanillaReferenceCounted(releasedCount::incrementAndGet, VanillaReferenceCounted.class);
    }

    @Test
    void warnAndReleaseIfNotReleasedClosesAndClears() {
        AtomicInteger released = new AtomicInteger();
        VanillaReferenceCounted ref = newRef(released);
        ref.reserve(ReferenceOwner.INIT);
        // force close without releasing all reservations
        assertDoesNotThrow(ref::warnAndReleaseIfNotReleased);
        assertEquals(1, released.get(), "warnAndReleaseIfNotReleased should invoke release callback once");
        assertEquals(0, ref.refCount(), "reference count should be zero after warnAndReleaseIfNotReleased");
        assertFalse(ref.tryReserve(ReferenceOwner.INIT), "tryReserve should fail after reference has been released");
    }

    @Test
    void reserveTransferNotifiesListener() {
        AtomicInteger xfers = new AtomicInteger();
        VanillaReferenceCounted ref = newRef(new AtomicInteger());
        ref.addReferenceChangeListener(new ReferenceChangeListener() {
            @Override
            public void onReferenceTransferred(ReferenceCounted referenceCounted, ReferenceOwner fromOwner, ReferenceOwner toOwner) {
                xfers.incrementAndGet();
            }
        });
        ReferenceOwner ownerA = ReferenceOwner.INIT;
        ReferenceOwner ownerB = ReferenceOwner.INIT; // for API symmetry; a second owner type is not required
        assertDoesNotThrow(() -> ref.reserveTransfer(ownerA, ownerB));
        assertEquals(1, xfers.get(), "reserveTransfer should notify listener exactly once");
        // cleanup
        ref.warnAndReleaseIfNotReleased();
    }
}
