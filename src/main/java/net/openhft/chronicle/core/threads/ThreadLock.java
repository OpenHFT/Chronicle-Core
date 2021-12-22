package net.openhft.chronicle.core.threads;

public interface ThreadLock {

    boolean tryLock(int threadId);

    void lock(int threadId) throws InterruptedRuntimeException;

    void unlock(int threadId) throws IllegalStateException;
}
