/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class BackgroundResourceReleaserTest {
    private final AtomicLong closed = new AtomicLong();
    private final AtomicLong released = new AtomicLong();

    @Test
    public void testResourcesCleanedUp() throws IllegalStateException {
        assumeTrue(BackgroundResourceReleaser.BG_RELEASER);
        int count = 20;
        for (int i = 1; i < count; i++) {
            new BGCloseable().close();
            new BGReferenceCounted().releaseLast();
        }
        assertEquals(1, closed.get(), 1);
        assertEquals(1, released.get(), 1);
        BGCloseable bgc = new BGCloseable();
        bgc.close();
        assertTrue(bgc.isClosing());
        assertFalse(bgc.isClosed());

        BGReferenceCounted bgr = new BGReferenceCounted();
        bgr.releaseLast();
        assertEquals(0, bgr.refCount());

        long start0 = System.currentTimeMillis();
        WaitingCloseable wc = new WaitingCloseable();
        new Thread(wc::close).start();
        wc.close();
        long time0 = System.currentTimeMillis() - start0;
        int error = Jvm.isAzulZing() || Jvm.isAzulZulu() || Jvm.isMacArm() ? 45 : Jvm.isArm() || OS.isWindows() ? 16 : 12;
        assertBetween(10, time0, 15 + 3 * error);

        BackgroundResourceReleaser.releasePendingResources();
        long time = System.currentTimeMillis() - start0;
        assertBetween(count * 9, time, count * (Jvm.isAzulZulu() || Jvm.isMacArm() ? 60 : 18));
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
    }

    @Test
    public void testResourcesCleanedUpManually() throws IllegalStateException, IOException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class)
                .withJvmArguments("-Dbackground.releaser.thread=false").withProgramArguments("manual").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain manual", process);
        }
    }

    @Test
    public void testResourcesCleanedUpAndThreadStopped() throws IllegalStateException, IOException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class).withProgramArguments("stop").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain stop", process);
        }
    }

    @Test
    public void testResourcesCleanedUpInForeground() throws IllegalStateException, IOException, InterruptedException {
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

        public WasInBackgroundResourceReleaserRecorder(boolean shouldPerformCloseInBackground) {
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

        public Boolean wasClosedInBackgroundResourceReleaserThread() {
            return wasClosedInBackgroundResourceReleaserThread;
        }
    }

    static void assertBetween(long min, long actual, long max) {
        if (min <= actual && actual <= max)
            return;
        throw new AssertionError("Not in range " + min + " <= " + actual + " <= " + max);
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
            Jvm.pause(10);
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
            Jvm.pause(10);
        }
    }
}
