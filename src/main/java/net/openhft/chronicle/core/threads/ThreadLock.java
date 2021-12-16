package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.values.LongValue;

import java.io.File;

public class ThreadLock {
    static final Metrics METRICS = new Metrics();
    private static final long TOP_BITS = ~0L << 32;
    private static final int UNLOCKED = 0;
    private final LongValue twoThreadId;
    private final long timeoutMs;
    int busyLoopCount = 20_000;
    int busyLockSlowerCount = 100;

    public ThreadLock(LongValue twoThreadId, long timeoutMs) {
        this.twoThreadId = twoThreadId;
        this.timeoutMs = timeoutMs;
    }

    public boolean tryLock(int threadId) {
        assert threadId != UNLOCKED;
        long twoThreadId0 = twoThreadId.getVolatileValue();
        int threadId0 = (int) twoThreadId0;
        if (threadId0 != UNLOCKED) {
            if (threadId0 == threadId)
                throw new IllegalStateException("trying to lock twice");
            return false;
        }
        return twoThreadId.compareAndSwapValue(twoThreadId0, (twoThreadId0 & TOP_BITS) | threadId);
    }

    public void lock(int threadId) throws InterruptedRuntimeException {
        assert threadId != UNLOCKED;
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

    private boolean busyLockSlower(int threadId) throws InterruptedRuntimeException {
        long endMS = System.currentTimeMillis() + timeoutMs;
        do {
            for (int i = 0; i < busyLockSlowerCount; i++) {
                if (tryLock(threadId))
                    return true;
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

    private void forceUnlockAndRetry(int threadId) {
        long twoThreadId0 = twoThreadId.getVolatileValue();
        int threadId0 = (int) twoThreadId0;

        // unlock the previous thread
        if (twoThreadId.compareAndSwapValue(twoThreadId0, ((long) threadId0 << 32) | UNLOCKED)) {
            String status = METRICS.supportsProc
                    ? isThreadRunning(threadId0) ? "running" : "dead"
                    : "unknown";
            Jvm.warn().on(getClass(), "Successfully forced an unlock for threadId: " + threadId + ", previous thread held by: " + threadId0 + ", status: " + status);
        } else {
            Jvm.warn().on(getClass(), "Failed to forced an unlock for threadId: " + threadId);
        }
        lock(threadId);
    }

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

    private void unlock2(int threadId, long twoThreadId0, int threadId0) {
        if (threadId0 == 0) {
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

        Metrics() {
            this.supportsProc = new File("/proc").isDirectory();
            if (OS.isLinux() && !supportsProc) Jvm.warn().on(getClass(), "/proc not found on Linux");
        }
    }
}
