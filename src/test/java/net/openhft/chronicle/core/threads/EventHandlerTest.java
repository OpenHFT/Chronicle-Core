package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Test;
import java.io.Closeable;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

class EventHandlerTest {

    @Test
    void eventLoopShouldBeCalledWithCorrectEventLoop() {
        EventLoop mockEventLoop = mock(EventLoop.class);
        EventHandler handler = mock(EventHandler.class, CALLS_REAL_METHODS);

        handler.eventLoop(mockEventLoop);

        verify(handler).eventLoop(mockEventLoop);
    }

    @Test
    void loopStartedShouldBeCalled() {
        EventHandler handler = mock(EventHandler.class, CALLS_REAL_METHODS);

        handler.loopStarted();

        verify(handler).loopStarted();
    }

    @Test
    void loopFinishedShouldBeCalled() {
        EventHandler handler = mock(EventHandler.class, CALLS_REAL_METHODS);

        handler.loopFinished();

        verify(handler).loopFinished();
    }

    @Test
    void priorityShouldReturnMediumByDefault() {
        EventHandler handler = mock(EventHandler.class, CALLS_REAL_METHODS);

        assertEquals(HandlerPriority.MEDIUM, handler.priority());
    }

    @Test
    void closeShouldBeCalledIfEventHandlerIsCloseable() throws IOException {
        EventHandler handler = mock(EventHandler.class, withSettings().extraInterfaces(Closeable.class));

        handler.loopFinished();
        ((Closeable) handler).close();

        verify((Closeable) handler).close();
    }
}
