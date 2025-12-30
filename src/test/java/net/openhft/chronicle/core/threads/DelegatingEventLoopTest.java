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

    @DisplayName("Constructor stores inner event loop reference")
    @Test
    void constructorShouldAssignEventLoop() {
        assertEquals(innerEventLoop, delegatingEventLoop.inner,
                "constructor should store the supplied inner event loop");
    }

    @DisplayName("Delegating loop forwards name query to inner")
    @Test
    void nameShouldDelegateToInner() {
        delegatingEventLoop.name();
        verify(innerEventLoop).name();
    }

    @DisplayName("Delegating loop forwards start signal to inner")
    @Test
    void startShouldDelegateToInner() {
        delegatingEventLoop.start();
        verify(innerEventLoop).start();
    }

    @DisplayName("Delegating loop forwards unpause signal to inner")
    @Test
    void unpauseShouldDelegateToInner() {
        delegatingEventLoop.unpause();
        verify(innerEventLoop).unpause();
    }

    @DisplayName("Delegating loop forwards stop signal to inner")
    @Test
    void stopShouldDelegateToInner() {
        delegatingEventLoop.stop();
        verify(innerEventLoop).stop();
    }

    @DisplayName("Delegating loop forwards closed status query")
    @Test
    void isClosedShouldDelegateToInner() {
        delegatingEventLoop.isClosed();
        verify(innerEventLoop).isClosed();
    }

    @DisplayName("Delegating loop forwards stopped status query")
    @Test
    void isStoppedShouldDelegateToInner() {
        when(innerEventLoop.isStopped()).thenReturn(true);
        assertTrue(delegatingEventLoop.isStopped(), "delegating event loop should return stopped state from inner event loop");
        verify(innerEventLoop).isStopped();
    }

    @DisplayName("Delegating loop forwards closing status query")
    @Test
    void isClosingShouldDelegateToInner() {
        delegatingEventLoop.isClosing();
        verify(innerEventLoop).isClosing();
    }

    @DisplayName("Delegating loop forwards alive status query")
    @Test
    void isAliveShouldDelegateToInner() {
        delegatingEventLoop.isAlive();
        verify(innerEventLoop).isAlive();
    }

    @DisplayName("Delegating loop forwards close signal to inner")
    @Test
    void closeShouldDelegateToInner() {
        delegatingEventLoop.close();
        verify(innerEventLoop).close();
    }

    @DisplayName("Delegating loop forwards handler registration to inner")
    @Test
    void addHandlerShouldDelegateToInner() {
        EventHandler handler = mock(EventHandler.class);
        delegatingEventLoop.addHandler(handler);
        verify(innerEventLoop).addHandler(handler);
    }

    @DisplayName("Delegating loop forwards core loop membership query")
    @Test
    void runsInsideCoreLoopShouldDelegateToInner() {
        delegatingEventLoop.runsInsideCoreLoop();
        verify(innerEventLoop).runsInsideCoreLoop();
    }
}
