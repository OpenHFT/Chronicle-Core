package net.openhft.chronicle.core.onoes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.slf4j.Logger;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ThreadLocalisedExceptionHandlerGptTest {

    @Mock
    private ExceptionHandler defaultHandler;

    @Mock
    private ExceptionHandler threadLocalHandler;

    @Mock
    private Logger logger;

    private ThreadLocalisedExceptionHandler underTest;

    @Before
    public void setUp() {
        underTest = new ThreadLocalisedExceptionHandler(defaultHandler);
    }

    @Test
    public void testOn_withException() {
        Throwable exception = new RuntimeException();
        underTest.threadLocalHandler(threadLocalHandler);
        underTest.on(logger, "Error occurred", exception);

        Mockito.verify(threadLocalHandler).on(logger, "Error occurred", exception);
    }

    @Test
    public void testOn_noThreadLocalHandler_withException() {
        Throwable exception = new RuntimeException();
        underTest.on(logger, "Error occurred", exception);

        Mockito.verify(defaultHandler).on(logger, "Error occurred", exception);
    }

    @Test
    public void testOn_noException() {
        underTest.threadLocalHandler(threadLocalHandler);
        underTest.on(logger, "Error occurred", null);

        Mockito.verify(threadLocalHandler).on(logger, "Error occurred", null);
    }

    @Test
    public void testOn_noThreadLocalHandler_noException() {
        underTest.on(logger, "Error occurred", null);

        Mockito.verify(defaultHandler).on(logger, "Error occurred", null);
    }
    
    @Test
    public void testIsEnabled() {
        Mockito.when(threadLocalHandler.isEnabled(ThreadLocalisedExceptionHandlerGptTest.class)).thenReturn(true);
        underTest.threadLocalHandler(threadLocalHandler);

        boolean isEnabled = underTest.isEnabled(ThreadLocalisedExceptionHandlerGptTest.class);

        assertTrue(isEnabled);
        Mockito.verify(threadLocalHandler).isEnabled(ThreadLocalisedExceptionHandlerGptTest.class);
    }
    
    @Test
    public void testResetThreadLocalHandler() {
        underTest.threadLocalHandler(threadLocalHandler);
        underTest.resetThreadLocalHandler();

        Throwable exception = new RuntimeException();
        underTest.on(logger, "Error occurred", exception);

        Mockito.verify(defaultHandler).on(logger, "Error occurred", exception);
        Mockito.verify(threadLocalHandler, Mockito.never()).on(logger, "Error occurred", exception);
    }
}
