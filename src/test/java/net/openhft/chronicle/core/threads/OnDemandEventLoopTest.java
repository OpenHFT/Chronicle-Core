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
                throw new UnsupportedOperationException();
            }

            @Override
            public void start() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void unpause() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void stop() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isClosed() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isAlive() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isStopped() {
                return false;
            }

            @Override
            public void close() {
            }
        });
        assertFalse(el.hasEventLoop());
        assertEquals("dummy", el.name());
        assertTrue(el.hasEventLoop());
        el.close();
    }
}
