package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class WeakReferenceCleanerTest {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicInteger processedCount = new AtomicInteger(0);

    @Before
    public void setUp() throws Exception {
        WeakReferenceCleaner.startReferenceProcessor(executorService);
    }

    @Test
    public void shouldRunThunkAfterReferenceIsProcessed() throws Exception {
        Container a = allocate("a");
        Container b = allocate("b");
        Container c = allocate("c");

        System.gc();
        System.gc();
        waitForProcessedCount(0);

        referToObject(a);
        referToObject(b);
        referToObject(c);
        assertThat(processedCount.get(), is(0));
        a = null;

        System.gc();
        System.gc();
        waitForProcessedCount(1);

        referToObject(b);
        referToObject(c);
        b = null;
        c = null;

        System.gc();
        System.gc();
        waitForProcessedCount(3);
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdownNow();
    }

    private static void referToObject(final Container container) {
        if("This is unexpected".equals(container.toString())) {
            fail("This is unexpected");
        }
    }

    @NotNull
    private Container allocate(final String data) {
        return new Container(data);
    }

    private void waitForProcessedCount(final int expectedCount) {
        final long timeoutAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(45L);
        while (System.currentTimeMillis() < timeoutAt) {
            if (expectedCount == processedCount.get()) {
                return;
            }
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1L));
            System.gc();
        }

        fail(String.format("Processed count did not reach %d, was %d", expectedCount, processedCount.get()));
    }

    private final class Container {
        private WeakReferenceCleaner cleaner;
        private final String data;

        Container(final String data) {
            this.data = data;
            this.cleaner = WeakReferenceCleaner.newCleaner(this, processedCount::incrementAndGet);
        }
    }
}