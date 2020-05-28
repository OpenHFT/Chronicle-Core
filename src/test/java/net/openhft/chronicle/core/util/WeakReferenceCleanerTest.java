package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static net.openhft.chronicle.core.util.WeakReferenceCleaner.THREAD_SHUTTING_DOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WeakReferenceCleanerTest {

    private final AtomicInteger processedCount = new AtomicInteger(0);

    private static void referToObject(final Container container) {
        if ("This is unexpected".equals(container.toString())) {
            fail("This is unexpected");
        }
    }

    @BeforeClass
    public static void setUp() {
        WeakReferenceCleaner.startCleanerThreadIfNotStarted();
    }

    @AfterClass
    public static void tearDown() {
        THREAD_SHUTTING_DOWN.set(true);
    }

    @Test
    public void shouldRunOnceWhenRequested() {
        final Container foo = allocate("foo");
        assertEquals(0, processedCount.get());

        foo.cleaner.clean();
        assertEquals(1, processedCount.get());

        foo.cleaner.clean();
        assertEquals(1, processedCount.get());
    }

    @Test
    public void shouldRunOnceWhenRequestedScheduled() {
        final Container foo = allocate("foo");
        assertEquals(0, processedCount.get());

        foo.cleaner.scheduleForClean();
        waitForProcessedCount(1);
        assertEquals(1, processedCount.get());

        foo.cleaner.clean();
        assertEquals(1, processedCount.get());
    }

    @Test
    public void shouldRunThunkAfterReferenceIsProcessed() {
        Container a = allocate("a");
        Container b = allocate("b");
        Container c = allocate("c");

        waitForProcessedCount(0);

        referToObject(a);
        referToObject(b);
        referToObject(c);
        assertEquals(0, processedCount.get());
        a = null;

        waitForProcessedCount(1);

        referToObject(b);
        referToObject(c);
        b = null;
        c = null;

        waitForProcessedCount(3);
    }

    @NotNull
    private Container allocate(final String data) {
        return new Container(data);
    }

    private void waitForProcessedCount(final int expectedCount) {
        final long timeoutAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(45L);
        while (System.currentTimeMillis() < timeoutAt) {
            System.gc();
            if (expectedCount == processedCount.get())
                return;

            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1L));
        }

        fail(String.format("Processed count did not reach %d, was %d", expectedCount, processedCount.get()));
    }

    private final class Container {
        private final WeakReferenceCleaner cleaner;
        private final String data;

        Container(final String data) {
            this.data = data;
            this.cleaner = WeakReferenceCleaner.newCleaner(this, processedCount::incrementAndGet);
        }
    }
}