/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.test;

public final class RecordingRunnable implements Runnable {
    private int runCount;

    @Override
    public void run() {
        runCount++;
    }

    public int runCount() {
        return runCount;
    }
}
