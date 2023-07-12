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

package net.openhft.chronicle.core.threads;

import net.openhft.posix.PosixAPI;

@Deprecated(/* to be moved in x.26 */)
public interface ThreadLock {

    /**
     * Try to lock once or fail using the current OS thread id.
     *
     * @return true if the lock could be obtained, or false if not.
     * @throws IllegalStateException if the same threadId already holds the lock
     */
    default boolean tryLock() throws IllegalStateException {
        return tryLock(gettid());
    }

    /**
     * @return the thread id used for locking for the current thread, or -1 is unknown.
     */
    default int gettid() {
        try {
            return PosixAPI.posix().gettid();
        } catch (Error e) {
            return -1;
        }
    }

    /**
     * Try to lock once or fail
     *
     * @param osThreadId to attempt to lock for
     * @return true if the lock could be obtained, or false if not.
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
