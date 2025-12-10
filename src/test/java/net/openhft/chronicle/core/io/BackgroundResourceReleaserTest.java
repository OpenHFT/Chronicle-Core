/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class BackgroundResourceReleaserTest extends CoreTestCommon {
    private final AtomicLong closed = new AtomicLong();
    private final AtomicLong released = new AtomicLong();

    private static void assertBetween(long min, long actual, long max) {
        if (min <= actual && actual <= max)
            return;
        throw new AssertionError("Not in range " + min + " <= " + actual + " <= " + max);
    }

    @Test
    public void testResourcesCleanedUp() throws IllegalStateException {
        int count = 20;
        BackgroundResourceReleaserSupport.createResources(0, count - 1, closed, released);
        BackgroundResourceReleaserSupport.assertExpectedCounts(count, closed, released);
        BackgroundResourceReleaserSupport.exerciseCloseableAndReferenceCounted(closed, released, !BackgroundResourceReleaser.BG_RELEASER);

        long start0 = System.currentTimeMillis();
        BackgroundResourceReleaserSupport.WaitingCloseable wc = BackgroundResourceReleaserSupport.createWaitingCloseable();
        new Thread(wc::close).start();
        wc.close();
        long time0 = System.currentTimeMillis() - start0;
        int error = Jvm.isAzulZing() || Jvm.isAzulZulu() || Jvm.isMacArm() ? 45 : 20;
        assertBetween(10, time0, 20 + 3 * error);

        BackgroundResourceReleaser.releasePendingResources();
        long time = System.currentTimeMillis() - start0;
        if (BackgroundResourceReleaser.BG_RELEASER) {
            long factor = (long) count * (Jvm.isAzulZulu() || OS.isMacOSX() ? 80L : OS.isWindows() ? 20L : 18L);
            assertBetween(count * 9L, time, factor);
        }
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
    }

    @Test
    public void testResourcesCleanedUpManually() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class)
                .withJvmArguments("-Dbackground.releaser=false").withProgramArguments("manual").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain manual", process);
        }
    }

    @Test
    public void testResourcesCleanedUpAndThreadStopped() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class).withProgramArguments("stop").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain stop", process);
        }
    }

    @Test
    public void testResourcesCleanedUpInForeground() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class)
                .withJvmArguments("-Dbackground.releaser=false").withProgramArguments("foreground").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain stop", process);
        }
    }

    @Test
    public void isOnBackgroundResourceReleaserThreadIsTrueWhenOnThread() {
        assumeTrue(BackgroundResourceReleaser.BG_RELEASER);
        final WasInBackgroundResourceReleaserRecorder recorder = new WasInBackgroundResourceReleaserRecorder(true);
        recorder.close();
        assertValueBecomes(true, recorder::wasClosedInBackgroundResourceReleaserThread);
        assertTrue(recorder.wasClosedInBackgroundResourceReleaserThread());
    }

    @Test
    public void isOnBackgroundResourceReleaserThreadIsFalseWhenNotOnThread() {
        final WasInBackgroundResourceReleaserRecorder recorder = new WasInBackgroundResourceReleaserRecorder(false);
        recorder.close();
        assertValueBecomes(false, recorder::wasClosedInBackgroundResourceReleaserThread);
        assertFalse(recorder.wasClosedInBackgroundResourceReleaserThread());
    }

    @Test
    public void releasePendingResourcesFlushesQueuedWork() {
        AtomicInteger closedCount = new AtomicInteger();
        int total = 32;
        for (int i = 0; i < total; i++) {
            CountingCloseable closeable = new CountingCloseable(closedCount);
            BackgroundResourceReleaser.release(closeable);
        }
        BackgroundResourceReleaser.releasePendingResources();
        assertEquals(total, closedCount.get());
    }

    @Test
    public void releasePendingResourcesReassertsInterrupt() throws InterruptedException {
        AtomicBoolean interrupted = new AtomicBoolean();
        AtomicInteger closed = new AtomicInteger();
        Thread t = new Thread(() -> {
            CountingCloseable closeable = new CountingCloseable(closed);
            BackgroundResourceReleaser.release(closeable);
            Thread.currentThread().interrupt();
            BackgroundResourceReleaser.releasePendingResources();
            interrupted.set(Thread.currentThread().isInterrupted());
        });
        t.start();
        t.join();
        assertEquals(1, closed.get());
        assertTrue(interrupted.get());
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

    static class CountingCloseable extends AbstractCloseable {

        private final AtomicInteger counter;

        CountingCloseable(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        protected boolean shouldPerformCloseInBackground() {
            return true;
        }

        @Override
        protected void performClose() {
            counter.incrementAndGet();
        }
    }
}
