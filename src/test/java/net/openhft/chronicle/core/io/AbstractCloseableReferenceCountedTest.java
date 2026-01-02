/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractCloseableReferenceCountedTest extends ReferenceCountedTracerContractTest {

    private MyCloseableReferenceCounted referenceCounted;

    @BeforeEach
    public void discardResources() {
        ignoreException("Failed to release LAST, closing anyway");
    }

    @AfterEach
    public void checkResources() {
        referenceCounted = null;
    }

    @Test
    @DisplayName("Reserve increments reference count on closeable")
    void reserve() throws IllegalStateException, IllegalArgumentException {
        Jvm.setResourceTracing(true);

        MyCloseableReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount(), "reserve: initial refCount should be 1");

        exerciseReserveLifecycle(rc, () -> rc.performRelease);
    }

    @Test
    @DisplayName("Reserve when closed abstract closeable reference")
    void reserveWhenClosed() throws IllegalStateException, IllegalArgumentException {
        MyCloseableReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount(), "reserveWhenClosed: initial refCount should be 1");

        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount(), "Reference count should be 2 after reserving with owner 'a'");

        rc.close();
        assertEquals(1, rc.refCount(), "Reference count should be 1 after close() (owner 'a' still holds reference)");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        assertThrows(IllegalStateException.class, () -> rc.reserve(b),
                "reserve should throw when resource is closed");
        assertEquals(1, rc.refCount(), "Reference count should remain 1 after failed reserve() on closed resource");

        assertFalse(rc.tryReserve(b), "tryReserve() should return false when resource is closed");
        assertEquals(1, rc.refCount(), "Reference count should remain 1 after failed tryReserve() on closed resource");

        rc.release(a);
        assertEquals(0, rc.refCount(), "Reference count should be 0 after releasing final owner 'a'");
        assertEquals(1, rc.performRelease, "performRelease() should be invoked exactly once after final release");

        assertThrows(IllegalStateException.class, rc::throwExceptionIfReleased,
                "throwExceptionIfReleased should throw after final release");
    }

    @Override
    protected MyCloseableReferenceCounted createReferenceCounted() {
        referenceCounted = new MyCloseableReferenceCounted();
        return referenceCounted;
    }

    @Override
    public void afterChecks() {
        Closeable.closeQuietly(referenceCounted);
        super.afterChecks();
    }

    static class MyCloseableReferenceCounted extends AbstractCloseableReferenceCounted {
        int performRelease = 0;

        @Override
        protected void performRelease() {
            performRelease++;
        }
    }
}
