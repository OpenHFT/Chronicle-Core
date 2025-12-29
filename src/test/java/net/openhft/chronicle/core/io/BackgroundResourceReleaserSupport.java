/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BackgroundResourceReleaserSupport {

    private BackgroundResourceReleaserSupport() {
    }

    static void createResources(int startInclusive, int endExclusive, AtomicLong closed, AtomicLong released) {
        for (int i = startInclusive; i < endExclusive; i++) {
            new BGCloseable(closed).close();
            new BGReferenceCounted(released).releaseLast();
        }
    }

    static void assertExpectedCounts(int count, AtomicLong closed, AtomicLong released) {
        int expectedCount = BackgroundResourceReleaser.BG_RELEASER ? 2 : count;
        long closedCount = closed.get();
        assertTrue(Math.abs((long) expectedCount - closedCount) <= 2,
                "closed count [expected=" + expectedCount + ", actual=" + closedCount + "]");
        long releasedCount = released.get();
        assertTrue(Math.abs((long) expectedCount - releasedCount) <= 2,
                "released count [expected=" + expectedCount + ", actual=" + releasedCount + "]");
    }

    static void exerciseCloseableAndReferenceCounted(AtomicLong closed, AtomicLong released, boolean expectClosed) {
        BGCloseable bgc = new BGCloseable(closed);
        bgc.close();
        assertTrue(bgc.isClosing(), "closeable should report closing");
        assertEquals(expectClosed, bgc.isClosed(), "closeable closed state should match expected value");

        BGReferenceCounted bgr = new BGReferenceCounted(released);
        bgr.releaseLast();
        assertEquals(0, bgr.refCount(), "refCount should be zero after releaseLast");
    }

    static WaitingCloseable createWaitingCloseable() {
        return new WaitingCloseable();
    }

    static final class BGCloseable extends AbstractCloseable {
        private final AtomicLong closed;

        BGCloseable(AtomicLong closed) {
            this.closed = closed;
        }

        @Override
        protected boolean shouldPerformCloseInBackground() {
            return true;
        }

        @Override
        protected void performClose() {
            closed.incrementAndGet();
            Jvm.pause(10);
        }
    }

    static final class BGReferenceCounted extends AbstractReferenceCounted {
        private final AtomicLong released;

        BGReferenceCounted(AtomicLong released) {
            this.released = released;
        }

        @Override
        protected boolean canReleaseInBackground() {
            return true;
        }

        @Override
        protected void performRelease() {
            released.incrementAndGet();
            Jvm.pause(10);
        }
    }

    static final class WaitingCloseable extends AbstractCloseable {
        @Override
        protected boolean shouldWaitForClosed() {
            return true;
        }

        @Override
        protected void performClose() {
            Jvm.pause(10);
        }
    }
}
