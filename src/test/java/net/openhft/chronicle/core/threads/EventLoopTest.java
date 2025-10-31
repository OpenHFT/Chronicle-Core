/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EventLoopTest {

    private EventLoop eventLoop;
    private EventHandler handler;

    @BeforeEach
    public void setUp() {
        eventLoop = mock(EventLoop.class);
        handler = mock(EventHandler.class);
    }

    @Test
    public void testName() {
        when(eventLoop.name()).thenReturn("TestEventLoop");
        assertEquals("TestEventLoop", eventLoop.name());
    }

    @Test
    public void testAddHandler() {
        doNothing().when(eventLoop).addHandler(handler);
        eventLoop.addHandler(handler);
        verify(eventLoop).addHandler(handler);
    }

    @Test
    public void testStart() {
        doNothing().when(eventLoop).start();
        eventLoop.start();
        verify(eventLoop).start();
    }

    @Test
    public void testUnpause() {
        doNothing().when(eventLoop).unpause();
        eventLoop.unpause();
        verify(eventLoop).unpause();
    }

    @Test
    public void testStop() {
        doNothing().when(eventLoop).stop();
        eventLoop.stop();
        verify(eventLoop).stop();
    }

    @Test
    public void testIsAlive() {
        when(eventLoop.isAlive()).thenReturn(true);
        assertTrue(eventLoop.isAlive());
    }

    @Test
    public void testIsStopped() {
        when(eventLoop.isStopped()).thenReturn(true);
        assertTrue(eventLoop.isStopped());
    }

    @Test
    public void testClose() throws Exception {
        doNothing().when(eventLoop).close();
        eventLoop.close();
        verify(eventLoop).close();
    }

    @Test
    public void testRunsInsideCoreLoop() {
        when(eventLoop.runsInsideCoreLoop()).thenReturn(true);
        assertTrue(eventLoop.runsInsideCoreLoop());
    }
}
