package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractCloseableGptTest {
    private ConcreteCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = new ConcreteCloseable();
    }

    @Test
    public void testNotClosedInitially() {
        assertFalse(closeable.isClosed());
    }

    @Test
    public void testClose() {
        closeable.close();
        assertTrue(closeable.isClosed());
    }

    @Test
    public void testThrowExceptionIfClosedWhenNotClosed() {
        assertDoesNotThrow(closeable::throwExceptionIfClosed);
    }

    @Test
    public void testThrowExceptionIfClosedWhenClosed() {
        closeable.close();
        assertThrows(IllegalStateException.class, closeable::throwExceptionIfClosed);
    }

    @Test
    public void testWarnAndCloseIfNotClosedWhenClosed() {
        closeable.close();
        assertDoesNotThrow(closeable::warnAndCloseIfNotClosed);
    }

    @Test
    public void testWarnAndCloseIfNotClosedWhenNotClosed() {
        assertDoesNotThrow(closeable::warnAndCloseIfNotClosed);
        assertTrue(closeable.isClosed());
    }

    static class ConcreteCloseable extends AbstractCloseable {
        @Override
        protected void performClose() throws IllegalStateException {
            // Add your specific close logic here
        }
    }
}
