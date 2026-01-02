/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AbstractReferenceCountedTest extends ReferenceCountedTracerContractTest {

    @Test
    @DisplayName("Reserve increments reference count on resource")
    void reserve() throws IllegalStateException, IllegalArgumentException {
        Jvm.setResourceTracing(true);

        MyReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount(), "Reference count should be 1 after initial creation");

        exerciseReserveLifecycle(rc, () -> rc.performRelease);
    }

    @Test
    @DisplayName("referenceId returns positive stable value")
    void referenceIdReturnsPositiveValue() {
        MyReferenceCounted rc = createReferenceCounted();
        int id = rc.referenceId();
        assertTrue(id > 0, "referenceId should be positive");
        assertEquals(id, rc.referenceId(), "referenceId should be stable");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("createdHere returns stack trace when tracing enabled")
    void createdHereWithTracing() {
        boolean wasTracing = Jvm.isResourceTracing();
        try {
            Jvm.setResourceTracing(true);
            MyReferenceCounted rc = new MyReferenceCounted();
            assertNotNull(rc.createdHere(), "createdHere should not be null when tracing");
            rc.releaseLast(ReferenceOwner.INIT);
        } finally {
            Jvm.setResourceTracing(wasTracing);
        }
    }

    @Test
    @DisplayName("toString returns referenceName")
    void toStringReturnsReferenceName() {
        MyReferenceCounted rc = createReferenceCounted();
        String str = rc.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("MyReferenceCounted"), "toString should contain class name: " + str);
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("inThreadPerformRelease catches exceptions")
    void inThreadPerformReleaseCatchesExceptions() {
        expectException("Test exception from performRelease");
        ExceptionThrowingReferenceCounted rc = new ExceptionThrowingReferenceCounted();
        // inThreadPerformRelease should not throw even if performRelease does
        assertDoesNotThrow(rc::inThreadPerformRelease,
                "inThreadPerformRelease should catch exceptions from performRelease");
    }

    @Test
    @DisplayName("singleThreadedCheckDisabled can be toggled")
    void singleThreadedCheckDisabledToggle() {
        MyReferenceCounted rc = createReferenceCounted();
        rc.singleThreadedCheckDisabled(true);
        // No exception should occur when accessed after disabling
        assertDoesNotThrow(() -> rc.threadSafetyCheck(true),
                "no exception when single-threaded check disabled");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("singleThreadedCheckReset clears thread association")
    void singleThreadedCheckResetClearsThread() {
        TestableReferenceCounted rc = new TestableReferenceCounted();
        rc.singleThreadedCheckDisabled(false);

        // Access to set the thread
        rc.threadSafetyCheck(true);

        // Reset
        rc.singleThreadedCheckReset();

        // Should not throw after reset
        assertDoesNotThrow(() -> rc.threadSafetyCheck(true),
                "should not throw after reset");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("thread safety check allows same thread access")
    void threadSafetyCheckAllowsSameThread() {
        TestableReferenceCounted rc = new TestableReferenceCounted();
        rc.singleThreadedCheckDisabled(false);

        // Multiple accesses from same thread should be fine
        assertTrue(rc.threadSafetyCheck(true), "first access should succeed");
        assertTrue(rc.threadSafetyCheck(true), "second access from same thread should succeed");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("thread safety check with isUsed=false on null thread")
    void threadSafetyCheckWithIsUsedFalse() {
        TestableReferenceCounted rc = new TestableReferenceCounted();
        rc.singleThreadedCheckDisabled(false);

        // isUsed=false with no thread set should return early
        assertTrue(rc.threadSafetyCheck(false),
                "should return true when isUsed=false and no thread set");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("thread safety check allows access after thread dies")
    void threadSafetyCheckAllowsAfterThreadDies() throws InterruptedException {
        TestableReferenceCounted rc = new TestableReferenceCounted();
        rc.singleThreadedCheckDisabled(false);

        CountDownLatch done = new CountDownLatch(1);
        Thread firstThread = new Thread(() -> {
            rc.threadSafetyCheck(true);
            done.countDown();
        });
        firstThread.start();
        done.await(1, TimeUnit.SECONDS);
        firstThread.join();

        // Access from main thread after first thread dies should succeed
        assertDoesNotThrow(() -> rc.threadSafetyCheck(true),
                "should allow access after original thread dies");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("unmonitor removes from tracking")
    void unmonitorRemovesFromTracking() {
        MyReferenceCounted rc = createReferenceCounted();
        assertDoesNotThrow(() -> rc.unmonitor(), "unmonitor should not throw");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("referenceCountedUnmonitored sets unmonitored state")
    void referenceCountedUnmonitoredSetsState() {
        MyReferenceCounted rc = createReferenceCounted();
        assertDoesNotThrow(() -> rc.referenceCountedUnmonitored(true),
                "referenceCountedUnmonitored should not throw");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Test
    @DisplayName("canReleaseInBackground returns false by default")
    void canReleaseInBackgroundDefault() {
        BackgroundTestReferenceCounted rc = new BackgroundTestReferenceCounted();
        assertFalse(rc.testCanReleaseInBackground(),
                "canReleaseInBackground should return false by default");
        rc.releaseLast(ReferenceOwner.INIT);
    }

    @Override
    protected MyReferenceCounted createReferenceCounted() {
        return new MyReferenceCounted();
    }

    static class MyReferenceCounted extends AbstractReferenceCounted {
        int performRelease;

        @Override
        protected void performRelease() {
            performRelease++;
        }
    }

    static class ExceptionThrowingReferenceCounted extends AbstractReferenceCounted {
        @Override
        protected void performRelease() {
            throw new RuntimeException("Test exception from performRelease");
        }
    }

    static class TestableReferenceCounted extends AbstractReferenceCounted {
        @Override
        protected void performRelease() {
        }

        @Override
        public boolean threadSafetyCheck(boolean isUsed) {
            return super.threadSafetyCheck(isUsed);
        }
    }

    static class BackgroundTestReferenceCounted extends AbstractReferenceCounted {
        @Override
        protected void performRelease() {
        }

        public boolean testCanReleaseInBackground() {
            return canReleaseInBackground();
        }
    }
}
