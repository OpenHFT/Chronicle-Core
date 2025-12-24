/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

class CloseableUtilsTest {
    private ManagedCloseable mockCloseable;
    private AbstractCloseable anonCloseable;
    private AutoCloseable mockAutoCloseable;
    private HttpURLConnection mockHttpURLConnection;

    @BeforeEach
    public void setUp() {
        assumeTrue(Jvm.majorVersion() <= 17);
        anonCloseable = new AbstractCloseable() {
            @Override
            protected void performClose() {
                // No-op: placeholder method
            }
        };
        mockCloseable = mock(ManagedCloseable.class);
        CloseableUtils.enableCloseableTracing();
        mockAutoCloseable = mock(AutoCloseable.class);
        mockHttpURLConnection = mock(HttpURLConnection.class);
    }

    @AfterEach
    public void tearDown() {
        CloseableUtils.disableCloseableTracing();
        Closeable.closeQuietly(anonCloseable);
    }

    @Test
    void testAdd() {
        CloseableUtils.add(mockCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        Set<Closeable> closeables = closeablesRef.get();
        assertTrue(closeables.contains(mockCloseable), closeables + " should contain " + mockCloseable);
    }

    @Test
    void testEnableCloseableTracing() {
        assertNotNull(getCloseablesRef().get(), "closeables set should be initialized when tracing is enabled");
    }

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
            throw new RuntimeException("Failed to access CloseableUtils.CLOSEABLES", e);
        }
    }

    @Test
    void testWaitForCloseablesToClose() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosing()).thenReturn(true);

        assertTrue(CloseableUtils.waitForCloseablesToClose(1000), "closeables reach closing state");
    }

    @Test
    void testWaitForCloseablesToCloseWithException() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosing()).thenThrow(new IllegalStateException("testWaitForCloseablesToCloseWithException"));

        assertThrows(IllegalStateException.class,
                () -> CloseableUtils.waitForCloseablesToClose(1000),
                "closing check raises error");
    }

    @Test
    void testAssertCloseablesClosed() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosed()).thenReturn(true);

        assertDoesNotThrow(CloseableUtils::assertCloseablesClosed, "assertCloseablesClosed should pass when closeables are closed");
    }

    @Test
    void testAssertCloseablesClosedWithOpenCloseables() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosed()).thenReturn(false);

        assertThrows(AssertionError.class,
                CloseableUtils::assertCloseablesClosed,
                "open closeable triggers assert");
    }

    @Test
    void testUnmonitor() {
        CloseableUtils.add(mockCloseable);
        CloseableUtils.unmonitor(mockCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        Set<Closeable> closeables = closeablesRef.get();
        assertFalse(closeables.contains(mockCloseable), "after unmonitor, closeables should not contain " + mockCloseable + ": " + closeables);
    }

    @Test
    void testIOToolsUnmonitor() {
        IOTools.unmonitor(null);
        IOTools.unmonitor("hello");
        CloseableUtils.add(anonCloseable);
        IOTools.unmonitor(anonCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        Set<Closeable> closeables = closeablesRef.get();
        assertFalse(closeables.contains(anonCloseable), "after IOTools.unmonitor, closeables should not contain " + anonCloseable + ": " + closeables);
    }

    @Test
    void testCloseQuietlyArray() {
        Object[] array = {mock(Closeable.class), mock(Closeable.class)};

        CloseableUtils.closeQuietly(array);

        for (Object o : array) {
            verify((Closeable) o, times(1)).close();
        }
    }

    @Test
    void testCloseQuietlyAutoCloseable() throws Exception {
        CloseableUtils.closeQuietly(mockAutoCloseable);

        verify(mockAutoCloseable, times(1)).close();
    }

    @Test
    void testCloseQuietlyHttpURLConnection() {
        CloseableUtils.closeQuietly(mockHttpURLConnection);

        verify(mockHttpURLConnection, times(1)).disconnect();
    }
}
