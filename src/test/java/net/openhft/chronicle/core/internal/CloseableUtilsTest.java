/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.*;
import net.openhft.chronicle.core.test.RecordingCloseable;
import net.openhft.chronicle.core.test.RecordingManagedCloseable;
import net.openhft.chronicle.core.threads.CleaningThreadLocal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CloseableUtilsTest {
    private RecordingManagedCloseable closeable;
    private AbstractCloseable anonCloseable;
    private RecordingAutoCloseable autoCloseable;
    private RecordingHttpURLConnection httpURLConnection;

    @BeforeEach
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
    void waitForCloseablesToCloseReturnsFalseForOpenResource() {
        CloseableUtils.add(closeable);

        assertFalse(CloseableUtils.waitForCloseablesToClose(50));
    }

    @Test
    void waitForCloseablesToCloseRepeatsOrphanCleanup() throws Exception {
        CountDownLatch firstQuery = new CountDownLatch(1);
        AtomicBoolean closing = new AtomicBoolean();
        AtomicInteger cleanups = new AtomicInteger();
        ManagedCloseable resource = new ManagedCloseable() {
            @Override
            public void close() {
                cleanups.incrementAndGet();
                closing.set(true);
            }

            @Override
            public boolean isClosing() {
                firstQuery.countDown();
                return closing.get();
            }

            @Override
            public boolean isClosed() {
                return closing.get();
            }
        };
        CleaningThreadLocal<ManagedCloseable> local =
                CleaningThreadLocal.withCleanup(() -> resource, ManagedCloseable::close);
        CountDownLatch populated = new CountDownLatch(1);
        CountDownLatch stopOwner = new CountDownLatch(1);
        Thread owner = new Thread(() -> {
            local.get();
            populated.countDown();
            try {
                stopOwner.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "closeable-orphan-owner");
        ExecutorService waiter = Executors.newSingleThreadExecutor();

        try {
            owner.start();
            assertTrue(populated.await(5, TimeUnit.SECONDS));
            CloseableUtils.add(resource);

            Future<Boolean> closed = waiter.submit(() -> CloseableUtils.waitForCloseablesToClose(2_000));
            assertTrue(firstQuery.await(5, TimeUnit.SECONDS));
            stopOwner.countDown();
            owner.join(5_000);
            assertFalse(owner.isAlive());

            assertTrue(closed.get(5, TimeUnit.SECONDS));
            assertEquals(1, cleanups.get());
            CleaningThreadLocal.cleanupNonCleaningThreads();
            assertEquals(1, cleanups.get(), "a later sweep must not repeat cleanup");
        } finally {
            stopOwner.countDown();
            owner.join(5_000);
            CleaningThreadLocal.cleanupNonCleaningThreads();
            waiter.shutdownNow();
        }
    }

    @Test
    void waitForCloseablesToClosePreservesInterruptWithoutSpinning() {
        AtomicInteger queries = new AtomicInteger();
        ManagedCloseable resource = new ManagedCloseable() {
            @Override
            public void close() {
            }

            @Override
            public boolean isClosing() {
                queries.incrementAndGet();
                return false;
            }

            @Override
            public boolean isClosed() {
                return false;
            }
        };
        CloseableUtils.add(resource);

        try {
            Thread.currentThread().interrupt();
            assertFalse(CloseableUtils.waitForCloseablesToClose(100));
            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(queries.get() < 100, "an interrupted wait must still pause between polls");
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void waitForCloseablesToCloseQueriesOutsideRegistryLock() {
        Set<ManagedCloseable> traceSet = getCloseablesRef().get();
        AtomicBoolean queried = new AtomicBoolean();
        ManagedCloseable resource = new ManagedCloseable() {
            @Override
            public void close() {
            }

            @Override
            public boolean isClosing() {
                queried.set(true);
                assertFalse(Thread.holdsLock(traceSet), "isClosing must not run under the registry lock");
                return true;
            }

            @Override
            public boolean isClosed() {
                return true;
            }
        };
        CloseableUtils.add(resource);

        assertTrue(CloseableUtils.waitForCloseablesToClose(1_000));
        assertTrue(queried.get());
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
        public void connect() {
            // nothing to be done for virtual connection
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
