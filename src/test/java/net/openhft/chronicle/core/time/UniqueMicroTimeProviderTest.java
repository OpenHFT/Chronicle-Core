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

package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UniqueMicroTimeProviderTest extends CoreTestCommon {
    private UniqueMicroTimeProvider timeProvider;
    private SetTimeProvider setTimeProvider;

    @Before
    public void setUp() {
        timeProvider = new UniqueMicroTimeProvider();
        setTimeProvider = new SetTimeProvider(SystemTimeProvider.INSTANCE.currentTimeNanos());
        timeProvider.provider(setTimeProvider);
    }

    @Test
    public void shouldProvideUniqueTimeAcrossThreads() throws InterruptedException {
        final Set<Long> timeSet = ConcurrentHashMap.newKeySet();
        final int numThreads = 100;
        final int numIterations = 1_000;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.execute(() -> {
                for (int j = 0; j < numIterations; j++) {
                    setTimeProvider.advanceNanos(j);
                    timeSet.add(timeProvider.currentTimeMicros());
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals("All timestamps should be unique", numThreads * numIterations, timeSet.size());
    }

    @Test
    public void shouldAdvanceTimeWhenExceedingCallsPerSecond() {
        final int iterations = 1_000_001;
        long lastTimeMicros = 0;

        for (int i = 0; i < iterations; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeMicros = timeProvider.currentTimeMicros();
            assertTrue("Each timestamp must be greater than the last", currentTimeMicros > lastTimeMicros);
            lastTimeMicros = currentTimeMicros;
        }
    }

    @Test
    public void currentTimeMillisShouldBeCorrect() {
        int iterations = 1_000;
        long lastTimeMillis = 0;
        final long startTimeMillis = setTimeProvider.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeMillis = timeProvider.currentTimeMillis();
            assertTrue(currentTimeMillis >= startTimeMillis);
            assertTrue(currentTimeMillis <= startTimeMillis + iterations);
            assertTrue("Millisecond timestamps must increase", currentTimeMillis > lastTimeMillis);
            lastTimeMillis = currentTimeMillis;
        }
    }

    @Test
    public void currentTimeMicrosShouldBeCorrect() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeMicros = timeProvider.currentTimeMicros();
            assertTrue("Microsecond timestamps must increase", currentTimeMicros > lastTimeMicros);
            lastTimeMicros = currentTimeMicros;
        }
    }

    @Test
    public void currentTimeNanosShouldBeCorrect() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeNanos = timeProvider.currentTimeNanos();
            assertTrue("Nanosecond timestamps adjusted to microsecond level should increase", currentTimeNanos / 1000 > lastTimeMicros);
            lastTimeMicros = currentTimeNanos / 1000;
        }
    }
}

