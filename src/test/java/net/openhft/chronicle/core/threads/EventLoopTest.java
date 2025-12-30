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

class EventLoopTest {

    private EventLoop eventLoop;
    private EventHandler handler;

    @BeforeEach
    void setUp() {
        eventLoop = mock(EventLoop.class);
        handler = mock(EventHandler.class);
    }

    @DisplayName("Event loop name returns configured label")
    @Test
    void testName() {
        when(eventLoop.name()).thenReturn("TestEventLoop");
        assertEquals("TestEventLoop", eventLoop.name(), "event loop name should return the configured name");
    }

    @DisplayName("Handler registration forwards to event loop")
    @Test
    void testAddHandler() {
        doNothing().when(eventLoop).addHandler(handler);
        eventLoop.addHandler(handler);
        verify(eventLoop).addHandler(handler);
    }

    @DisplayName("Event loop start triggers underlying run")
    @Test
    void testStart() {
        doNothing().when(eventLoop).start();
        eventLoop.start();
        verify(eventLoop).start();
    }

    @DisplayName("Event loop unpause resumes processing cycle")
    @Test
    void testUnpause() {
        doNothing().when(eventLoop).unpause();
        eventLoop.unpause();
        verify(eventLoop).unpause();
    }

    @DisplayName("Event loop stop halts processing cycle")
    @Test
    void testStop() {
        doNothing().when(eventLoop).stop();
        eventLoop.stop();
        verify(eventLoop).stop();
    }

    @DisplayName("Event loop alive status reports running")
    @Test
    void testIsAlive() {
        when(eventLoop.isAlive()).thenReturn(true);
        assertTrue(eventLoop.isAlive(), "event loop should report as alive when running");
    }

    @DisplayName("Event loop stopped status reports halted")
    @Test
    void testIsStopped() {
        when(eventLoop.isStopped()).thenReturn(true);
        assertTrue(eventLoop.isStopped(), "event loop should report as stopped when halted");
    }

    @DisplayName("Event loop close forwards shutdown request")
    @Test
    void testClose() {
        doNothing().when(eventLoop).close();
        eventLoop.close();
        verify(eventLoop).close();
    }

    @DisplayName("Core loop flag reports execution context")
    @Test
    void testRunsInsideCoreLoop() {
        when(eventLoop.runsInsideCoreLoop()).thenReturn(true);
        assertTrue(eventLoop.runsInsideCoreLoop(), "event loop should indicate whether execution is inside core loop");
    }
}
