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

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

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
            }
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    public void runResourcesCleanedUpManually() throws IllegalAccessException {
        assumeTrue(BackgroundResourceReleaser.BG_RELEASER);
        assertNull(getReleaserThread());
        int count = 20;
        for (int i = 1; i < count; i++) {
            new BGCloseable().close();
            new BGReferenceCounted().releaseLast();
        }
        assertEquals(2, closed.get(), 2);
        assertEquals(2, released.get(), 2);
        BGCloseable bgc = new BGCloseable();
        bgc.close();
        assertTrue(bgc.isClosing());
        assertFalse(bgc.isClosed());

        BGReferenceCounted bgr = new BGReferenceCounted();
        bgr.releaseLast();
        assertEquals(0, bgr.refCount());

        WaitingCloseable wc = new WaitingCloseable();
        new Thread(wc::close).start();
        wc.close();
        assertNotEquals(count, closed.get());
        assertNotEquals(count, released.get());

        BackgroundResourceReleaser.releasePendingResources();
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
        BackgroundResourceReleaser.releasePendingResources();
    }

    public void runResourcesCleanedUpAndStopped() throws IllegalAccessException {
        assumeTrue(BackgroundResourceReleaser.BG_RELEASER);
        assertNotNull(getReleaserThread());
        int count = 20;
        for (int i = 1; i < count; i++) {
            new BGCloseable().close();
            new BGReferenceCounted().releaseLast();
        }
        assertEquals(2, closed.get(), 2);
        assertEquals(2, released.get(), 2);
        BGCloseable bgc = new BGCloseable();
        bgc.close();
        assertTrue(bgc.isClosing());
        assertFalse(bgc.isClosed());

        BGReferenceCounted bgr = new BGReferenceCounted();
        bgr.releaseLast();
        assertEquals(0, bgr.refCount());

        WaitingCloseable wc = new WaitingCloseable();
        new Thread(wc::close).start();
        wc.close();

        BackgroundResourceReleaser.stop();
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
        BackgroundResourceReleaser.stop();

        for (int i = 1; i < 1000; i += i) {
            if (!getReleaserThread().isAlive()) return;

            Jvm.pause(i);
        }

        fail("Resource releaser thread did not terminate.");
    }

    public void runResourcesCleanedUpInForeground() throws IllegalAccessException {
        assumeFalse(BackgroundResourceReleaser.BG_RELEASER);
        assertNull(getReleaserThread());
        int count = 20;
        for (int i = 1; i < count; i++) {
            new BGCloseable().close();
            new BGReferenceCounted().releaseLast();
        }
        assertEquals(count - 1, closed.get(), 1);
        assertEquals(count - 1, released.get(), 1);
        BGCloseable bgc = new BGCloseable();
        bgc.close();
        assertTrue(bgc.isClosing());
        assertTrue(bgc.isClosed());

        BGReferenceCounted bgr = new BGReferenceCounted();
        bgr.releaseLast();
        assertEquals(0, bgr.refCount());

        WaitingCloseable wc = new WaitingCloseable();
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