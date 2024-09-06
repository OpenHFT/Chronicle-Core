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

package net.openhft.chronicle.core.util;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * A simple cleaner class that executes a cleanup action (represented by a {@link Runnable})
 * exactly once. This is useful for cases where resources need to be released or cleaned up
 * in a thread-safe manner.
 */
public class SimpleCleaner {

    /**
     * An {@link AtomicIntegerFieldUpdater} for the {@code cleaned} field. This is used to ensure
     * that the cleanup action is only executed once.
     */
    private static final AtomicIntegerFieldUpdater<SimpleCleaner> CLEANED_FLAG =
            AtomicIntegerFieldUpdater.newUpdater(SimpleCleaner.class, "cleaned");

    /**
     * The cleanup action to be performed. This {@link Runnable} is executed exactly once when
     * {@link #clean()} is called for the first time.
     */
    private final Runnable thunk;

    /**
     * A volatile field that indicates whether the cleanup action has been executed.
     * This field is managed by CLEANED_FLAG to ensure atomic updates.
     * <p>
     * 0 indicates that the cleanup has not been performed yet.
     * 1 indicates that the cleanup has been performed.
     * </p>
     */
    @SuppressWarnings("unused")
    private volatile int cleaned = 0;

    /**
     * Constructs a new {@code SimpleCleaner} with the specified cleanup action.
     *
     * @param thunk The cleanup action to be performed when {@link #clean()} is called.
     */
    public SimpleCleaner(Runnable thunk) {
        this.thunk = thunk;
    }

    /**
     * Executes the cleanup action if it has not already been executed.
     * This method uses an atomic compare-and-set operation to ensure that the cleanup
     * action is performed only once, even if multiple threads call this method simultaneously.
     */
    public void clean() {
        // Atomically check if cleanup has already been performed and set cleaned to 1 if not
        if (CLEANED_FLAG.compareAndSet(this, 0, 1))
            // Execute the cleanup action
            thunk.run();
    }
}
