/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractCloseableReferenceCountedTest extends ReferenceCountedTracerContractTest {

    private MyCloseableReferenceCounted referenceCounted;

    @Before
    public void discardResources() {
        ignoreException("Failed to release LAST, closing anyway");
    }

    @Test
    public void reserve() throws IllegalStateException, IllegalArgumentException {
        Jvm.setResourceTracing(true);

        MyCloseableReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount());

        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals(3, rc.refCount());

        try {
            rc.reserve(a);
            fail();
        } catch (IllegalStateException ignored) {
        }
        assertEquals(3, rc.refCount());

        rc.release(b);
        assertEquals(2, rc.refCount());

        rc.release(a);
        assertEquals(1, rc.refCount());
        assertEquals(0, rc.performRelease);

        rc.releaseLast();
        assertEquals(0, rc.refCount());
        assertEquals(1, rc.performRelease);
    }

    @Test
    public void reserveWhenClosed() throws IllegalStateException, IllegalArgumentException {
        MyCloseableReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount());

        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount());

        rc.close();
        assertEquals(1, rc.refCount());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        try {
            rc.reserve(b);
            fail();
        } catch (IllegalStateException ignored) {
        }
        assertEquals(1, rc.refCount());

        assertFalse(rc.tryReserve(b));
        assertEquals(1, rc.refCount());

        rc.release(a);
        assertEquals(0, rc.refCount());
        assertEquals(1, rc.performRelease);

        try {
            rc.throwExceptionIfReleased();
            fail();
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    public void releaseLastWillReleaseThenFailWhenReferenceIsNotLast() {
        super.releaseLastWillReleaseThenFailWhenReferenceIsNotLast();
        referenceCounted = null;
        assertNull(referenceCounted);
    }

    @Test
    public void listenersShouldNotBeNotifiedOnWarnAndReleaseIfNotReleased() {
        super.listenersShouldNotBeNotifiedOnWarnAndReleaseIfNotReleased();
        referenceCounted = null;
        assertNull(referenceCounted);
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
        int performRelease;

        public MyCloseableReferenceCounted() {
        }

        @Override
        protected void performRelease() {
            performRelease++;
        }
    }
}
