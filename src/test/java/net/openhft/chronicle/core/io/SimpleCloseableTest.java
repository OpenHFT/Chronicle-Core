package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.io.SimpleCloseable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SimpleCloseableTest {

    public static class TestableSimpleCloseable extends SimpleCloseable {
        private boolean performCloseCalled = false;

        @Override
        protected void performClose() {
            if (!performCloseCalled) {
                super.performClose();
                performCloseCalled = true;
            }
        }

        boolean isPerformCloseCalled() {
            return performCloseCalled;
        }
    }

    @Test
    public void testClose() {
        TestableSimpleCloseable closeable = new TestableSimpleCloseable();

        assertFalse(closeable.isClosed());
        closeable.close();
        assertTrue(closeable.isClosed());
        assertTrue(closeable.isPerformCloseCalled());

        closeable.close();
        assertTrue(closeable.isClosed());
    }

    @Test
    public void testIsClosed() {
        TestableSimpleCloseable closeable = new TestableSimpleCloseable();

        assertFalse(closeable.isClosed());
        closeable.close();
        assertTrue(closeable.isClosed());
    }
}
