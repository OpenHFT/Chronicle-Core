/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.test;

import java.util.concurrent.Callable;

public final class RecordingCallable<V> implements Callable<V> {
    private final V value;
    private int callCount;

    public RecordingCallable() {
        this(null);
    }

    public RecordingCallable(V value) {
        this.value = value;
    }

    @Override
    public V call() {
        callCount++;
        return value;
    }

    public int callCount() {
        return callCount;
    }
}
