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

public class UniqueMicroTimeProviderTest extends CoreTestCommon {
    private UniqueMicroTimeProvider timeProvider;
    private SetTimeProvider setTimeProvider;

    @BeforeEach
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
                        assertTrue(currentTimeMillis > lastTimestamp, "Timestamps should always increase");
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

        assertEquals(numberOfThreads * iterationsPerThread,
                allGeneratedTimestamps.size(), "All timestamps across all threads and iterations should be unique");
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
                        assertTrue(currentTimeMicros > lastTimestamp, "Timestamps should always increase");
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

        assertEquals(numberOfThreads * iterationsPerThread * factor,
                allGeneratedTimestamps.size(), "All timestamps across all threads and iterations should be unique");
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
                        assertTrue(currentTimeNanos / 1000 > lastTimestamp / 1000, "Timestamps should always be in the next micros");
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

        assertEquals(numberOfThreads * iterationsPerThread * factor,
                allGeneratedTimestamps.size(), "All timestamps across all threads and iterations should be unique");
    }

    @Test
    public void shouldAdvanceTimeWhenExceedingCallsPerSecond() {
        final int iterations = 1_000_001;
        long lastTimeMicros = 0;

        for (int i = 0; i < iterations; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeMicros = timeProvider.currentTimeMicros();
            assertTrue(currentTimeMicros > lastTimeMicros, "Each timestamp must be greater than the last");
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
            assertTrue(currentTimeMillis > lastTimeMillis, "Millisecond timestamps must increase");
            lastTimeMillis = currentTimeMillis;
        }
    }

    @Test
    public void currentTimeMicrosShouldBeCorrect() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeMicros = timeProvider.currentTimeMicros();
            assertTrue(currentTimeMicros > lastTimeMicros, "Microsecond timestamps must increase");
            lastTimeMicros = currentTimeMicros;
        }
    }

    @Test
    public void currentTimeMicrosShouldBeCorrectBackwards() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(-i);
            long currentTimeMicros = timeProvider.currentTimeMicros();
            assertTrue(currentTimeMicros > lastTimeMicros, "Microsecond timestamps must increase");
            lastTimeMicros = currentTimeMicros;
        }
    }

    @Test
    public void currentTimeNanosShouldBeCorrect() {
        long lastTimeMicros = 0;

        for (int i = 0; i < 4_000; i++) {
            setTimeProvider.advanceNanos(i);
            long currentTimeNanos = timeProvider.currentTimeNanos();
            assertTrue(currentTimeNanos / 1000 > lastTimeMicros, "Nanosecond timestamps adjusted to microsecond level should increase");
            lastTimeMicros = currentTimeNanos / 1000;
        }
    }
}
