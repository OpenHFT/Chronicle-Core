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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UniqueMicroTimeProviderTest extends CoreTestCommon {
    private UniqueMicroTimeProvider timeProvider;
    private SetTimeProvider setTimeProvider;

    @Before
    public void setUp() {
        timeProvider = new UniqueMicroTimeProvider();
        setTimeProvider = new SetTimeProvider(0);
        timeProvider.provider(setTimeProvider);
    }

    @Test
    public void shouldProvideUniqueTimeAcrossThreadsMillis() throws InterruptedException {
        final Set<Long> allGeneratedTimestamps = ConcurrentHashMap.newKeySet();
        final int numberOfThreads = 100;
        final int iterationsPerThread = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.execute(() -> {
                try {
                    List<Long> threadTimeSet = new ArrayList<>(iterationsPerThread);
                    long lastTimestamp = 0;
                    for (int j = 0; j < iterationsPerThread; j++) {

                        // there could be a race condition for the next two methods, but it shouldn't matter for this test
                        setTimeProvider.advanceMicros(j * 100);
                        long currentTimeMillis = timeProvider.currentTimeMillis();

                        threadTimeSet.add(currentTimeMillis);
                        assertTrue("Timestamps should always increase", currentTimeMillis > lastTimestamp);
                        lastTimestamp = currentTimeMillis;
                    }
                    allGeneratedTimestamps.addAll(threadTimeSet);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals("All timestamps across all threads and iterations should be unique",
                numberOfThreads * iterationsPerThread, allGeneratedTimestamps.size());
    }

    @Test
    public void shouldProvideUniqueTimeAcrossThreadsMicros() throws InterruptedException {
        final Set<Long> allGeneratedTimestamps = ConcurrentHashMap.newKeySet();
        final int numberOfThreads = 50;
        final int factor = 50;
        final int iterationsPerThread = 1000;
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads * factor);

        for (int i = 0; i < numberOfThreads * factor; i++) {
            executor.execute(() -> {
                try {
                    List<Long> threadTimeSet = new ArrayList<>(iterationsPerThread);
                    long lastTimestamp = 0;
                    for (int j = 0; j < iterationsPerThread; j++) {

                        // there could be a race condition for the next two methods, but it shouldn't matter for this test
                        setTimeProvider.advanceMicros(j);
                        long currentTimeMicros = timeProvider.currentTimeMicros();

                        threadTimeSet.add(currentTimeMicros);
                        assertTrue("Timestamps should always increase", currentTimeMicros > lastTimestamp);
                        lastTimestamp = currentTimeMicros;
                    }
                    allGeneratedTimestamps.addAll(threadTimeSet);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals("All timestamps across all threads and iterations should be unique",
                numberOfThreads * iterationsPerThread * factor, allGeneratedTimestamps.size());
    }

    @Test
    public void shouldProvideUniqueTimeAcrossThreadsNanos() throws InterruptedException {
        final Set<Long> allGeneratedTimestamps = ConcurrentHashMap.newKeySet();
        final int numberOfThreads = 50;
        final int factor = 50;
        final int iterationsPerThread = 500;
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads * factor);

        for (int i = 0; i < numberOfThreads * factor; i++) {
            executor.execute(() -> {
                try {
                    List<Long> threadTimeSet = new ArrayList<>(iterationsPerThread);
                    long lastTimestamp = 0;
                    for (int j = 0; j < iterationsPerThread; j++) {

                        // there could be a race condition for the next two methods, but it shouldn't matter for this test
                        setTimeProvider.advanceNanos(j);
                        long currentTimeNanos = timeProvider.currentTimeNanos();

                        threadTimeSet.add(currentTimeNanos);
                        assertTrue("Timestamps should always be in the next micros", currentTimeNanos / 1000 > lastTimestamp / 1000);
                        lastTimestamp = currentTimeNanos;
                    }
                    allGeneratedTimestamps.addAll(threadTimeSet);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals("All timestamps across all threads and iterations should be unique",
                numberOfThreads * iterationsPerThread * factor, allGeneratedTimestamps.size());
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
    public void currentTimeMicrosShouldBeCorrectBackwards() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(-i);
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

    /* prints
now 1720082371 1720082371.000116654 diff 1.1682510375976562E-4
now 1720082372 1720082372.000126830 diff 1.2683868408203125E-4
now 1720082373 1720082373.000039087 diff 3.910064697265625E-5
now 1720082374 1720082374.000082835 diff 8.296966552734375E-5
now 1720082375 1720082375.000135960 diff 1.3589859008789062E-4
now 1720082376 1720082376.000064154 diff 6.437301635742188E-5
now 1720082377 1720082377.000150545 diff 1.5044212341308594E-4
     */
    // Long running test for drift
    public static void main(String[] args) {
        UniqueMicroTimeProvider tp = UniqueMicroTimeProvider.INSTANCE;
        long last = 0;
        long print = 0;
        while (true) {
            for (int i = 0; i < 100; i++) {
                long now = tp.currentTimeNanos();
                if (now <= last)
                    assertTrue("Should be positive" + now + " " + last + " diff " + (now - last), now > last);
                last = now;
                long nowSec = System.currentTimeMillis() / 1000;
                if (nowSec > print)
                    break;
            }
            long nowSec = System.currentTimeMillis() / 1000;
            if (nowSec > print) {
                print = nowSec;
                BigDecimal lastSec = BigDecimal.valueOf(last).divide(BigDecimal.valueOf(1_000_000_000), 9, BigDecimal.ROUND_HALF_UP);
                System.out.println("now " + nowSec + " " + lastSec + " diff " + (last / 1e9 - nowSec));
            }
            LockSupport.parkNanos(100_000);
        }
    }
}
