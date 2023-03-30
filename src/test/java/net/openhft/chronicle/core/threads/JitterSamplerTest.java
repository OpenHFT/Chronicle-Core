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

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class JitterSamplerTest {

    @Test
    public void takeSnapshot() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Thread t = new Thread(() -> {
            JitterSampler.atStage("started");
            waitForLatch(latch);
            int millis = Jvm.isArm() ? 120 : 60;
            Jvm.pause(millis);
            JitterSampler.atStage("finishing");
            Jvm.pause(millis);
            JitterSampler.finished();
        });
        t.start();
        waitForLatch(latch);
        for (int i = 0; i < 10; i++) {
            Jvm.busyWaitMicros(1000);
            String s = JitterSampler.takeSnapshot(10_000_000);
            final String desc = JitterSampler.desc;
            if ("finishing".equals(desc)) {
                if (s != null && s.contains("finish"))
                    break;
            } else {
                assertEquals("started", desc);
            }
        }
        t.join();
        String s = JitterSampler.takeSnapshot();
        assertNull(s);
    }

    private void waitForLatch(CountDownLatch latch) {
        latch.countDown();
        try {
            assertTrue(latch.await(3, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted waiting for latch", e);
        }
    }
}