/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class BackgroundResourceReleaserTest extends CoreTestCommon {
    private final AtomicLong closed = new AtomicLong();
    private final AtomicLong released = new AtomicLong();

    @Test
    void testResourcesCleanedUp() throws IllegalStateException {
        int count = 20;
        for (int i = 0; i < count - 1; i++) {
            new BGCloseable().close();
            new BGReferenceCounted().releaseLast();
        }
        BGCloseable bgc = new BGCloseable();
        bgc.close();
        assertTrue(bgc.isClosing());

        BGReferenceCounted bgr = new BGReferenceCounted();
        bgr.releaseLast();
        assertEquals(0, bgr.refCount());

        BackgroundResourceReleaser.releasePendingResources();
        assertTrue(bgc.isClosed());
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
    }

    @Test
    void testConcurrentCloseWaitsForClosed() throws InterruptedException {
        WaitingCloseable wc = new WaitingCloseable();
        Thread closer = new Thread(wc::close);
        closer.start();
        // ensure the other thread does the close, so this thread exercises waitForClosed()
        long deadline = System.currentTimeMillis() + 5_000;
        while (!wc.isClosing()) {
            assertTrue(System.currentTimeMillis() < deadline, "closer thread never started closing");
            Thread.yield();
        }
        long waitStart = System.nanoTime();
        wc.close();
        long waitedMs = (System.nanoTime() - waitStart) / 1_000_000;
        assertTrue(wc.isClosed(), "close() returned before the concurrent close completed");
        // expected wait is ~10 ms (the closer's performClose); 500 ms allows for OS and GC stalls
        // while still catching a regression in waitForClosed() polling or its abort path
        assertTrue(waitedMs < 500, "close() waited " + waitedMs + " ms");
        closer.join();
    }

    @Test
    void testResourcesCleanedUpManually() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class)
                .withJvmArguments("-Dbackground.releaser=true", "-Dbackground.releaser.thread=false")
                .withProgramArguments("manual").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain manual", process);
        }
    }

    @Test
    void testResourcesCleanedUpAndThreadStopped() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class).withProgramArguments("stop").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain stop", process);
        }
    }

    @Test
    void testResourcesCleanedUpInForeground() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class)
                .withJvmArguments("-Dbackground.releaser=false").withProgramArguments("foreground").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain stop", process);
        }
    }

    @Test
    void isOnBackgroundResourceReleaserThreadIsTrueWhenOnThread() {
        assumeTrue(BackgroundResourceReleaser.BG_RELEASER);
        final WasInBackgroundResourceReleaserRecorder recorder = new WasInBackgroundResourceReleaserRecorder(true);
        recorder.close();
        assertValueBecomes(true, recorder::wasClosedInBackgroundResourceReleaserThread);
        assertTrue(recorder.wasClosedInBackgroundResourceReleaserThread());
    }

    @Test
    void isOnBackgroundResourceReleaserThreadIsFalseWhenNotOnThread() {
        final WasInBackgroundResourceReleaserRecorder recorder = new WasInBackgroundResourceReleaserRecorder(false);
        recorder.close();
        assertValueBecomes(false, recorder::wasClosedInBackgroundResourceReleaserThread);
        assertFalse(recorder.wasClosedInBackgroundResourceReleaserThread());
    }

    private void assertValueBecomes(boolean expectedValue, Supplier<Boolean> supplier) {
        long endTime = System.currentTimeMillis() + 5_000;
        while (supplier.get() == null) {
            Jvm.pause(10);
            if (System.currentTimeMillis() > endTime) {
                fail("Timed out waiting for value");
            }
        }
        assertEquals(expectedValue, supplier.get());
    }

    private static class WasInBackgroundResourceReleaserRecorder extends AbstractCloseable {

        private final boolean shouldPerformCloseInBackground;
        private Boolean wasClosedInBackgroundResourceReleaserThread = null;

        WasInBackgroundResourceReleaserRecorder(boolean shouldPerformCloseInBackground) {
            this.shouldPerformCloseInBackground = shouldPerformCloseInBackground;
        }

        @Override
        protected boolean shouldPerformCloseInBackground() {
            return shouldPerformCloseInBackground;
        }

        @Override
        protected void performClose() {
            wasClosedInBackgroundResourceReleaserThread = BackgroundResourceReleaser.isOnBackgroundResourceReleaserThread();
        }

        Boolean wasClosedInBackgroundResourceReleaserThread() {
            return wasClosedInBackgroundResourceReleaserThread;
        }
    }

    static class WaitingCloseable extends AbstractCloseable {
        @Override
        protected boolean shouldWaitForClosed() {
            return true;
        }

        @Override
        protected void performClose() {
            Jvm.pause(10);
        }
    }

    class BGCloseable extends AbstractCloseable {
        @Override
        protected boolean shouldPerformCloseInBackground() {
            return true;
        }

        @Override
        protected void performClose() {
            closed.incrementAndGet();
        }
    }

    class BGReferenceCounted extends AbstractReferenceCounted {
        @Override
        protected boolean canReleaseInBackground() {
            return true;
        }

        @Override
        protected void performRelease() {
            released.incrementAndGet();
        }
    }
}
