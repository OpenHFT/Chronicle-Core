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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assumptions.*;

class CloseableUtilsTest {
    private ManagedCloseable mockCloseable;
    private AbstractCloseable anonCloseable;
    private AutoCloseable mockAutoCloseable;
    private HttpURLConnection mockHttpURLConnection;

    @BeforeEach
    void beforeEachCloseableUtilsTest() {
        mockitoNotSupportedOnJava21();
        setUp();
    }

    public void mockitoNotSupportedOnJava21() {
        assumeTrue(Jvm.majorVersion() <= 17);
    }

    public void setUp() {
        anonCloseable = new AbstractCloseable() {
            @Override
            protected void performClose() {

            }
        };
        mockitoNotSupportedOnJava21();
        mockCloseable = mock(ManagedCloseable.class);
        CloseableUtils.enableCloseableTracing();
        mockAutoCloseable = mock(AutoCloseable.class);
        mockHttpURLConnection = mock(HttpURLConnection.class);
    }

    @AfterEach
    void tearDown() {
        CloseableUtils.disableCloseableTracing();
        Closeable.closeQuietly(anonCloseable);
    }

    @Test
    void testAdd() {
        CloseableUtils.add(mockCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertTrue(closeablesRef.get().contains(mockCloseable));
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
    void testWaitForCloseablesToClose() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosing()).thenReturn(true);

        assertTrue(CloseableUtils.waitForCloseablesToClose(1000));
    }

    @Test
    void testWaitForCloseablesToCloseWithException() {
        assertThrows(IllegalStateException.class, () -> {
            CloseableUtils.add(mockCloseable);
            when(mockCloseable.isClosing()).thenReturn(false);
            doThrow(IllegalStateException.class).when(mockCloseable).isClosing();

            CloseableUtils.waitForCloseablesToClose(1000);
        });
    }

    @Test
    void testAssertCloseablesClosed() {
        CloseableUtils.add(mockCloseable);
        when(mockCloseable.isClosed()).thenReturn(true);

        CloseableUtils.assertCloseablesClosed();
    }

    @Test
    void testAssertCloseablesClosedWithOpenCloseables() {
        assertThrows(AssertionError.class, () -> {
            CloseableUtils.add(mockCloseable);
            when(mockCloseable.isClosed()).thenReturn(false);

            CloseableUtils.assertCloseablesClosed();
        });
    }

    @Test
    void testUnmonitor() {
        CloseableUtils.add(mockCloseable);
        CloseableUtils.unmonitor(mockCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(mockCloseable));
    }

    @Test
    void testIOToolsUnmonitor() {
        IOTools.unmonitor(null);
        IOTools.unmonitor("hello");
        CloseableUtils.add(anonCloseable);
        IOTools.unmonitor(anonCloseable);
        AtomicReference<Set<Closeable>> closeablesRef = getCloseablesRef();
        assertFalse(closeablesRef.get().contains(mockCloseable));
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
