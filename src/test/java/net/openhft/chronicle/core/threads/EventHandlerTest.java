package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.Closeable;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

class EventHandlerTest {

    @BeforeEach
    public void mockitoNotSupportedOnJava21() {
        Assumptions.assumeTrue(Jvm.majorVersion() <= 17);
    }
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
