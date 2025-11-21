/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.UsedViaReflection;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class SimpleCleaner {
    private static final AtomicIntegerFieldUpdater<SimpleCleaner> CLEANED_FLAG =
            AtomicIntegerFieldUpdater.newUpdater(SimpleCleaner.class, "cleaned");

    private final Runnable thunk;
    // this must be volatile for the newUpdater about to work
    @UsedViaReflection
    @SuppressWarnings("unused")
    private volatile int cleaned = 0;

    public SimpleCleaner(Runnable thunk) {
        this.thunk = thunk;
    }

    public void clean() {
        if (CLEANED_FLAG.compareAndSet(this, 0, 1))
            thunk.run();
    }
}
