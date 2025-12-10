/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class BackgroundResourceReleaserMain {
    private final AtomicLong closed = new AtomicLong();
    private final AtomicLong released = new AtomicLong();

    public static void main(String[] args) throws Throwable {
        try {
            switch (args[0]) {
                case "stop":
                    new BackgroundResourceReleaserMain().runResourcesCleanedUpAndStopped();
                    break;
                case "manual":
                    new BackgroundResourceReleaserMain().runResourcesCleanedUpManually();
                    break;
                case "foreground":
                    new BackgroundResourceReleaserMain().runResourcesCleanedUpInForeground();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown mode: " + args[0]);
            }
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    private void runResourcesCleanedUpManually() throws IllegalAccessException {
        assertNull(getReleaserThread());
        int count = 20;
        BackgroundResourceReleaserSupport.createResources(0, count - 1, closed, released);
        BackgroundResourceReleaserSupport.assertExpectedCounts(count, closed, released);
        BackgroundResourceReleaserSupport.exerciseCloseableAndReferenceCounted(closed, released, !BackgroundResourceReleaser.BG_RELEASER);

        BackgroundResourceReleaserSupport.WaitingCloseable wc = BackgroundResourceReleaserSupport.createWaitingCloseable();
        new Thread(wc::close).start();
        wc.close();
        if (BackgroundResourceReleaser.BG_RELEASER) {
            assertNotEquals(count, closed.get());
            assertNotEquals(count, released.get());
        } else {
            assertEquals(count, closed.get());
            assertEquals(count, released.get());
        }

        BackgroundResourceReleaser.releasePendingResources();
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
        BackgroundResourceReleaser.releasePendingResources();
    }

    private void runResourcesCleanedUpAndStopped() throws IllegalAccessException {
        Thread releaserThread = getReleaserThread();
        if (BackgroundResourceReleaser.BG_RELEASER)
            assertNotNull(releaserThread);
        int count = 20;
        BackgroundResourceReleaserSupport.createResources(1, count, closed, released);
        BackgroundResourceReleaserSupport.assertExpectedCounts(count, closed, released);
        BackgroundResourceReleaserSupport.exerciseCloseableAndReferenceCounted(closed, released, !BackgroundResourceReleaser.BG_RELEASER);

        BackgroundResourceReleaserSupport.WaitingCloseable wc = BackgroundResourceReleaserSupport.createWaitingCloseable();
        new Thread(wc::close).start();
        wc.close();

        BackgroundResourceReleaser.stop();
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
        BackgroundResourceReleaser.stop();

        if (getReleaserThread() == null) {
            // If the background resource releaser is not running, we are done.
            return;
        }
        for (int i = 1; i < 1000; i += i) {
            if (!releaserThread.isAlive()) return;

            Jvm.pause(i);
        }

        fail("Resource releaser thread did not terminate.");
    }

    private void runResourcesCleanedUpInForeground() throws IllegalAccessException {
        assertNull(getReleaserThread());
        int count = 20;
        BackgroundResourceReleaserSupport.createResources(0, count - 1, closed, released);
        assertEquals(count - 1, closed.get());
        assertEquals(count - 1, released.get());
        BackgroundResourceReleaserSupport.exerciseCloseableAndReferenceCounted(closed, released, true);

        BackgroundResourceReleaserSupport.WaitingCloseable wc = BackgroundResourceReleaserSupport.createWaitingCloseable();
        new Thread(wc::close).start();
        wc.close();
        assertEquals(count, closed.get());
        assertEquals(count, released.get());

        // Does nothing
        BackgroundResourceReleaser.releasePendingResources();
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
    }

    private Thread getReleaserThread() throws IllegalAccessException {
        return (Thread) Jvm.getField(BackgroundResourceReleaser.class, "RELEASER").get(null);
    }
}
