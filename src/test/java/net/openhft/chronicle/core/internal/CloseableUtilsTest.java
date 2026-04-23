/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.*;
import net.openhft.chronicle.core.test.RecordingCloseable;
import net.openhft.chronicle.core.test.RecordingManagedCloseable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CloseableUtilsTest {
    private RecordingManagedCloseable closeable;
    private AbstractCloseable anonCloseable;
    private RecordingAutoCloseable autoCloseable;
    private RecordingHttpURLConnection httpURLConnection;

    @BeforeEach
    void beforeEachCloseableUtilsTest() {
        setUp();
    }

    void setUp() {
        anonCloseable = new AbstractCloseable() {
            @Override
            protected void performClose() {

            }
        };
        closeable = new RecordingManagedCloseable();
        CloseableUtils.enableCloseableTracing();
        autoCloseable = new RecordingAutoCloseable();
        httpURLConnection = new RecordingHttpURLConnection();
    }

    @AfterEach
    void tearDown() {
        CloseableUtils.disableCloseableTracing();
        Closeable.closeQuietly(anonCloseable);
    }

    @Test
    void testAdd() {
        CloseableUtils.add(closeable);
        AtomicReference<Set<ManagedCloseable>> closeablesRef = getCloseablesRef();
        assertTrue(closeablesRef.get().contains(closeable));
    }

    @Test
    void testEnableCloseableTracing() {
        assertNotNull(getCloseablesRef().get());
    }

    @Test
    void testDisableCloseableTracing() {
        CloseableUtils.disableCloseableTracing();
        assertNull(getCloseablesRef().get());
    }

    // Private helper to access the private CLOSEABLES field in CloseableUtils
    @SuppressWarnings("unchecked")
    private AtomicReference<Set<ManagedCloseable>> getCloseablesRef() {
        try {
            java.lang.reflect.Field field = CloseableUtils.class.getDeclaredField("CLOSEABLES");
            field.setAccessible(true);
            //noinspection unchecked
            return (AtomicReference<Set<ManagedCloseable>>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testWaitForCloseablesToClose() {
        closeable.closing(true);
        CloseableUtils.add(closeable);

        assertTrue(CloseableUtils.waitForCloseablesToClose(1000));
    }

    @Test
    void testWaitForCloseablesToCloseWithException() {
        closeable.throwFromIsClosing(new IllegalStateException("not closed"));
        CloseableUtils.add(closeable);

        assertThrows(IllegalStateException.class, () -> CloseableUtils.waitForCloseablesToClose(1000));
    }

    @Test
    void testAssertCloseablesClosed() {
        closeable.closed(true);
        CloseableUtils.add(closeable);

        CloseableUtils.assertCloseablesClosed();
    }

    @Test
    void testAssertCloseablesClosedWithOpenCloseables() {
        CloseableUtils.add(closeable);

        assertThrows(AssertionError.class, CloseableUtils::assertCloseablesClosed);
    }

    @Test
    void testUnmonitor() {
        CloseableUtils.add(closeable);
        CloseableUtils.unmonitor(closeable);
        AtomicReference<Set<ManagedCloseable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(closeable));
    }

    @Test
    void testIOToolsUnmonitor() {
        IOTools.unmonitor(null);
        IOTools.unmonitor("hello");
        CloseableUtils.add(anonCloseable);
        IOTools.unmonitor(anonCloseable);
        AtomicReference<Set<ManagedCloseable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(anonCloseable));
    }

    @Test
    void testCloseQuietlyArray() {
        RecordingCloseable first = new RecordingCloseable();
        RecordingCloseable second = new RecordingCloseable();
        Object[] array = {first, second};

        CloseableUtils.closeQuietly(array);

        assertEquals(1, first.closeCount());
        assertEquals(1, second.closeCount());
    }

    @Test
    void testCloseQuietlyAutoCloseable() {
        CloseableUtils.closeQuietly(autoCloseable);

        assertEquals(1, autoCloseable.closeCount);
    }

    @Test
    void testCloseQuietlyHttpURLConnection() {
        CloseableUtils.closeQuietly(httpURLConnection);

        assertEquals(1, httpURLConnection.disconnectCount);
    }

    private static final class RecordingAutoCloseable implements AutoCloseable {
        private int closeCount;

        @Override
        public void close() {
            closeCount++;
        }
    }

    private static final class RecordingHttpURLConnection extends HttpURLConnection {
        private int disconnectCount;

        private RecordingHttpURLConnection() {
            super(toUrl());
        }

        @Override
        public void disconnect() {
            disconnectCount++;
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() throws IOException {
        }

        private static URL toUrl() {
            try {
                return new URL("http://localhost/");
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }
}
