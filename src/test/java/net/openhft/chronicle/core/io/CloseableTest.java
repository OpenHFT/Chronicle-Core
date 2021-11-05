package net.openhft.chronicle.core.io;

import org.junit.Test;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CloseableTest {

    @Test
    public void closeQuietlyHandlesNull() {
        CloseableImpl closeable = new CloseableImpl();
        Closeable.closeQuietly(null, null, null, closeable);
        assertTrue(closeable.wasClosed);
    }

    @Test
    public void closeQuietlyCallsCloseOnAutoCloseable() {
        final AutoCloseableImpl autoCloseable = new AutoCloseableImpl();
        Closeable.closeQuietly(autoCloseable);
        assertTrue(autoCloseable.wasClosed);
    }

    @Test
    public void closeQuietlyCallsCloseOnCloseable() {
        final CloseableImpl closeable = new CloseableImpl();
        Closeable.closeQuietly(closeable);
        assertTrue(closeable.wasClosed);
    }

    @Test
    public void closeQuietlyClosesCollections() {
        final List<CloseableImpl> closeables = Arrays.asList(new CloseableImpl(), new CloseableImpl(), new CloseableImpl(), new CloseableImpl());
        Closeable.closeQuietly(closeables);
        for (CloseableImpl closeable : closeables) {
            assertTrue(closeable.wasClosed);
        }
    }

    @Test
    public void closeQuietlyClosesReferences() {
        final CloseableImpl closeable = new CloseableImpl();
        final SoftReference<CloseableImpl> closeableRef = new SoftReference<>(closeable);
        Closeable.closeQuietly(closeableRef);
        assertTrue(closeable.wasClosed);
    }

    @Test
    public void closeQuietlyClosesRecursively() {
        CloseableImpl closeable1 = new CloseableImpl();
        CloseableImpl closeable2 = new CloseableImpl();
        CloseableImpl closeable3 = new CloseableImpl();
        CloseableImpl closeable4 = new CloseableImpl();
        SoftReference<List<List<CloseableImpl>>> structure =
                new SoftReference<>(Arrays.asList(Arrays.asList(closeable1, closeable2), Arrays.asList(closeable3, closeable4)));
        Closeable.closeQuietly(structure);
        assertTrue(closeable1.wasClosed);
        assertTrue(closeable2.wasClosed);
        assertTrue(closeable3.wasClosed);
        assertTrue(closeable4.wasClosed);
    }

    static class CloseableImpl implements Closeable {
        public boolean wasClosed = false;

        @Override
        public void close() {
            wasClosed = true;
        }

        @Override
        public boolean isClosed() {
            return wasClosed;
        }
    }

    static class AutoCloseableImpl implements AutoCloseable {
        boolean wasClosed = false;

        @Override
        public void close() {
            wasClosed = true;
        }
    }
}