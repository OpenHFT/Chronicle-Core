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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StackSamplerGptTest {

    private StackSampler stackSampler;

    @BeforeEach
    public void setUp() {
        stackSampler = new StackSampler();
    }

    @AfterEach
    public void tearDown() {
        stackSampler.stop();
    }

    @Test
    public void testSamplingOfThread() throws InterruptedException {
        // Creating a thread that will be sampled
        CountDownLatch latch = new CountDownLatch(1);
        Thread threadToBeSampled = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        });
        threadToBeSampled.start();

        // Setting the thread to be sampled
        stackSampler.thread(threadToBeSampled);

        // Give the sampler some time to sample the stack trace
        Thread.sleep(100);

        // Check that stack trace is sampled
        StackTraceElement[] stackTrace = stackSampler.getAndReset();
        assertNotNull(stackTrace);
        // The length should be more than 0 if the stack was sampled properly
        assert(stackTrace.length > 0);

        // Test that stack trace is reset after getAndReset()
        assertNull(stackSampler.getAndReset());

        // Release the latch so the thread can finish
        latch.countDown();

        // Give the thread some time to finish
        threadToBeSampled.join(1000);
    }
}
