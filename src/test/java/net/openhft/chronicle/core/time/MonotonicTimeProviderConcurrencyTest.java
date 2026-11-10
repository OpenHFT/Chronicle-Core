/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Live-clock contention coverage complements the controlled clock-step tests. */
class MonotonicTimeProviderConcurrencyTest extends CoreTestCommon {
    private static final int THREADS = Math.max(8, Math.min(64, 2 * Runtime.getRuntime().availableProcessors()));
    private static final long CALLS_PER_THREAD = 1_000_000;
    private static final int ROUNDS = 5;

    @Test
    @Timeout(value = 300, unit = TimeUnit.SECONDS)
    void currentTimeNanosDoesNotGoDownWithinOneThreadUnderConcurrentCalls() throws InterruptedException {
        for (int r = 1; r <= ROUNDS; r++) {
            Round round = runRound(MonotonicTimeProvider.INSTANCE, THREADS, CALLS_PER_THREAD,
                    Executors.defaultThreadFactory());
            System.out.println("round " + r + ": " + round);
            assertEquals(0, round.stepsBack, "currentTimeNanos() went down within one thread: " + round);
        }
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void currentTimeNanosDoesNotGoDownOnOneThread() throws InterruptedException {
        Round round = runRound(MonotonicTimeProvider.INSTANCE, 1, 10_000_000,
                Executors.defaultThreadFactory());
        assertEquals(0, round.stepsBack, "currentTimeNanos() went down with no other caller: " + round);
    }

    @Test
    @Timeout(15)
    void interruptionStopsWorkersAndPreservesInterrupt() throws InterruptedException {
        List<Thread> workers = new CopyOnWriteArrayList<>();
        Set<Thread> observed = ConcurrentHashMap.newKeySet();
        CountDownLatch entered = new CountDownLatch(2);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread parent = new Thread(() -> {
            try {
                runRound(() -> {
                    if (observed.add(Thread.currentThread()))
                        entered.countDown();
                    return 0;
                }, 2, Long.MAX_VALUE, trackingFactory(workers));
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                interrupted.set(Thread.currentThread().isInterrupted());
            }
        }, "time-round-parent");
        parent.start();
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS), "workers did not start");
            parent.interrupt();
            parent.join(6_000);
            assertFalse(parent.isAlive(), "interrupted round did not stop");
            assertInstanceOf(InterruptedException.class, failure.get());
            assertTrue(interrupted.get(), "round lost the parent's interrupt");
            assertStopped(workers);
        } finally {
            parent.interrupt();
            parent.join(6_000);
        }
    }

    @Test
    @Timeout(15)
    void workerFailureStopsOtherWorkersAndPreservesCause() {
        List<Thread> workers = new CopyOnWriteArrayList<>();
        CountDownLatch otherEntered = new CountDownLatch(1);
        IllegalStateException cause = new IllegalStateException("injected caller failure");
        TimeProvider provider = () -> {
            if (Thread.currentThread().getName().equals("time-caller-0")) {
                try {
                    assertTrue(otherEntered.await(5, TimeUnit.SECONDS), "other worker did not start");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
                throw cause;
            }
            otherEntered.countDown();
            return 0;
        };
        AssertionError failure = assertThrows(AssertionError.class,
                () -> runRound(provider, 2, Long.MAX_VALUE, trackingFactory(workers)));
        assertSame(cause, failure.getCause());
        assertStopped(workers);
    }

    @Test
    @Timeout(15)
    void partialStartupStopsAlreadyStartedWorkers() {
        List<Thread> workers = new CopyOnWriteArrayList<>();
        ThreadFactory delegate = trackingFactory(workers);
        IllegalStateException cause = new IllegalStateException("injected startup failure");
        ThreadFactory factory = runnable -> {
            if (!workers.isEmpty())
                throw cause;
            return delegate.newThread(runnable);
        };
        assertSame(cause, assertThrows(IllegalStateException.class,
                () -> runRound(MonotonicTimeProvider.INSTANCE, 2, Long.MAX_VALUE, factory)));
        assertEquals(1, workers.size());
        assertStopped(workers);
    }

    private static ThreadFactory trackingFactory(List<Thread> workers) {
        AtomicInteger next = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, "time-caller-" + next.getAndIncrement());
            workers.add(thread);
            return thread;
        };
    }

    private static void assertStopped(List<Thread> workers) {
        for (Thread worker : workers)
            assertFalse(worker.isAlive(), "surviving worker: " + worker.getName());
    }

    private static Round runRound(TimeProvider provider, int threadCount, long callsPerThread,
                                  ThreadFactory threadFactory) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> workers = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount, runnable -> {
            Thread thread = threadFactory.newThread(runnable);
            workers.add(thread);
            return thread;
        });
        CompletionService<Caller> completed = new ExecutorCompletionService<>(executor);
        Throwable failure = null;
        try {
            for (int t = 0; t < threadCount; t++)
                completed.submit(new Caller(provider, start, callsPerThread));
            start.countDown();
            Round round = new Round(threadCount, callsPerThread);
            for (int t = 0; t < threadCount; t++) {
                try {
                    round.add(completed.take().get());
                } catch (ExecutionException e) {
                    throw new AssertionError("time caller failed", e.getCause());
                }
            }
            return round;
        } catch (InterruptedException e) {
            failure = e;
            Thread.currentThread().interrupt();
            throw e;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            start.countDown();
            executor.shutdownNow();
            joinWorkers(workers, failure);
        }
    }

    private static void joinWorkers(List<Thread> workers, Throwable failure) {
        boolean interrupted = Thread.interrupted();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        try {
            // Executor termination can be signalled before its last Thread exits.
            for (Thread worker : workers) {
                while (worker.isAlive()) {
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0) {
                        AssertionError cleanup = new AssertionError("time callers did not terminate");
                        if (failure == null)
                            throw cleanup;
                        failure.addSuppressed(cleanup);
                        return;
                    }
                    try {
                        TimeUnit.NANOSECONDS.timedJoin(worker, remaining);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    private static final class Caller implements Callable<Caller> {
        private final TimeProvider provider;
        private final CountDownLatch start;
        private final long calls;
        private long completedCalls;
        private long stepsBack;
        private long largestDropNs;

        Caller(TimeProvider provider, CountDownLatch start, long calls) {
            this.provider = provider;
            this.start = start;
            this.calls = calls;
        }

        @Override
        public Caller call() throws InterruptedException {
            start.await();
            long lastNanos = provider.currentTimeNanos();
            long i = 1;
            for (; i < calls && !Thread.currentThread().isInterrupted(); i++) {
                long nanos = provider.currentTimeNanos();
                if (nanos < lastNanos) {
                    stepsBack++;
                    largestDropNs = Math.max(largestDropNs, lastNanos - nanos);
                }
                lastNanos = nanos;
            }
            completedCalls = i;
            return this;
        }
    }

    private static final class Round {
        private final int threads;
        private final long callsPerThread;
        private long stepsBack;
        private long largestDropNs;

        Round(int threads, long callsPerThread) {
            this.threads = threads;
            this.callsPerThread = callsPerThread;
        }

        void add(Caller caller) {
            assertEquals(callsPerThread, caller.completedCalls, "caller stopped before completing its workload");
            stepsBack += caller.stepsBack;
            largestDropNs = Math.max(largestDropNs, caller.largestDropNs);
        }

        @Override
        public String toString() {
            return "threads=" + threads + " callsPerThread=" + callsPerThread
                    + " stepsBack=" + stepsBack + " largestDropNs=" + largestDropNs;
        }
    }
}
