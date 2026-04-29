/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.test;

import net.openhft.chronicle.core.io.ManagedCloseable;

public final class RecordingManagedCloseable extends RecordingCloseable implements ManagedCloseable {
    private IllegalStateException isClosingFailure;

    @Override
    public boolean isClosing() {
        if (isClosingFailure != null)
            throw isClosingFailure;
        return super.isClosing();
    }

    public void throwFromIsClosing(IllegalStateException failure) {
        isClosingFailure = failure;
    }
}
