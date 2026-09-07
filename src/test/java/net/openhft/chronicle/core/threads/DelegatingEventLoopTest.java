/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.test.RecordingEventLoop;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class DelegatingEventLoopTest {

    private RecordingEventLoop innerEventLoop;
    private DelegatingEventLoop delegatingEventLoop;

    @BeforeEach
    void setUp() {
        innerEventLoop = new RecordingEventLoop();
        delegatingEventLoop = new DelegatingEventLoop(innerEventLoop);
    }

    @Test
    void constructorShouldAssignEventLoop() {
        assertEquals(innerEventLoop, delegatingEventLoop.inner);
    }

    @Test
    void nameShouldDelegateToInner() {
        delegatingEventLoop.name();
        assertEquals(1, innerEventLoop.nameCalls());
    }

    @Test
    void startShouldDelegateToInner() {
        delegatingEventLoop.start();
        assertEquals(1, innerEventLoop.startCalls());
    }

    @Test
    void unpauseShouldDelegateToInner() {
        delegatingEventLoop.unpause();
        assertEquals(1, innerEventLoop.unpauseCalls());
    }

    @Test
    void stopShouldDelegateToInner() {
        delegatingEventLoop.stop();
        assertEquals(1, innerEventLoop.stopCalls());
    }

    @Test
    void isClosedShouldDelegateToInner() {
        delegatingEventLoop.isClosed();
        assertEquals(1, innerEventLoop.isClosedCalls());
    }

    @Test
    void isStoppedShouldDelegateToInner() {
        delegatingEventLoop.isStopped();
        assertEquals(1, innerEventLoop.isStoppedCalls());
    }

    @Test
    void isClosingShouldDelegateToInner() {
        delegatingEventLoop.isClosing();
        assertEquals(1, innerEventLoop.isClosingCalls());
    }

    @Test
    void isAliveShouldDelegateToInner() {
        delegatingEventLoop.isAlive();
        assertEquals(1, innerEventLoop.isAliveCalls());
    }

    @Test
    void closeShouldDelegateToInner() {
        delegatingEventLoop.close();
        assertEquals(1, innerEventLoop.closeCalls());
    }

    @Test
    void addHandlerShouldDelegateToInner() {
        EventHandler handler = () -> false;
        delegatingEventLoop.addHandler(handler);
        assertSame(handler, innerEventLoop.lastHandler());
        assertEquals(1, innerEventLoop.addHandlerCalls());
    }

    @Test
    void runsInsideCoreLoopShouldDelegateToInner() {
        delegatingEventLoop.runsInsideCoreLoop();
        assertEquals(1, innerEventLoop.runsInsideCoreLoopCalls());
    }
}
