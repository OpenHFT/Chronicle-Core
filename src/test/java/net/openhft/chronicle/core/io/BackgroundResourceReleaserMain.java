/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNull(getReleaserThread(), "releaser thread should not exist in manual cleanup mode");
        int count = 20;
        BackgroundResourceReleaserSupport.createResources(0, count - 1, closed, released);
        BackgroundResourceReleaserSupport.assertExpectedCounts(count, closed, released);
        BackgroundResourceReleaserSupport.exerciseCloseableAndReferenceCounted(closed, released, !BackgroundResourceReleaser.BG_RELEASER);

        BackgroundResourceReleaserSupport.WaitingCloseable wc = BackgroundResourceReleaserSupport.createWaitingCloseable();
        new Thread(wc::close).start();
        wc.close();
        if (BackgroundResourceReleaser.BG_RELEASER) {
            assertNotEquals(count, closed.get(), "background releaser should not close all resources immediately");
            assertNotEquals(count, released.get(), "background releaser should not release all resources immediately");
        } else {
            assertEquals(count, closed.get(), "all resources should be closed without background releaser");
            assertEquals(count, released.get(), "all resources should be released without background releaser");
        }

        BackgroundResourceReleaser.releasePendingResources();
        assertEquals(count, closed.get(), "all resources should be closed after manual release");
        assertEquals(count, released.get(), "all resources should be released after manual release");
        AbstractCloseable.assertCloseablesClosed();
        BackgroundResourceReleaser.releasePendingResources();
    }

    private void runResourcesCleanedUpAndStopped() throws IllegalAccessException {
        Thread releaserThread = getReleaserThread();
        if (BackgroundResourceReleaser.BG_RELEASER)
            assertNotNull(releaserThread, "thread reference should exist");
        int count = 20;
        BackgroundResourceReleaserSupport.createResources(1, count, closed, released);
        BackgroundResourceReleaserSupport.assertExpectedCounts(count, closed, released);
        BackgroundResourceReleaserSupport.exerciseCloseableAndReferenceCounted(closed, released, !BackgroundResourceReleaser.BG_RELEASER);

        BackgroundResourceReleaserSupport.WaitingCloseable wc = BackgroundResourceReleaserSupport.createWaitingCloseable();
        new Thread(wc::close).start();
        wc.close();

        BackgroundResourceReleaser.stop();
        assertEquals(count, closed.get(), "all resources should be closed after stopping background releaser");
        assertEquals(count, released.get(), "all resources should be released after stopping background releaser");
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
        assertNull(getReleaserThread(), "releaser thread should not exist in foreground cleanup mode");
        int count = 20;
        BackgroundResourceReleaserSupport.createResources(0, count - 1, closed, released);
        assertEquals(count - 1, closed.get(), "resources should be closed in foreground before waiting closeable");
        assertEquals(count - 1, released.get(), "resources should be released in foreground before waiting closeable");
        BackgroundResourceReleaserSupport.exerciseCloseableAndReferenceCounted(closed, released, true);

        BackgroundResourceReleaserSupport.WaitingCloseable wc = BackgroundResourceReleaserSupport.createWaitingCloseable();
        new Thread(wc::close).start();
        wc.close();
        assertEquals(count, closed.get(), "all resources should be closed after waiting closeable completes");
        assertEquals(count, released.get(), "all resources should be released after waiting closeable completes");

        // Does nothing
        BackgroundResourceReleaser.releasePendingResources();
        assertEquals(count, closed.get(), "resource count should remain unchanged after no-op release");
        assertEquals(count, released.get(), "released count should remain unchanged after no-op release");
        AbstractCloseable.assertCloseablesClosed();
    }

    private Thread getReleaserThread() throws IllegalAccessException {
        return (Thread) Jvm.getField(BackgroundResourceReleaser.class, "RELEASER").get(null);
    }
}
