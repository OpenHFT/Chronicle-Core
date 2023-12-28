package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EventLoopTest {

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
