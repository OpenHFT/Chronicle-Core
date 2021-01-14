package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class BackgroundResourceReleaserTest {
    final AtomicLong closed = new AtomicLong();
    final AtomicLong released = new AtomicLong();

    @Test
    public void testResourcesCleanedUp() {
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
        int error = Jvm.isArm() || OS.isWindows() ? 15 : 7;
        assertEquals(10 + error, time0, error);

        long start = System.currentTimeMillis();
        BackgroundResourceReleaser.releasePendingResources();
        long time = System.currentTimeMillis() - start;
        assertBetween(count * 4, time, count * 11);
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
    }

    private void assertBetween(long min, long actual, long max) {
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
        protected boolean performReleaseInBackground() {
            return true;
        }

        @Override
        protected void performRelease() {
            released.incrementAndGet();
            Jvm.pause(10);
        }
    }

}