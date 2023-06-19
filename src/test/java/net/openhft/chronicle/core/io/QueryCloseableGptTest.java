package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QueryCloseableGptTest {

    private static class TestCloseable implements QueryCloseable {
        private boolean closing;
        private boolean closed;

        public void startClosing() {
            this.closing = true;
        }

        public void finishClosing() {
            this.closing = false;
            this.closed = true;
        }

        @Override
        public boolean isClosing() {
            return closing;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }

    @Test
    public void testIsClosing() {
        TestCloseable closeable = new TestCloseable();

        assertFalse(closeable.isClosing());

        closeable.startClosing();

        assertTrue(closeable.isClosing());
    }

    @Test
    public void testIsClosed() {
        TestCloseable closeable = new TestCloseable();

        assertFalse(closeable.isClosed());

        closeable.startClosing();

        assertFalse(closeable.isClosed());

        closeable.finishClosing();

        assertTrue(closeable.isClosed());
    }

    @Test
    public void testIsClosingReturnsTrueIfIsClosedReturnsTrue() {
        TestCloseable closeable = new TestCloseable();

        closeable.startClosing();
        closeable.finishClosing();

        assertTrue(closeable.isClosing());
        assertTrue(closeable.isClosed());
    }
}
