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

class VanillaReferenceCountedEdgeTest {

    private VanillaReferenceCounted newRef(AtomicInteger released) {
        return new VanillaReferenceCounted(released::incrementAndGet, VanillaReferenceCounted.class);
    }

    @Test
    void doubleReleaseThrows() {
        AtomicInteger released = new AtomicInteger();
        VanillaReferenceCounted ref = newRef(released);
        ref.release(ReferenceOwner.INIT);
        assertEquals(1, released.get());
        ClosedIllegalStateException ex = assertThrows(ClosedIllegalStateException.class, () -> ref.release(ReferenceOwner.INIT));
        assertTrue(ex.getMessage().contains("released"));
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
        assertEquals(1, released.get());
    }

    @Test
    void listenersAreCalledOnAddRemove() {
        AtomicInteger added = new AtomicInteger();
        AtomicInteger removed = new AtomicInteger();
        VanillaReferenceCounted ref = newRef(new AtomicInteger());
        ReferenceChangeListener listener = new ReferenceChangeListener() {
            @Override
            public void onReferenceAdded(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) { added.incrementAndGet(); }
            @Override
            public void onReferenceRemoved(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) { removed.incrementAndGet(); }
            @Override
            public void onReferenceTransferred(ReferenceCounted referenceCounted, ReferenceOwner from, ReferenceOwner to) { /* ignore */ }
        };
        ref.addReferenceChangeListener(listener);
        ref.reserve(ReferenceOwner.INIT);
        ref.release(ReferenceOwner.INIT);
        ref.removeReferenceChangeListener(listener);
        assertEquals(1, added.get());
        assertEquals(1, removed.get());
    }
}
