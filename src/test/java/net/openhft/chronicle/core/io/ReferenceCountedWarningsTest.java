/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

