/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.test;

import net.openhft.chronicle.core.io.InvalidMarshallableException;
import net.openhft.chronicle.core.threads.InvalidEventHandlerException;
import net.openhft.chronicle.core.threads.VanillaEventHandler;

public final class RecordingVanillaEventHandler implements VanillaEventHandler {
    private int actionCount;
    private boolean result;

    @Override
    public boolean action() throws InvalidEventHandlerException, InvalidMarshallableException {
        actionCount++;
        return result;
    }

    public int actionCount() {
        return actionCount;
    }

    public void reset() {
        actionCount = 0;
    }

    public void result(boolean result) {
        this.result = result;
    }
}
