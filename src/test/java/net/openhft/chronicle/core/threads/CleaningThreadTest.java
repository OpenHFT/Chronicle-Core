package net.openhft.chronicle.core.threads;

import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CleaningThreadTest {
    @Test
    public void cleanupThreadLocal() throws InterruptedException {
        BlockingQueue<String> ints = new LinkedBlockingQueue<>();
        CleaningThreadLocal<String> counter = CleaningThreadLocal.withCleanup(() -> Thread.currentThread().getName(), ints::add);
        CleaningThread ct = new CleaningThread(() -> {
            assertNotNull(counter.get());
        }, "ctl-test");
        ct.start();
        String poll = ints.poll(1, TimeUnit.SECONDS);
        assertEquals("ctl-test", poll);
    }

}