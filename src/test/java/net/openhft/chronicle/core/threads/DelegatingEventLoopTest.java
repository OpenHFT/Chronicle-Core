/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DelegatingEventLoopTest {

    private EventLoop innerEventLoop;
    private DelegatingEventLoop delegatingEventLoop;

    @BeforeEach
    void setUp() {
        innerEventLoop = mock(EventLoop.class);
        delegatingEventLoop = new DelegatingEventLoop(innerEventLoop);
    }

    @Test
    @DisplayName("Constructor stores inner event loop reference")
    void constructorShouldAssignEventLoop() {
        assertEquals(innerEventLoop, delegatingEventLoop.inner,
                "constructor should store the supplied inner event loop");
    }

    @Test
    @DisplayName("Delegating loop forwards name query to inner")
    void nameShouldDelegateToInner() {
        delegatingEventLoop.name();
        verify(innerEventLoop).name();
    }

    @Test
    @DisplayName("Delegating loop forwards start signal to inner")
    void startShouldDelegateToInner() {
        delegatingEventLoop.start();
        verify(innerEventLoop).start();
    }

    @Test
    @DisplayName("Delegating loop forwards unpause signal to inner")
    void unpauseShouldDelegateToInner() {
        delegatingEventLoop.unpause();
        verify(innerEventLoop).unpause();
    }

    @Test
    @DisplayName("Delegating loop forwards stop signal to inner")
    void stopShouldDelegateToInner() {
        delegatingEventLoop.stop();
        verify(innerEventLoop).stop();
    }

    @Test
    @DisplayName("Delegating loop forwards closed status query")
    void isClosedShouldDelegateToInner() {
        delegatingEventLoop.isClosed();
        verify(innerEventLoop).isClosed();
    }

    @Test
    @DisplayName("Delegating loop forwards stopped status query")
    void isStoppedShouldDelegateToInner() {
        when(innerEventLoop.isStopped()).thenReturn(true);
        assertTrue(delegatingEventLoop.isStopped(), "delegating event loop should return stopped state from inner event loop");
        verify(innerEventLoop).isStopped();
    }

    @Test
    @DisplayName("Delegating loop forwards closing status query")
    void isClosingShouldDelegateToInner() {
        delegatingEventLoop.isClosing();
        verify(innerEventLoop).isClosing();
    }

    @Test
    @DisplayName("Delegating loop forwards alive status query")
    void isAliveShouldDelegateToInner() {
        delegatingEventLoop.isAlive();
        verify(innerEventLoop).isAlive();
    }

    @Test
    @DisplayName("Delegating loop forwards close signal to inner")
    void closeShouldDelegateToInner() {
        delegatingEventLoop.close();
        verify(innerEventLoop).close();
    }

    @Test
    @DisplayName("Delegating loop forwards handler registration to inner")
    void addHandlerShouldDelegateToInner() {
        EventHandler handler = mock(EventHandler.class);
        delegatingEventLoop.addHandler(handler);
        verify(innerEventLoop).addHandler(handler);
    }

    @Test
    @DisplayName("Delegating loop forwards core loop membership query")
    void runsInsideCoreLoopShouldDelegateToInner() {
        delegatingEventLoop.runsInsideCoreLoop();
        verify(innerEventLoop).runsInsideCoreLoop();
    }
}
