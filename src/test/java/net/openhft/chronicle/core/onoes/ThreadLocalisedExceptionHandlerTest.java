package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ThreadLocalisedExceptionHandlerTest {

    private ExceptionHandler defaultHandler;
    private ThreadLocalisedExceptionHandler tlExceptionHandler;

    @BeforeEach
    public void setUp() {
        defaultHandler = mock(ExceptionHandler.class);
        tlExceptionHandler = new ThreadLocalisedExceptionHandler(defaultHandler);
    }

    @Test
    public void testUnwrapExceptionHandler() {
        assertSame(defaultHandler, ThreadLocalisedExceptionHandler.unwrap(tlExceptionHandler));
    }

    @Test
    public void testIsEnabled() {
        when(defaultHandler.isEnabled(Exception.class)).thenReturn(true);
        assertTrue(tlExceptionHandler.isEnabled(Exception.class));
    }

    // Add more tests as necessary for other methods and edge cases.
}
