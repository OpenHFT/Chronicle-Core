package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;

public class JitterSamplerTest {

    @Test
    public void takeSnapshot() throws InterruptedException {

//        Assume.assumeTrue(!Jvm.isArm());
        Thread t = new Thread(() -> {
            JitterSampler.atStage("started");
            int millis = Jvm.isArm() ? 120 : 60;
            JitterSampler.sleepSilently(millis);
            JitterSampler.atStage("finishing");
            JitterSampler.sleepSilently(millis);
            JitterSampler.finished();
        });
        t.start();
        STARTED:
        {
            for (int j = 20; j >= 0; j--) {
                Jvm.pause(20);
                if (JitterSampler.desc != null)
                    break STARTED;
            }
            fail("Not started");
        }
        for (int i = 0; i < 10; i++) {
            JitterSampler.sleepSilently(10);
            String s = JitterSampler.takeSnapshot(10_000_000);
            //System.out.println(s);
            if ("finishing".equals(JitterSampler.desc)) {
                if (s != null && s.contains("finish"))
                    break;
            } else {
                assertEquals("started", JitterSampler.desc);
            }
        }
        t.join();
        String s = JitterSampler.takeSnapshot();
        assertNull(s);
    }
}