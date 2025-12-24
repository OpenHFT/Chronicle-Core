/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniqueMicroTimeProviderTest extends CoreTestCommon {
    private UniqueMicroTimeProvider timeProvider;
    private SetTimeProvider setTimeProvider;

    @BeforeEach
    public void setUp() {
        timeProvider = new UniqueMicroTimeProvider();
        setTimeProvider = new SetTimeProvider(0);
        timeProvider.provider(setTimeProvider);
    }

    @Test
    void shouldProvideUniqueTimeAcrossThreadsMillis() throws InterruptedException {
        final Set<Long> allGeneratedTimestamps = ConcurrentHashMap.newKeySet();
        final int numberOfThreads = 100;
        final int iterationsPerThread = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            executor.execute(() -> {
                try {
                    List<Long> threadTimeSet = new ArrayList<>(iterationsPerThread);
                    long lastTimestamp = 0;
                    for (int j = 0; j < iterationsPerThread; j++) {

                        // there could be a race condition for the next two methods, but it shouldn't matter for this test
                        setTimeProvider.advanceMicros(j * 100L);
                        long currentTimeMillis = timeProvider.currentTimeMillis();

                        threadTimeSet.add(currentTimeMillis);
                        assertTrue(currentTimeMillis > lastTimestamp,
                                "millis timestamp " + currentTimeMillis + " should be > " + lastTimestamp
                                        + " for thread " + threadIndex + " at iteration " + j);
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

        long expectedMillisCount = (long) numberOfThreads * iterationsPerThread;
        assertEquals(expectedMillisCount, allGeneratedTimestamps.size(),
                "all millis timestamps across threads and iterations should be unique");
    }

    @Test
    void shouldProvideUniqueTimeAcrossThreadsMicros() throws InterruptedException {
        final Set<Long> allGeneratedTimestamps = ConcurrentHashMap.newKeySet();
        final int numberOfThreads = 50;
        final int factor = 50;
        final int iterationsPerThread = 1000;
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads * factor);

        for (int i = 0; i < numberOfThreads * factor; i++) {
            final int threadIndex = i;
            executor.execute(() -> {
                try {
                    List<Long> threadTimeSet = new ArrayList<>(iterationsPerThread);
                    long lastTimestamp = 0;
                    for (int j = 0; j < iterationsPerThread; j++) {

                        // there could be a race condition for the next two methods, but it shouldn't matter for this test
                        setTimeProvider.advanceMicros(j);
                        long currentTimeMicros = timeProvider.currentTimeMicros();

                        threadTimeSet.add(currentTimeMicros);
                        assertTrue(currentTimeMicros > lastTimestamp,
                                "thread micros timestamp " + currentTimeMicros + " should be > " + lastTimestamp
                                        + " for thread " + threadIndex + " at iteration " + j);
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

        long expectedMicrosCount = (long) numberOfThreads * iterationsPerThread * factor;
        assertEquals(expectedMicrosCount, allGeneratedTimestamps.size(),
                "all micros timestamps across threads and iterations should be unique");
    }

    @Test
    void shouldProvideUniqueTimeAcrossThreadsNanos() throws InterruptedException {
        final Set<Long> allGeneratedTimestamps = ConcurrentHashMap.newKeySet();
        final int numberOfThreads = 50;
        final int factor = 50;
        final int iterationsPerThread = 500;
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads * factor);

        for (int i = 0; i < numberOfThreads * factor; i++) {
            final int threadIndex = i;
            executor.execute(() -> {
                try {
                    List<Long> threadTimeSet = new ArrayList<>(iterationsPerThread);
                    long lastTimestamp = 0;
                    for (int j = 0; j < iterationsPerThread; j++) {

                        // there could be a race condition for the next two methods, but it shouldn't matter for this test
                        setTimeProvider.advanceNanos(j);
                        long currentTimeNanos = timeProvider.currentTimeNanos();
                        long currentMicros = currentTimeNanos / 1000;
                        long lastMicros = lastTimestamp / 1000;

                        threadTimeSet.add(currentTimeNanos);
                        assertTrue(currentMicros > lastMicros,
                                "thread nanos micros " + currentMicros + " should be > " + lastMicros
                                        + " for thread " + threadIndex + " at iteration " + j);
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

        long expectedNanosCount = (long) numberOfThreads * iterationsPerThread * factor;
        assertEquals(expectedNanosCount, allGeneratedTimestamps.size(),
                "all nanos timestamps across threads and iterations should be unique");
    }

    @Test
    void shouldAdvanceTimeWhenExceedingCallsPerSecond() {
        final int iterations = 1_000_001;
        long lastTimeMicros = 0;

        for (int i = 0; i < iterations; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeMicros = timeProvider.currentTimeMicros();
            assertTrue(currentTimeMicros > lastTimeMicros,
                    "rate-limit micros timestamp " + currentTimeMicros + " should be > " + lastTimeMicros
                            + " at iteration " + i);
            lastTimeMicros = currentTimeMicros;
        }
    }

    @Test
    void currentTimeMillisShouldBeCorrect() {
        int iterations = 1_000;
        long lastTimeMillis = 0;
        final long startTimeMillis = setTimeProvider.currentTimeMillis();
        final long maxTimeMillis = startTimeMillis + iterations;

        for (int i = 0; i < iterations; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeMillis = timeProvider.currentTimeMillis();
            assertTrue(currentTimeMillis >= startTimeMillis,
                    "current millis " + currentTimeMillis + " should be >= " + startTimeMillis + " at iteration " + i);
            assertTrue(currentTimeMillis <= maxTimeMillis,
                    "current millis " + currentTimeMillis + " should be <= " + maxTimeMillis + " at iteration " + i);
            assertTrue(currentTimeMillis > lastTimeMillis,
                    "current millis " + currentTimeMillis + " should be > " + lastTimeMillis + " at iteration " + i);
            lastTimeMillis = currentTimeMillis;
        }
    }

    @Test
    void currentTimeMicrosShouldBeCorrect() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeMicros = timeProvider.currentTimeMicros();
            assertTrue(currentTimeMicros > lastTimeMicros,
                    "forward micros timestamp " + currentTimeMicros + " should be > " + lastTimeMicros
                            + " at iteration " + i);
            lastTimeMicros = currentTimeMicros;
        }
    }

    @Test
    void currentTimeMicrosShouldBeCorrectBackwards() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(-i);
            long currentTimeMicros = timeProvider.currentTimeMicros();
            assertTrue(currentTimeMicros > lastTimeMicros,
                    "backward micros timestamp " + currentTimeMicros + " should be > " + lastTimeMicros
                            + " at iteration " + i);
            lastTimeMicros = currentTimeMicros;
        }
    }

    @Test
    void currentTimeNanosShouldBeCorrect() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeNanos = timeProvider.currentTimeNanos();
            long currentMicros = currentTimeNanos / 1000;
            assertTrue(currentMicros > lastTimeMicros,
                    "current nanos micros " + currentMicros + " should be > " + lastTimeMicros
                            + " at iteration " + i);
            lastTimeMicros = currentMicros;
        }
    }
}
