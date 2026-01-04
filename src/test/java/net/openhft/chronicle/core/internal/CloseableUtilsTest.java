/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.AbstractCloseable;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.io.ManagedCloseable;
import net.openhft.chronicle.core.io.QueryCloseable;
import net.openhft.chronicle.core.io.ReferenceChangeListener;
import net.openhft.chronicle.core.io.ReferenceCounted;
import net.openhft.chronicle.core.io.ReferenceOwner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @Test
    @DisplayName("add tracks closeables in tracing set")
    void testAdd() {
        CloseableUtils.add(managedCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertTrue(closeablesRef.get().contains(managedCloseable),
                "added closeable should be present in tracked closeables set: " + closeablesRef.get());
    }

    @Test
    @DisplayName("Enable closeable tracing initialises tracked set")
    void testEnableCloseableTracing() {
        assertNotNull(getCloseablesRef().get(), "closeables set should be initialized when tracing is enabled");
    }

    @Test
    @DisplayName("Disable closeable tracing clears tracked set")
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

    @Test
    @DisplayName("Wait for closeables to close returns true when closing")
    void testWaitForCloseablesToClose() {
        managedCloseable.setClosing(true);
        CloseableUtils.add(managedCloseable);

        assertTrue(CloseableUtils.waitForCloseablesToClose(1000), "waitForCloseablesToClose should return true when all closeables are closing");
    }

    @Test
    @DisplayName("Wait for closeables to close propagates closeable exceptions")
    void testWaitForCloseablesToCloseWithException() {
        managedCloseable.throwOnIsClosing(new IllegalStateException("testWaitForCloseablesToCloseWithException"));
        CloseableUtils.add(managedCloseable);

        assertThrows(IllegalStateException.class,
                () -> CloseableUtils.waitForCloseablesToClose(1000),
                "exception from closeable should propagate");
    }

    @Test
    @DisplayName("Assert closeables closed succeeds when all closed closeable")
    void testAssertCloseablesClosed() {
        managedCloseable.setClosed(true);
        CloseableUtils.add(managedCloseable);

        assertDoesNotThrow(CloseableUtils::assertCloseablesClosed, "assertCloseablesClosed should not throw when closed");
    }

    @Test
    @DisplayName("Assert closeables closed fails when closeables open")
    void testAssertCloseablesClosedWithOpenCloseables() {
        managedCloseable.setClosed(false);
        managedCloseable.setClosing(false);
        CloseableUtils.add(managedCloseable);

        assertThrows(AssertionError.class,
                CloseableUtils::assertCloseablesClosed,
                "assertCloseablesClosed should throw when open");
    }

    @Test
    @DisplayName("unmonitor removes closeable from tracking set")
    void testUnmonitor() {
        CloseableUtils.add(managedCloseable);
        CloseableUtils.unmonitor(managedCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(managedCloseable),
                "unmonitored closeable should be removed from tracked closeables set: " + closeablesRef.get());
    }

    @Test
    @DisplayName("IO tools unmonitor removes tracked closeables")
    void testIOToolsUnmonitor() {
        IOTools.unmonitor(null);
        IOTools.unmonitor("hello");
        CloseableUtils.add(anonCloseable);
        IOTools.unmonitor(anonCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(anonCloseable),
                "IOTools.unmonitor should remove closeable from tracked closeables set: " + closeablesRef.get());
    }

    @Test
    @DisplayName("closeQuietly closes each element in array")
    void testCloseQuietlyArray() {
        CloseableProbe first = new CloseableProbe();
        CloseableProbe second = new CloseableProbe();
        Object[] array = {first, second};

        CloseableUtils.closeQuietly(array);

        assertTrue(first.isClosed(), "first closeable should be closed");
        assertTrue(second.isClosed(), "second closeable should be closed");
    }

    @Test
    @DisplayName("Close quietly closes auto closeable once")
    void testCloseQuietlyAutoCloseable() throws Exception {
        CloseableUtils.closeQuietly(autoCloseable);

        assertTrue(autoCloseable.isClosed(), "AutoCloseable instance should be closed by closeQuietly");
    }

    @Test
    @DisplayName("Close quietly disconnects http URL connection instance")
    void testCloseQuietlyHttpURLConnection() {
        CloseableUtils.closeQuietly(httpURLConnection);

        assertTrue(httpURLConnection.isDisconnected(), "HttpURLConnection should be disconnected after closeQuietly");
    }

    @Test
    @DisplayName("closeQuietly ignores null input without throwing exceptions")
    void testCloseQuietlyNull() {
        assertDoesNotThrow(() -> CloseableUtils.closeQuietly((Object[]) null),
                "closeQuietly should ignore null Object array without throwing");
        assertDoesNotThrow(() -> CloseableUtils.closeQuietly((Object) null),
                "closeQuietly should ignore null Object without throwing");
    }

    @Test
    @DisplayName("closeQuietly closes all elements in a collection")
    void testCloseQuietlyCollection() {
        CloseableProbe first = new CloseableProbe();
        CloseableProbe second = new CloseableProbe();
        List<Closeable> collection = new ArrayList<>();
        collection.add(first);
        collection.add(second);

        CloseableUtils.closeQuietly(collection);

        assertTrue(first.isClosed(), "First Closeable in collection should be closed by closeQuietly");
        assertTrue(second.isClosed(), "Second Closeable in collection should be closed by closeQuietly");
    }

    @Test
    @DisplayName("closeQuietly ignores empty Closeable collection without throwing")
    void testCloseQuietlyEmptyCollection() {
        List<Closeable> emptyCollection = Collections.emptyList();

        assertDoesNotThrow(() -> CloseableUtils.closeQuietly(emptyCollection),
                "closeQuietly should handle empty Closeable collection without throwing");
    }

    @Test
    @DisplayName("closeQuietly closes ServerSocketChannel resources cleanly without error")
    void testCloseQuietlyServerSocketChannel() throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(0)); // bind to any available port

        CloseableUtils.closeQuietly(channel);

        assertFalse(channel.isOpen(), "ServerSocketChannel should be closed by closeQuietly");
    }

    @Test
    @DisplayName("closeQuietly handles ServerSocketChannel IOException without throwing")
    void testCloseQuietlyServerSocketChannelAlreadyClosed() throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.close(); // close it first

        assertDoesNotThrow(() -> CloseableUtils.closeQuietly(channel),
                "closeQuietly should ignore already closed ServerSocketChannel without throwing");
    }

    @Test
    @DisplayName("closeQuietly handles AutoCloseable that throws exception")
    void testCloseQuietlyAutoCloseableThrows() {
        AutoCloseable throwingCloseable = () -> {
            throw new IOException("test exception");
        };

        assertDoesNotThrow(() -> CloseableUtils.closeQuietly(throwingCloseable),
                "closeQuietly should suppress IOException thrown by AutoCloseable");
    }

    @Test
    @DisplayName("closeQuietly closes Closeable inside Reference wrapper")
    void testCloseQuietlyReference() {
        CloseableProbe closeable = new CloseableProbe();
        WeakReference<CloseableProbe> ref = new WeakReference<>(closeable);

        CloseableUtils.closeQuietly(ref);

        assertTrue(closeable.isClosed(), "Closeable held by Reference should be closed by closeQuietly");
    }

    @Test
    @DisplayName("closeQuietly ignores cleared Reference without throwing")
    void testCloseQuietlyClearedReference() {
        WeakReference<CloseableProbe> ref = new WeakReference<>(null);

        assertDoesNotThrow(() -> CloseableUtils.closeQuietly(ref),
                "closeQuietly should handle cleared Reference without throwing");
    }

    @Test
    @DisplayName("waitForCloseablesToClose returns true when closeable tracing is disabled")
    void testWaitForCloseablesToCloseTracingDisabled() {
        CloseableUtils.disableCloseableTracing();

        assertTrue(CloseableUtils.waitForCloseablesToClose(1000),
                "waitForCloseablesToClose should return true when tracing is disabled");
    }

    @Test
    @DisplayName("add does nothing when tracing is disabled")
    void testAddWhenTracingDisabled() {
        CloseableUtils.disableCloseableTracing();

        assertDoesNotThrow(() -> CloseableUtils.add(managedCloseable),
                "add should not throw when tracing is disabled");
    }

    @Test
    @DisplayName("unmonitor does nothing when tracing is disabled")
    void testUnmonitorWhenTracingDisabled() {
        CloseableUtils.disableCloseableTracing();

        assertDoesNotThrow(() -> CloseableUtils.unmonitor(managedCloseable),
                "unmonitor should not throw when tracing is disabled");
    }

    @Test
    @DisplayName("asString returns INIT for ReferenceOwner.INIT state")
    void testAsStringInit() {
        String result = CloseableUtils.asString(ReferenceOwner.INIT);

        assertTrue(result.contains("INIT"), "asString should return INIT for ReferenceOwner.INIT");
    }

    @Test
    @DisplayName("asString includes refCount for ReferenceCounted objects")
    void testAsStringReferenceCounted() {
        ReferenceCountedProbe probe = new ReferenceCountedProbe();
        probe.setRefCount(5);

        String result = CloseableUtils.asString(probe);

        assertTrue(result.contains("refCount=5"), "asString should include refCount for ReferenceCounted: " + result);
    }

    @Test
    @DisplayName("asString includes closed status for QueryCloseable objects")
    void testAsStringQueryCloseable() {
        QueryCloseableProbe probe = new QueryCloseableProbe();
        probe.setClosed(true);

        String result = CloseableUtils.asString(probe);

        assertTrue(result.contains("closed=true"), "asString should include closed status for QueryCloseable: " + result);
    }

    @Test
    @DisplayName("asString uses referenceName for ReferenceOwner in trace output")
    void testAsStringReferenceOwner() {
        ReferenceOwner owner = new ReferenceOwner() {
            @Override
            public String referenceName() {
                return "testOwnerName";
            }
        };

        String result = CloseableUtils.asString(owner);

        assertTrue(result.contains("testOwnerName"), "asString should use referenceName for ReferenceOwner: " + result);
    }

    @Test
    @DisplayName("asString handles object with class name and identity hash")
    void testAsStringRegularObject() {
        Object obj = new Object();

        String result = CloseableUtils.asString(obj);

        assertTrue(result.contains("Object@"), "asString should include class name and hash for regular objects: " + result);
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

    private static final class ReferenceCountedProbe implements ReferenceCounted {
        private int refCount;

        void setRefCount(int refCount) {
            this.refCount = refCount;
        }

        @Override
        public void reserve(ReferenceOwner id) throws IllegalStateException {
        }

        @Override
        public void release(ReferenceOwner id) throws IllegalStateException {
        }

        @Override
        public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        }

        @Override
        public int refCount() {
            return refCount;
        }

        @Override
        public boolean tryReserve(ReferenceOwner id) throws IllegalStateException {
            return false;
        }

        @Override
        public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
        }

        @Override
        public void addReferenceChangeListener(ReferenceChangeListener listener) {
        }

        @Override
        public void removeReferenceChangeListener(ReferenceChangeListener listener) {
        }
    }

    private static final class QueryCloseableProbe implements QueryCloseable {
        private boolean closed;

        void setClosed(boolean closed) {
            this.closed = closed;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }
}
