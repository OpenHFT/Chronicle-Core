package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ChainedExceptionHandlerGptTest {

    @Mock
    private ExceptionHandler handler1;

    @Mock
    private ExceptionHandler handler2;

    private ChainedExceptionHandler chainedExceptionHandler;

    @BeforeEach
    public void setup() {
        chainedExceptionHandler = new ChainedExceptionHandler(handler1, handler2);
    }

    @Test
    public void testOnClassMessageThrowable() {
        Class<?> clazz = ChainedExceptionHandlerGptTest.class;
        String message = "test message";
        Throwable thrown = new RuntimeException("test exception");

        chainedExceptionHandler.on(clazz, message, thrown);

        // Verify that each handler in the chain was called
        verify(handler1, times(1)).on(clazz, message, thrown);
        verify(handler2, times(1)).on(clazz, message, thrown);
    }

    @Test
    public void testOnLoggerMessageThrowable() {
        Logger logger = LoggerFactory.getLogger(ChainedExceptionHandlerGptTest.class);
        String message = "test message";
        Throwable thrown = new RuntimeException("test exception");

        chainedExceptionHandler.on(logger, message, thrown);

        // Verify that each handler in the chain was called
        verify(handler1, times(1)).on(logger, message, thrown);
        verify(handler2, times(1)).on(logger, message, thrown);
    }
}
