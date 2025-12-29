/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.AbstractCloseable;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.io.ManagedCloseable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CloseableUtilsTest {
    private ManagedCloseableProbe managedCloseable;
    private AbstractCloseable anonCloseable;
    private AutoCloseableProbe autoCloseable;
    private HttpURLConnectionStub httpURLConnection;

    @BeforeEach
    public void setUp() {
        anonCloseable = new AbstractCloseable() {
            @Override
            protected void performClose() {

            }
        };
        managedCloseable = new ManagedCloseableProbe();
        CloseableUtils.enableCloseableTracing();
        autoCloseable = new AutoCloseableProbe();
        httpURLConnection = new HttpURLConnectionStub();
    }

    @AfterEach
    public void tearDown() {
        CloseableUtils.disableCloseableTracing();
        Closeable.closeQuietly(anonCloseable);
    }

    @DisplayName("add tracks closeables in tracing set")
    @Test
    void testAdd() {
        CloseableUtils.add(managedCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertTrue(closeablesRef.get().contains(managedCloseable),
                "added closeable should be present in tracked closeables set: " + closeablesRef.get());
    }

    @DisplayName("enableCloseableTracing initialises tracked set behaviour under expected input and output conditions")
    @Test
    void testEnableCloseableTracing() {
        assertNotNull(getCloseablesRef().get(), "closeables set should be initialized when tracing is enabled");
    }

    @DisplayName("disableCloseableTracing clears tracked set behaviour under expected input and output conditions")
    @Test
    void testDisableCloseableTracing() {
        CloseableUtils.disableCloseableTracing();
        assertNull(getCloseablesRef().get(), "closeables set should be null when tracing is disabled");
    }

    // Private helper to access the private CLOSEABLES field in CloseableUtils
    @SuppressWarnings("unchecked")
    private AtomicReference<Set<Closeable>> getCloseablesRef() {
        try {
            java.lang.reflect.Field field = CloseableUtils.class.getDeclaredField("CLOSEABLES");
            field.setAccessible(true);
            //noinspection unchecked
            return (AtomicReference<Set<Closeable>>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access CloseableUtils.CLOSEABLES via reflection", e);
        }
    }

    @DisplayName("waitForCloseablesToClose returns true when closing behaviour under expected input and output conditions")
    @Test
    void testWaitForCloseablesToClose() {
        managedCloseable.setClosing(true);
        CloseableUtils.add(managedCloseable);

        assertTrue(CloseableUtils.waitForCloseablesToClose(1000), "waitForCloseablesToClose should return true when all closeables are closing");
    }

    @DisplayName("waitForCloseablesToClose propagates closeable exceptions behaviour under expected input and output conditions")
    @Test
    void testWaitForCloseablesToCloseWithException() {
        managedCloseable.throwOnIsClosing(new IllegalStateException("testWaitForCloseablesToCloseWithException"));
        CloseableUtils.add(managedCloseable);

        assertThrows(IllegalStateException.class,
                () -> CloseableUtils.waitForCloseablesToClose(1000),
                "exception from closeable should propagate");
    }

    @DisplayName("assertCloseablesClosed succeeds when all closed behaviour under expected input and output conditions")
    @Test
    void testAssertCloseablesClosed() {
        managedCloseable.setClosed(true);
        CloseableUtils.add(managedCloseable);

        assertDoesNotThrow(CloseableUtils::assertCloseablesClosed, "assertCloseablesClosed should not throw when closed");
    }

    @DisplayName("assertCloseablesClosed fails when closeables open behaviour under expected input and output conditions")
    @Test
    void testAssertCloseablesClosedWithOpenCloseables() {
        managedCloseable.setClosed(false);
        managedCloseable.setClosing(false);
        CloseableUtils.add(managedCloseable);

        assertThrows(AssertionError.class,
                CloseableUtils::assertCloseablesClosed,
                "assertCloseablesClosed should throw when open");
    }

    @DisplayName("unmonitor removes closeable from tracking set")
    @Test
    void testUnmonitor() {
        CloseableUtils.add(managedCloseable);
        CloseableUtils.unmonitor(managedCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(managedCloseable),
                "unmonitored closeable should be removed from tracked closeables set: " + closeablesRef.get());
    }

    @DisplayName("IOTools.unmonitor removes tracked closeables behaviour under expected input and output conditions")
    @Test
    void testIOToolsUnmonitor() {
        IOTools.unmonitor(null);
        IOTools.unmonitor("hello");
        CloseableUtils.add(anonCloseable);
        IOTools.unmonitor(anonCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(anonCloseable),
                "IOTools.unmonitor should remove closeable from tracked closeables set: " + closeablesRef.get());
    }

    @DisplayName("closeQuietly closes each element in array")
    @Test
    void testCloseQuietlyArray() {
        CloseableProbe first = new CloseableProbe();
        CloseableProbe second = new CloseableProbe();
        Object[] array = {first, second};

        CloseableUtils.closeQuietly(array);

        assertTrue(first.isClosed(), "first closeable should be closed");
        assertTrue(second.isClosed(), "second closeable should be closed");
    }

    @DisplayName("closeQuietly closes AutoCloseable once behaviour under expected input and output conditions")
    @Test
    void testCloseQuietlyAutoCloseable() throws Exception {
        CloseableUtils.closeQuietly(autoCloseable);

        assertTrue(autoCloseable.isClosed(), "auto-closeable should be closed");
    }

    @DisplayName("closeQuietly disconnects HttpURLConnection instance behaviour under expected input and output conditions")
    @Test
    void testCloseQuietlyHttpURLConnection() {
        CloseableUtils.closeQuietly(httpURLConnection);

        assertTrue(httpURLConnection.isDisconnected(), "http connection should be disconnected");
    }

    private static final class ManagedCloseableProbe implements ManagedCloseable {
        private boolean closing;
        private boolean closed;
        private RuntimeException isClosingException;

        @Override
        public void close() {
            closed = true;
            closing = true;
        }

        @Override
        public boolean isClosing() {
            if (isClosingException != null) {
                throw isClosingException;
            }
            return closing;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        void setClosing(boolean closing) {
            this.closing = closing;
        }

        void setClosed(boolean closed) {
            this.closed = closed;
            if (closed) {
                this.closing = true;
            }
        }

        void throwOnIsClosing(RuntimeException exception) {
            this.isClosingException = exception;
        }

    }

    private static final class AutoCloseableProbe implements AutoCloseable {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        boolean isClosed() {
            return closed;
        }
    }

    private static final class HttpURLConnectionStub extends HttpURLConnection {
        private boolean disconnected;

        HttpURLConnectionStub() {
            super(localUrl());
        }

        @Override
        public void disconnect() {
            disconnected = true;
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() {
        }

        boolean isDisconnected() {
            return disconnected;
        }
    }

    private static final class CloseableProbe implements Closeable {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

    }

    private static URL localUrl() {
        try {
            return URI.create("http://localhost").toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError("Failed to create test URL", e);
        }
    }
}
