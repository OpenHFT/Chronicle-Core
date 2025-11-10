//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
        assertEquals(1, released.get());
        assertEquals(0, ref.refCount());
        assertFalse(ref.tryReserve(ReferenceOwner.INIT));
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
        ReferenceOwner A = ReferenceOwner.INIT;
        ReferenceOwner B = ReferenceOwner.INIT; // for API symmetry; a second owner type is not required for counting
        assertDoesNotThrow(() -> ref.reserveTransfer(A, B));
        assertEquals(1, xfers.get());
        // cleanup
        ref.warnAndReleaseIfNotReleased();
    }
}

