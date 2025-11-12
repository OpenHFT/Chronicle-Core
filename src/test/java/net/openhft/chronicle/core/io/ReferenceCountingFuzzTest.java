/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceCountingFuzzTest {

    private static final ReferenceOwner[] OWNERS = {
            ReferenceOwner.temporary("owner-A"),
            ReferenceOwner.temporary("owner-B"),
            ReferenceOwner.temporary("owner-C"),
            ReferenceOwner.temporary("owner-D")
    };

    @RepeatedTest(25)
    void randomisedReserveReleaseSequence(org.junit.jupiter.api.RepetitionInfo repetitionInfo) throws ClosedIllegalStateException {
        TestReference ref = new TestReference(false);
        boolean[] hasOwner = new boolean[OWNERS.length];
        Random random = new Random(repetitionInfo.getCurrentRepetition());

        for (int step = 0; step < 200; step++) {
            int idx = random.nextInt(OWNERS.length);
            ReferenceOwner owner = OWNERS[idx];
            if (random.nextBoolean()) {
                if (!hasOwner[idx]) {
                    assertTrue(ref.tryReserve(owner));
                    hasOwner[idx] = true;
                }
            } else {
                if (hasOwner[idx]) {
                    ref.release(owner);
                    hasOwner[idx] = false;
                }
            }
        }

        for (int i = 0; i < OWNERS.length; i++) {
            if (hasOwner[i]) {
                ref.release(OWNERS[i]);
                hasOwner[i] = false;
            }
        }

        ref.releaseLast(ReferenceOwner.INIT);
        assertEquals(1, ref.releaseCount.get());
        assertThrows(ClosedIllegalStateException.class, () -> ref.reserve(OWNERS[0]));
        assertThrows(ClosedIllegalStateException.class, ref::throwExceptionIfReleased);
    }

    @Test
    void backgroundReleaseHappensOnReleaserThread() throws ClosedIllegalStateException {
        TestReference ref = new TestReference(true);
        ref.releaseLast(ReferenceOwner.INIT);
        BackgroundResourceReleaser.releasePendingResources();

        assertEquals(1, ref.releaseCount.get());
        assertNotNull(ref.releasedOnBackgroundThread.get(),
                "performRelease should have been invoked exactly once");
    }

    private static final class TestReference extends AbstractReferenceCounted {
        private final boolean background;
        private final AtomicReference<Thread> releaseThread = new AtomicReference<>();
        private final AtomicInteger releaseCount = new AtomicInteger();
        private final AtomicReference<Boolean> releasedOnBackgroundThread = new AtomicReference<>();

        TestReference(boolean background) {
            this.background = background;
            singleThreadedCheckDisabled(true);
        }

        @Override
        protected boolean canReleaseInBackground() {
            return background;
        }

        @Override
        protected void performRelease() {
            releaseThread.set(Thread.currentThread());
            releaseCount.incrementAndGet();
            releasedOnBackgroundThread.set(BackgroundResourceReleaser.isOnBackgroundResourceReleaserThread());
        }
    }
}
