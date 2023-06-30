/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.threads;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DelegatingEventLoopGptTest {

    private EventLoop mockEventLoop;
    private DelegatingEventLoop delegatingEventLoop;

    @Before
    public void setUp() {
        mockEventLoop = mock(EventLoop.class);
        delegatingEventLoop = new DelegatingEventLoop(mockEventLoop);
    }

    @Test
    public void testStart() {
        delegatingEventLoop.start();
        verify(mockEventLoop, times(1)).start();
    }

    @Test
    public void testUnpause() {
        delegatingEventLoop.unpause();
        verify(mockEventLoop, times(1)).unpause();
    }

    @Test
    public void testStop() {
        delegatingEventLoop.stop();
        verify(mockEventLoop, times(1)).stop();
    }

    @Test
    public void testIsClosed() {
        when(mockEventLoop.isClosed()).thenReturn(true);
        assertTrue(delegatingEventLoop.isClosed());
        verify(mockEventLoop, times(1)).isClosed();
    }

    @Test
    public void testIsStopped() {
        when(mockEventLoop.isStopped()).thenReturn(true);
        assertTrue(delegatingEventLoop.isStopped());
        verify(mockEventLoop, times(1)).isStopped();
    }

    @Test
    public void testIsClosing() {
        when(mockEventLoop.isClosing()).thenReturn(true);
        assertTrue(delegatingEventLoop.isClosing());
        verify(mockEventLoop, times(1)).isClosing();
    }

    @Test
    public void testIsAlive() {
        when(mockEventLoop.isAlive()).thenReturn(true);
        assertTrue(delegatingEventLoop.isAlive());
        verify(mockEventLoop, times(1)).isAlive();
    }

    @Test
    public void testAwaitTermination() {
        delegatingEventLoop.awaitTermination();
        verify(mockEventLoop, times(1)).awaitTermination();
    }

    @Test
    public void testAddHandler() {
        EventHandler handler = mock(EventHandler.class);
        delegatingEventLoop.addHandler(handler);
        verify(mockEventLoop, times(1)).addHandler(handler);
    }

    @Test
    public void testRunsInsideCoreLoop() {
        when(mockEventLoop.runsInsideCoreLoop()).thenReturn(true);
        assertTrue(delegatingEventLoop.runsInsideCoreLoop());
        verify(mockEventLoop, times(1)).runsInsideCoreLoop();
    }

    @Test
    public void testName() {
        when(mockEventLoop.name()).thenReturn("testName");
        assertEquals("testName", delegatingEventLoop.name());
        verify(mockEventLoop, times(1)).name();
    }

    @Test
    public void testClose() {
        delegatingEventLoop.close();
        verify(mockEventLoop, times(1)).close();
    }
}
