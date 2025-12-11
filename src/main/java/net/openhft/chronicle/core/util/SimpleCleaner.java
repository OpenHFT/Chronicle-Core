/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.UsedViaReflection;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Lightweight one shot cleaner.
 * <p>
 * Wraps a {@link Runnable} and guarantees it is only executed once even when {@link #clean()}
 * is called concurrently from multiple threads.
 */
public class SimpleCleaner {
    private static final AtomicIntegerFieldUpdater<SimpleCleaner> CLEANED_FLAG =
            AtomicIntegerFieldUpdater.newUpdater(SimpleCleaner.class, "cleaned");

    private final Runnable thunk;
    // this must be volatile for the newUpdater about to work
    @UsedViaReflection
    @SuppressWarnings("unused")
    private volatile int cleaned = 0;

    /**
     * Creates a cleaner that will run the provided action at most once.
     *
     * @param thunk action to run during cleaning
     */
    public SimpleCleaner(Runnable thunk) {
        this.thunk = thunk;
    }

    /**
     * Executes the action if it has not already run.
     */
    public void clean() {
        if (CLEANED_FLAG.compareAndSet(this, 0, 1))
            thunk.run();
    }
}
