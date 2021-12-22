package net.openhft.chronicle.core.internal.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.threads.InterruptedRuntimeException;
import net.openhft.chronicle.core.threads.ThreadLock;
import net.openhft.chronicle.core.values.LongValue;

import java.io.File;

public class VanillaThreadLock implements ThreadLock {
    static final Metrics METRICS = new Metrics();
    private static final long TOP_BITS = ~0L << 32;
    private static final int UNLOCKED = 0;
    private final LongValue twoThreadId;
    private final long timeoutMs;
    int busyLoopCount = 20_000;
    int busyLockSlowerCount = 100;
    private long oldLock;

    public VanillaThreadLock(LongValue twoThreadId, long timeoutMs) {
        this.twoThreadId = twoThreadId;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public boolean tryLock(int threadId) throws IllegalStateException {
        if (threadId == 0)
            throw new IllegalArgumentException();
        long twoThreadId0 = twoThreadId.getVolatileValue();
        int threadId0 = (int) twoThreadId0;
        if (threadId0 != UNLOCKED) {
            if (threadId0 == threadId)
                throw new IllegalStateException("trying to lock twice");
            return false;
        }
        return twoThreadId.compareAndSwapValue(twoThreadId0, (twoThreadId0 & TOP_BITS) | threadId);
    }

    @Override
    public void lock(int threadId) throws InterruptedRuntimeException {
        if (tryLock(threadId))
            return;
        busyLock(threadId);
    }

    private void busyLock(int threadId) {
        // busy wait
        for (int i = 0; i < busyLoopCount; i++) {
            if (tryLock(threadId))
                return;
            Jvm.nanoPause();
        }
        if (busyLockSlower(threadId))
            return;
        forceUnlockAndRetry(threadId);
    }

    @SuppressWarnings("java:S3776")
    private boolean busyLockSlower(int threadId) throws InterruptedRuntimeException {
        oldLock = 0;
        long endMS = System.currentTimeMillis() + timeoutMs;
        do {
            final long currLock = twoThreadId.getVolatileValue();
            if (oldLock == 0)
                oldLock = currLock;
            int lastThreadId = (int) currLock;
            for (int i = 0; i < busyLockSlowerCount; i++) {
                if (tryLock(threadId))
                    return true;
                int currThreadId = (int) twoThreadId.getVolatileValue();
                if (currThreadId != lastThreadId) {
                    Jvm.perf().on(getClass(), "Owning thread changed from " + lastThreadId + " to " + currThreadId);
                    lastThreadId = currThreadId;
                    oldLock = currLock;
                    endMS = System.currentTimeMillis() + timeoutMs;
                }
                Thread.yield();
            }
            if (METRICS.supportsProc) {
                int lockedThread = (int) twoThreadId.getVolatileValue();
                if (lockedThread == 0)
                    continue;
                if (!isThreadRunning(lockedThread)) {
                    Jvm.warn().on(getClass(), "ThreadId " + lockedThread + " died while holding a lock");
                    return false;
                }
            }
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedRuntimeException();

        } while (System.currentTimeMillis() < endMS);
        return false;
    }

    private boolean isThreadRunning(int lockedThread) {
        return new File("/proc/" + lockedThread).exists();
    }

    @SuppressWarnings("java:S3358")
    private void forceUnlockAndRetry(int threadId) {
        long twoThreadId0 = twoThreadId.getVolatileValue();
        int threadId0 = (int) twoThreadId0;

        // unlock the previous thread
        if (threadId0 != UNLOCKED && twoThreadId0 == oldLock) {
            if (twoThreadId.compareAndSwapValue(oldLock, ((long) threadId0 << 32) | UNLOCKED)) {
                String status = METRICS.supportsProc
                        ? isThreadRunning(threadId0) ? "running" : "dead"
                        : "unknown";
                Jvm.warn().on(getClass(), "Successfully forced an unlock for threadId: " + threadId + ", previous thread held by: " + threadId0 + ", status: " + status);
            } else {
                Jvm.warn().on(getClass(), "Failed to forced an unlock for threadId: " + threadId);
            }
        }
        lock(threadId);
    }

    @Override
    public void unlock(int threadId) throws IllegalStateException {
        long twoThreadId0 = twoThreadId.getVolatileValue();
        int threadId0 = (int) twoThreadId0;
        if (threadId0 != threadId) {
            unlock2(threadId, twoThreadId0, threadId0);
            return;
        }

        if (twoThreadId.compareAndSwapValue(twoThreadId0, ((long) threadId << 32) | UNLOCKED))
            return;
        Jvm.warn().on(getClass(), "Failed to unlock");
    }

    private void unlock2(int threadId, long twoThreadId0, int threadId0) throws IllegalStateException {
        if (threadId0 == UNLOCKED) {
            int prevThreadId = (int) (twoThreadId0 >>> 32);
            if (prevThreadId == threadId)
                throw new IllegalStateException("Lock already unlocked by threadId " + threadId);
            else
                Jvm.warn().on(getClass(), "Lock previously held by another thread " + prevThreadId + " not mine " + threadId);
        } else {
            Jvm.warn().on(getClass(), "Lock held by another thread " + threadId0 + " not mine " + threadId);
        }
    }

    static class Metrics {
        final boolean supportsProc;

        @SuppressWarnings("java:S1075")
        Metrics() {
            this.supportsProc = new File("/proc").isDirectory();
            if (OS.isLinux() && !supportsProc) Jvm.warn().on(getClass(), "/proc not found on Linux");
        }
    }
}
