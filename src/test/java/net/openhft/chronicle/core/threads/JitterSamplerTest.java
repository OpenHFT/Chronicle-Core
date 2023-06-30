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

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Tests the behavior of the {@link JitterSampler} class.
 */
public class JitterSamplerTest extends CoreTestCommon {

    /**
     * Tests the snapshot creation of JitterSampler with different thread stages.
     */
    @Test
    public void shouldTakeSnapshotAtDifferentStages() throws InterruptedException {
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
                if (s != null && s.contains("finish")) {
                    break;
                }
            } else {
                assertEquals("Expected 'started' stage description.", "started", desc);
            }
        }

        t.join();

        String s = JitterSampler.takeSnapshot();
        assertNull("Expected null snapshot after thread completion.", s);
    }

    /**
     * Waits for a CountDownLatch to reach zero.
     *
     * @param latch the CountDownLatch
     */
    private void waitForLatch(CountDownLatch latch) {
        latch.countDown();
        try {
            assertTrue("Latch did not reach zero within the time limit.", latch.await(3, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted waiting for latch", e);
        }
    }
}
