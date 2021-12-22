package net.openhft.chronicle.core.threads;

public interface ThreadLock {

    /**
     * Try to lock once or fail
     *
     * @param threadId to attempt to lock for
     * @return true if the lock could be obtains, or false if not.
     * @throws IllegalStateException if the same threadId already holds the lock
     */
    boolean tryLock(int threadId) throws IllegalStateException;

    /**
     * Lock the resource using a threadId
     *
     * @param threadId to lock
     * @throws InterruptedRuntimeException if an interrupt occurred before or during a busy loop retry. It won't throw this if the lock can be obtained immediately.
     * @throws IllegalStateException       if the same threadId already holds the lock
     */
    void lock(int threadId) throws InterruptedRuntimeException, IllegalStateException;

    /**
     * Unlock for a threadId
     *
     * @param threadId to unlock
     * @throws IllegalStateException if the thread previously held the lock but doesn't hold it now.
     */
    void unlock(int threadId) throws IllegalStateException;
}
