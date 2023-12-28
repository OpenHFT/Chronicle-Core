package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.IgnoresEverything;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ChainedExceptionHandlerTest {

    private ExceptionHandler handler1;
    private ExceptionHandler handler2;
    private ChainedExceptionHandler chainedHandler;

    @BeforeEach
    void setUp() {
        handler1 = mock(ExceptionHandler.class);
        handler2 = mock(ExceptionHandler.class);
        chainedHandler = new ChainedExceptionHandler(handler1, handler2);
    }

    @Test
    void testChainExecution() {
        Throwable throwable = new RuntimeException("Test");
        chainedHandler.on(Exception.class, "Test message", throwable);

        verify(handler1).on(Exception.class, "Test message", throwable);
        verify(handler2).on(Exception.class, "Test message", throwable);
    }

    @Test
    void onWithClassShouldCallEachHandler() {
        ExceptionHandler firstHandler = mock(ExceptionHandler.class);
        ExceptionHandler secondHandler = mock(ExceptionHandler.class);
        ChainedExceptionHandler chained = new ChainedExceptionHandler(firstHandler, secondHandler);

        Class<?> clazz = String.class;
        String message = "Test message";
        Throwable thrown = new RuntimeException();

        chained.on(clazz, message, thrown);

        InOrder inOrder = inOrder(firstHandler, secondHandler);
        inOrder.verify(firstHandler).on(clazz, message, thrown);
        inOrder.verify(secondHandler).on(clazz, message, thrown);
    }

    @Test
    void onShouldCatchExceptionsFromHandlers() {
        ExceptionHandler faultyHandler = (clazz, msg, thr) -> { throw new RuntimeException("Handler error"); };
        ChainedExceptionHandler chained = new ChainedExceptionHandler(faultyHandler);

        // This call should not throw an exception
        chained.on(String.class, "message", new RuntimeException());
    }
}
