package net.openhft.chronicle.core;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StackTraceTest {
    private static final CountDownLatch threadStarted = new CountDownLatch(1);

    static void thinking() {
        threadStarted.countDown();
        Jvm.pause(5_000);
    }

    @Test
    public void forThread() throws InterruptedException {
        // load Jvm class before
        Jvm.init();
        Thread t = new Thread(StackTraceTest::thinking, "background");
        t.start();
        boolean started = threadStarted.await(1, TimeUnit.SECONDS);
        assertTrue(started);
        // wait for Jvm.pause to start
        Jvm.pause(50);
        StackTrace st = StackTrace.forThread(t);
        t.interrupt();
        assertEquals("Thread[background,5,main] on main", st.getMessage());
        assertEquals("net.openhft.chronicle.core.Jvm.pause", st.getStackTrace()[0].toString().split("\\(")[0].replaceAll("^app//", ""));
    }
}