package net.openhft.chronicle.core.threads;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JitterSamplerTest {

    @Test
    public void takeSnapshot() throws InterruptedException {
        Thread t = new Thread(() -> {
            JitterSampler.atStage("started");
            JitterSampler.sleepSilently(60);
            JitterSampler.atStage("finishing");
            JitterSampler.sleepSilently(20);
            JitterSampler.finished();
        });
        t.start();
        JitterSampler.sleepSilently(20);
        for (int i = 0; i < 10; i++) {
            JitterSampler.sleepSilently(10);
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