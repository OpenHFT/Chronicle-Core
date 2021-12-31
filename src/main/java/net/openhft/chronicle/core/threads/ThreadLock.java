package net.openhft.chronicle.core.threads;

import net.openhft.posix.PosixAPI;

public interface ThreadLock {

    /**
     * Try to lock once or fail using the current OS thread id.
     *
     * @return true if the lock could be obtains, or false if not.
     * @throws IllegalStateException if the same threadId already holds the lock
     */
    default boolean tryLock() throws IllegalStateException {
        return tryLock(gettid());
    }

    /**
     * @return the thread id used for locking for the current thread
     */
    default int gettid() {
        return PosixAPI.posix().gettid();
    }

    /**
     * Try to lock once or fail
     *
     * @param osThreadId to attempt to lock for
     * @return true if the lock could be obtains, or false if not.
     * @throws IllegalStateException if the same threadId already holds the lock
     */
    boolean tryLock(int osThreadId) throws IllegalStateException;

    /**
     * Lock the resource using the current OS thread id.
     *
     * @throws InterruptedRuntimeException if an interrupt occurred before or during a busy loop retry. It won't throw this if the lock can be obtained immediately.
     * @throws IllegalStateException       if the same threadId already holds the lock
     */
    default void lock() throws InterruptedRuntimeException, IllegalStateException {
        lock(gettid());
    }

    /**
     * Lock the resource using a threadId
     *
     * @param osThreadId to lock
     * @throws InterruptedRuntimeException if an interrupt occurred before or during a busy loop retry. It won't throw this if the lock can be obtained immediately.
     * @throws IllegalStateException       if the same threadId already holds the lock
     */
    void lock(int osThreadId) throws InterruptedRuntimeException, IllegalStateException;

    /**
     * Unlock for a threadId
     *
     * @throws IllegalStateException if the thread previously held the lock but doesn't hold it now.
     */
    default void unlock() throws IllegalStateException {
        unlock(gettid());
    }

    /**
     * Unlock for a threadId
     *
     * @param osThreadId to unlock
     * @throws IllegalStateException if the thread previously held the lock but doesn't hold it now.
     */
    void unlock(int osThreadId) throws IllegalStateException;
}
