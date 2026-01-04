/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AbstractCloseableTest extends CoreTestCommon {

    @Test
    @DisplayName("Close triggers performClose once and marks closed")
    void close() throws IllegalStateException {
        MyCloseable mc = new MyCloseable();
        assertFalse(mc.isClosed(), "newly created closeable should not be closed");
        assertEquals(0, mc.performClose, "performClose should not have been called before first close");

        mc.throwExceptionIfClosed();

        mc.close();
        assertTrue(mc.isClosed(), "resource status indicator should be true after first close call");
        assertEquals(1, mc.performClose, "performClose should have been called exactly once after first close");

        mc.close();
        assertTrue(mc.isClosed(), "resource status indicator should remain true after second close call");
        assertEquals(1, mc.performClose, "performClose should not be called again on second close");
    }

    @Test
    @DisplayName("Throw exception if closed abstract closeable")
    void throwExceptionIfClosed() {
        MyCloseable mc = new MyCloseable();
        mc.close();
        assertThrows(IllegalStateException.class, mc::throwExceptionIfClosed, "throwExceptionIfClosed should throw IllegalStateException when resource is closed");

    }

    @Test
    @DisplayName("Warn and close if not closed abstract")
    void warnAndCloseIfNotClosed() {
        Jvm.setResourceTracing(true);

        final Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        MyCloseable mc = new MyCloseable();

        // not recorded for now.
        System.err.println("!!! The following warning is expected !!!");
        mc.warnAndCloseIfNotClosed();

        assertTrue(mc.isClosed(), "resource should be closed after warnAndCloseIfNotClosed");
        Jvm.resetExceptionHandlers();
        if (!AbstractCloseable.DISABLE_DISCARD_WARNING)
            assertEquals("Discarded without closing\n" +
                            "java.lang.IllegalStateException: net.openhft.chronicle.core.StackTrace: net.openhft.chronicle.core.io.AbstractCloseableTest$MyCloseable created here on main",
                    map.keySet().stream()
                            .map(e -> e.message() + "\n" + e.throwable())
                            .collect(Collectors.joining(", "))
                            .split(" at ")[0],
                    "warning message should indicate resource was discarded without closing");
    }

    @Test
    @DisplayName("assertCloseable validation guards performClose on first failure")
    void assertCloseable() {

        final MyCloseable myCloseable = new MyCloseable() {
            int cnt = 0;

            @Override
            protected void assertCloseable() {
                if (cnt++ == 0)
                    throw new IllegalStateException("First close will always fail!");
            }
        };

        assertThrows(IllegalStateException.class, myCloseable::close,
                "first close should throw when assertCloseable fails");
        assertEquals(0, myCloseable.performClose, "performClose should not be called when assertCloseable fails");

        myCloseable.close();
        assertEquals(1, myCloseable.performClose, "performClose should be called once after assertCloseable passes");
    }

    @Test
    @DisplayName("isClosing returns true during close process")
    void isClosingDuringClose() {
        AtomicBoolean wasClosing = new AtomicBoolean(false);
        AbstractCloseable mc = new AbstractCloseable() {
            @Override
            protected void performClose() {
                wasClosing.set(isClosing());
            }
        };

        assertFalse(mc.isClosing(), "isClosing should be false before close");
        mc.close();
        assertTrue(wasClosing.get(), "isClosing should be true during performClose");
        assertTrue(mc.isClosing(), "isClosing should be true after close");
    }

    @Test
    @DisplayName("throwExceptionIfClosedInSetter throws ClosedIllegalStateException when resource is closed")
    void throwExceptionIfClosedInSetterWhenClosed() {
        MyCloseable mc = new MyCloseable();
        mc.close();

        assertThrows(ClosedIllegalStateException.class, mc::throwExceptionIfClosedInSetter,
                "throwExceptionIfClosedInSetter should throw when closed");
    }

    @Test
    @DisplayName("throwExceptionIfClosedInSetter succeeds when resource remains open")
    void throwExceptionIfClosedInSetterWhenOpen() {
        MyCloseable mc = new MyCloseable();

        assertDoesNotThrow(mc::throwExceptionIfClosedInSetter,
                "throwExceptionIfClosedInSetter should not throw when open");
        mc.close();
    }

    @Test
    @DisplayName("referenceId returns stable unique identifier value for instance")
    void referenceIdStable() {
        MyCloseable mc = new MyCloseable();

        int id1 = mc.referenceId();
        int id2 = mc.referenceId();

        assertTrue(id1 > 0, "referenceId should be positive, id1=" + id1);
        assertEquals(id1, id2, "referenceId should return same value on subsequent calls");
        mc.close();
    }

    @Test
    @DisplayName("createdHere returns null when closeable tracing is disabled")
    void createdHereWithoutTracing() {
        boolean wasTracing = Jvm.isResourceTracing();
        try {
            Jvm.setResourceTracing(false);
            MyCloseable mc = new MyCloseable();

            assertNull(mc.createdHere(), "createdHere should be null when tracing disabled");
            mc.close();
        } finally {
            Jvm.setResourceTracing(wasTracing);
        }
    }

    @Test
    @DisplayName("singleThreadedCheckDisabled toggles single-threaded checks on demand")
    void singleThreadedCheckDisabledToggle() {
        MyCloseable mc = new MyCloseable();

        assertFalse(mc.singleThreadedCheckDisabled(), "single-threaded check should be enabled by default");

        mc.singleThreadedCheckDisabled(true);
        assertTrue(mc.singleThreadedCheckDisabled(), "single-threaded check should be disabled after setting");

        mc.singleThreadedCheckDisabled(false);
        assertFalse(mc.singleThreadedCheckDisabled(), "single-threaded check should be enabled after resetting");

        mc.close();
    }

    @Test
    @DisplayName("singleThreadedCheckReset clears thread association after reset")
    void singleThreadedCheckResetClearsThread() {
        MyCloseable mc = new MyCloseable();

        // Access resource to set the usedByThread
        mc.throwExceptionIfClosed();

        // Reset should clear the association
        mc.singleThreadedCheckReset();

        // Should not throw when accessed from same thread after reset
        assertDoesNotThrow(mc::throwExceptionIfClosed,
                "throwExceptionIfClosed should not throw after reset");
        mc.close();
    }

    @Test
    @DisplayName("toString returns referenceName including class identifier")
    void toStringReturnsReferenceName() {
        MyCloseable mc = new MyCloseable();

        String str = mc.toString();

        assertNotNull(str, "toString should return non-null referenceName string");
        assertTrue(str.contains("MyCloseable"), "toString should contain class name: " + str);
        mc.close();
    }

    @Test
    @DisplayName("isInUserThread returns true for user thread names")
    void isInUserThreadForUserThread() {
        TestableCloseable mc = new TestableCloseable();

        // Main thread is a user thread (no ~ in name)
        assertTrue(mc.testIsInUserThread(),
                "isInUserThread should treat main thread as user thread");
        mc.close();
    }

    @Test
    @DisplayName("isInUserThread returns false for system thread names")
    void isInUserThreadForSystemThread() throws InterruptedException {
        AtomicBoolean result = new AtomicBoolean(true);
        TestableCloseable mc = new TestableCloseable();

        Thread systemThread = new Thread(() -> {
            result.set(mc.testIsInUserThread());
        }, "test~system");
        systemThread.start();
        systemThread.join();

        assertFalse(result.get(), "isInUserThread should ignore thread names containing '~'");
        mc.close();
    }

    @Test
    @DisplayName("unmonitor removes instance from closeable tracking list")
    void unmonitorRemovesFromTracking() {
        ignoreException("Closeable tracing is disabled");
        AbstractCloseable.enableCloseableTracing();
        try {
            MyCloseable mc = new MyCloseable();

            // Unmonitor should not throw
            assertDoesNotThrow(mc::unmonitor, "unmonitor should not throw while tracing is enabled");

            mc.close();
        } finally {
            AbstractCloseable.disableCloseableTracing();
        }
    }

    @Test
    @DisplayName("performClose exception is logged but does not propagate")
    void performCloseExceptionLogged() {
        expectException("Error occurred while performing resource close operation");
        AbstractCloseable mc = new AbstractCloseable() {
            @Override
            protected void performClose() {
                throw new RuntimeException("Test exception from performClose");
            }
        };

        // close() should not throw even if performClose() throws
        assertDoesNotThrow(mc::close, "close should not propagate exception from performClose");
        assertTrue(mc.isClosed(), "resource should be marked closed even after exception in performClose");
    }

    @Test
    @DisplayName("shouldPerformCloseInBackground returns false for default closeable setting")
    void shouldPerformCloseInBackgroundDefault() {
        BackgroundTestCloseable mc = new BackgroundTestCloseable();

        assertFalse(mc.testShouldPerformCloseInBackground(),
                "shouldPerformCloseInBackground should return false by default");
        mc.close();
    }

    @Test
    @DisplayName("shouldWaitForClosed returns false for default closeable setting")
    void shouldWaitForClosedDefault() {
        WaitTestCloseable mc = new WaitTestCloseable();

        assertFalse(mc.testShouldWaitForClosed(),
                "shouldWaitForClosed should return false by default");
        mc.close();
    }

    @Test
    @DisplayName("double close from different threads handled correctly")
    void doubleCloseFromDifferentThreads() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        MyCloseable mc = new MyCloseable();

        Thread t = new Thread(() -> {
            try {
                latch.await(1, TimeUnit.SECONDS);
                mc.close();
            } catch (Throwable e) {
                error.set(e);
            }
        });
        t.start();

        mc.close();
        latch.countDown();
        t.join();

        assertNull(error.get(), "no exception should occur on double close from different threads");
        assertTrue(mc.isClosed(), "resource should be closed");
        assertEquals(1, mc.performClose, "performClose should only be called once");
    }

    @Test
    @DisplayName("enableCloseableTracing and disableCloseableTracing are static methods")
    void closeableTracingStaticMethods() {
        ignoreException("Closeable tracing is disabled");
        // Just verify these static methods work without errors
        assertDoesNotThrow(AbstractCloseable::enableCloseableTracing,
                "enableCloseableTracing should not throw");
        assertDoesNotThrow(AbstractCloseable::disableCloseableTracing,
                "disableCloseableTracing should not throw");
    }

    @Test
    @DisplayName("waitForCloseablesToClose returns true when closeable tracing is disabled")
    void waitForCloseablesToCloseTracingDisabled() {
        AbstractCloseable.disableCloseableTracing();
        try {
            assertTrue(AbstractCloseable.waitForCloseablesToClose(100),
                    "waitForCloseablesToClose should return true when tracing disabled");
        } finally {
            AbstractCloseable.enableCloseableTracing();
        }
    }

    @Test
    @DisplayName("thread safety check detects cross-thread access")
    void threadSafetyCheckDetectsCrossThreadAccess() throws InterruptedException {
        // Create a closeable with single-threaded check enabled
        MyCloseable mc = new MyCloseable();
        mc.singleThreadedCheckDisabled(false);

        // First access from main thread
        mc.throwExceptionIfClosed();

        // Try to access from another thread - should throw ThreadingIllegalStateException
        AtomicReference<Throwable> error = new AtomicReference<>();
        Thread otherThread = new Thread(() -> {
            try {
                mc.throwExceptionIfClosed();
            } catch (Throwable t) {
                error.set(t);
            }
        });
        otherThread.start();
        otherThread.join();

        assertNotNull(error.get(), "cross-thread access should throw exception");
        assertTrue(error.get() instanceof ThreadingIllegalStateException,
                "exception should be ThreadingIllegalStateException: " + error.get().getClass());
        mc.close();
    }

    @Test
    @DisplayName("thread safety check allows access after thread dies")
    void threadSafetyCheckAllowsAccessAfterThreadDies() throws InterruptedException {
        MyCloseable mc = new MyCloseable();
        mc.singleThreadedCheckDisabled(false);

        // First access from a thread that will die
        CountDownLatch done = new CountDownLatch(1);
        Thread firstThread = new Thread(() -> {
            mc.throwExceptionIfClosed();
            done.countDown();
        });
        firstThread.start();
        done.await(1, TimeUnit.SECONDS);
        firstThread.join(); // Wait for thread to die

        // Access from main thread should succeed after first thread dies
        assertDoesNotThrow(mc::throwExceptionIfClosed,
                "access should be allowed after original thread dies");
        mc.close();
    }

    @Test
    @DisplayName("warnAndCloseIfNotClosed does nothing when already closing")
    void warnAndCloseIfNotClosedWhenAlreadyClosing() {
        MyCloseable mc = new MyCloseable();
        mc.close();

        // Already closed, so this should do nothing
        assertDoesNotThrow(mc::warnAndCloseIfNotClosed,
                "warnAndCloseIfNotClosed should not throw when already closed");
        assertEquals(1, mc.performClose, "performClose should still only be called once");
    }

    @Test
    @DisplayName("createdHere returns stack trace when tracing enabled")
    void createdHereWithTracing() {
        boolean wasTracing = Jvm.isResourceTracing();
        try {
            Jvm.setResourceTracing(true);
            MyCloseable mc = new MyCloseable();

            assertNotNull(mc.createdHere(), "createdHere should not be null when tracing enabled");
            mc.close();
        } finally {
            Jvm.setResourceTracing(wasTracing);
        }
    }

    @Test
    @DisplayName("close sets closedHere when tracing enabled")
    void closeSetsClosedHereWhenTracing() {
        boolean wasTracing = Jvm.isResourceTracing();
        try {
            Jvm.setResourceTracing(true);
            TrackedCloseable mc = new TrackedCloseable();

            mc.close();

            assertNotNull(mc.getClosedHere(), "closedHere should be set when tracing enabled");
        } finally {
            Jvm.setResourceTracing(wasTracing);
        }
    }

    static class MyCloseable extends AbstractCloseable {
        int performClose;

        @Override
        protected void performClose() {
            assertTrue(isClosing(), "isClosing should return true during performClose execution");
            assertFalse(isClosed(), "isClosed should return false until performClose completes");
            performClose++;
        }
    }

    static class TestableCloseable extends AbstractCloseable {
        @Override
        protected void performClose() {
        }

        public boolean testIsInUserThread() {
            return isInUserThread();
        }
    }

    static class BackgroundTestCloseable extends AbstractCloseable {
        @Override
        protected void performClose() {
        }

        public boolean testShouldPerformCloseInBackground() {
            return shouldPerformCloseInBackground();
        }
    }

    static class WaitTestCloseable extends AbstractCloseable {
        @Override
        protected void performClose() {
        }

        public boolean testShouldWaitForClosed() {
            return shouldWaitForClosed();
        }
    }

    static class TrackedCloseable extends AbstractCloseable {
        @Override
        protected void performClose() {
        }

        public Object getClosedHere() {
            return closedHere;
        }
    }
}
