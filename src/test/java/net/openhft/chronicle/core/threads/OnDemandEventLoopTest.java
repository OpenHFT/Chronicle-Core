/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OnDemandEventLoopTest extends CoreTestCommon {
    @Test
    void onDemand() {
        OnDemandEventLoop el = new OnDemandEventLoop(() -> new EventLoop() {
            @Override
            public String name() {
                return "dummy";
            }

            @Override
            public void addHandler(EventHandler handler) {
                throw new UnsupportedOperationException("addHandler not supported in test");
            }

            @Override
            public void start() {
                throw new UnsupportedOperationException("start not supported in test");
            }

            @Override
            public void unpause() {
                throw new UnsupportedOperationException("unpause not supported in test");
            }

            @Override
            public void stop() {
                throw new UnsupportedOperationException("stop not supported in test");
            }

            @Override
            public boolean isClosed() {
                throw new UnsupportedOperationException("isClosed not supported in test");
            }

            @Override
            public boolean isAlive() {
                throw new UnsupportedOperationException("isAlive not supported in test");
            }

            @Override
            public boolean isStopped() {
                return false;
            }

            @Override
            public void close() {
                // No-op: placeholder method
            }
        });
        assertFalse(el.hasEventLoop(), "event loop should not exist before first access");
        assertEquals("dummy", el.name(), "accessing name should trigger event loop creation and return delegate name");
        assertTrue(el.hasEventLoop(), "event loop should exist after being accessed");
        el.close();
    }
}
