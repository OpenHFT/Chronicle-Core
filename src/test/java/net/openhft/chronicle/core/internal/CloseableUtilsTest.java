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

public class CloseableUtilsTest {
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
    public void testAdd() {
        CloseableUtils.add(mockCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertTrue(closeablesRef.get().contains(mockCloseable), "added closeable should be present in tracked closeables set");
    }

    @Test
    public void testEnableCloseableTracing() {
        assertNotNull(getCloseablesRef().get(), "closeables set should be initialized when tracing is enabled");
    }

    @Test
    public void testDisableCloseableTracing() {
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
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testWaitForCloseablesToClose() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosing()).thenReturn(true);

        assertTrue(CloseableUtils.waitForCloseablesToClose(1000), "waitForCloseablesToClose should return true when all closeables are closing");
    }

    @Test
    public void testWaitForCloseablesToCloseWithException() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosing()).thenThrow(new IllegalStateException("testWaitForCloseablesToCloseWithException"));

        assertThrows(IllegalStateException.class,
                () -> CloseableUtils.waitForCloseablesToClose(1000),
                "testWaitForCloseablesToCloseWithException");
    }

    @Test
    public void testAssertCloseablesClosed() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosed()).thenReturn(true);

        assertDoesNotThrow(CloseableUtils::assertCloseablesClosed, "testAssertCloseablesClosed");
    }

    @Test
    public void testAssertCloseablesClosedWithOpenCloseables() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosed()).thenReturn(false);

        assertThrows(AssertionError.class,
                CloseableUtils::assertCloseablesClosed,
                "testAssertCloseablesClosedWithOpenCloseables");
    }

    @Test
    public void testUnmonitor() {
        CloseableUtils.add(mockCloseable);
        CloseableUtils.unmonitor(mockCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(mockCloseable), "unmonitored closeable should be removed from tracked closeables set");
    }

    @Test
    public void testIOToolsUnmonitor() {
        IOTools.unmonitor(null);
        IOTools.unmonitor("hello");
        CloseableUtils.add(anonCloseable);
        IOTools.unmonitor(anonCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(anonCloseable), "IOTools.unmonitor should remove closeable from tracked closeables set");
    }

    @Test
    public void testCloseQuietlyArray() {
        Object[] array = {mock(Closeable.class), mock(Closeable.class)};

        CloseableUtils.closeQuietly(array);

        for (Object o : array) {
            verify((Closeable) o, times(1)).close();
        }
    }

    @Test
    public void testCloseQuietlyAutoCloseable() throws Exception {
        CloseableUtils.closeQuietly(mockAutoCloseable);

        verify(mockAutoCloseable, times(1)).close();
    }

    @Test
    public void testCloseQuietlyHttpURLConnection() {
        CloseableUtils.closeQuietly(mockHttpURLConnection);

        verify(mockHttpURLConnection, times(1)).disconnect();
    }
}
