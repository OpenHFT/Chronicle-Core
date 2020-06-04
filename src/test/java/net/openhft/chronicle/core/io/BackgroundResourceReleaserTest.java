package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class BackgroundResourceReleaserTest {
    static final AtomicLong closed = new AtomicLong();
    static final AtomicLong released = new AtomicLong();

    @Test
    public void testResourcesCleanedUp() {
        assumeTrue(BackgroundResourceReleaser.BG_RELEASER);
        int count = 10;
        for (int i = 0; i < count; i++) {
            new BGCloseable().close();
            new BGReferenceCounted().releaseLast();
        }
        assertEquals(1, closed.get(), 1);
        assertEquals(1, released.get(), 1);
        long start = System.currentTimeMillis();
        BackgroundResourceReleaser.releasePendingResources();
        long time = System.currentTimeMillis() - start;
        assertEquals(count * 10 + 20, time, 30);
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
    }

    static class BGCloseable extends AbstractCloseable {
        @Override
        protected boolean performCloseInBackground() {
            return true;
        }

        @Override
        protected void performClose() {
            closed.incrementAndGet();
            Jvm.pause(10);
        }
    }

    static class BGReferenceCounted extends AbstractReferenceCounted {
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