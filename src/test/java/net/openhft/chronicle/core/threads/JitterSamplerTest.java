package net.openhft.chronicle.core.threads;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JitterSamplerTest {

    protected static void sleepSilently(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void takeSnapshot() throws InterruptedException {
        Thread t = new Thread(() -> {
            JitterSampler.atStage("started");
            sleepSilently(60);
            JitterSampler.atStage("finishing");
            sleepSilently(20);
            JitterSampler.finished();
        });
        t.start();
        sleepSilently(20);
        for (int i = 0; i < 10; i++) {
            sleepSilently(10);
            String s = JitterSampler.takeSnapshot(10_000_000);
            System.out.println(s);
            assertNotNull(s);
            if (s.contains("finish"))
                break;
        }
        t.join();
        String s = JitterSampler.takeSnapshot(10_000_000);
        assertNull(s);
    }
}