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

    @DisplayName("constructorShouldAssignEventLoop behaviour under expected input and output conditions")
    @Test
    void constructorShouldAssignEventLoop() {
        assertEquals(innerEventLoop, delegatingEventLoop.inner,
                "constructor should store the supplied inner event loop");
    }

    @DisplayName("nameShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void nameShouldDelegateToInner() {
        delegatingEventLoop.name();
        verify(innerEventLoop).name();
    }

    @DisplayName("startShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void startShouldDelegateToInner() {
        delegatingEventLoop.start();
        verify(innerEventLoop).start();
    }

    @DisplayName("unpauseShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void unpauseShouldDelegateToInner() {
        delegatingEventLoop.unpause();
        verify(innerEventLoop).unpause();
    }

    @DisplayName("stopShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void stopShouldDelegateToInner() {
        delegatingEventLoop.stop();
        verify(innerEventLoop).stop();
    }

    @DisplayName("isClosedShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void isClosedShouldDelegateToInner() {
        delegatingEventLoop.isClosed();
        verify(innerEventLoop).isClosed();
    }

    @DisplayName("isStoppedShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void isStoppedShouldDelegateToInner() {
        when(innerEventLoop.isStopped()).thenReturn(true);
        assertTrue(delegatingEventLoop.isStopped(), "delegating event loop should return stopped state from inner event loop");
        verify(innerEventLoop).isStopped();
    }

    @DisplayName("isClosingShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void isClosingShouldDelegateToInner() {
        delegatingEventLoop.isClosing();
        verify(innerEventLoop).isClosing();
    }

    @DisplayName("isAliveShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void isAliveShouldDelegateToInner() {
        delegatingEventLoop.isAlive();
        verify(innerEventLoop).isAlive();
    }

    @DisplayName("closeShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void closeShouldDelegateToInner() {
        delegatingEventLoop.close();
        verify(innerEventLoop).close();
    }

    @DisplayName("addHandlerShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void addHandlerShouldDelegateToInner() {
        EventHandler handler = mock(EventHandler.class);
        delegatingEventLoop.addHandler(handler);
        verify(innerEventLoop).addHandler(handler);
    }

    @DisplayName("runsInsideCoreLoopShouldDelegateToInner behaviour under expected input and output conditions")
    @Test
    void runsInsideCoreLoopShouldDelegateToInner() {
        delegatingEventLoop.runsInsideCoreLoop();
        verify(innerEventLoop).runsInsideCoreLoop();
    }
}
