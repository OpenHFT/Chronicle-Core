/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractCloseableReferenceCountedTest extends ReferenceCountedTracerContractTest {

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
    public void reserve() throws IllegalStateException, IllegalArgumentException {
        Jvm.setResourceTracing(true);

        MyCloseableReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount(), "reserve: L32");

        exerciseReserveLifecycle(rc, () -> rc.performRelease);
    }

    @Test
    public void reserveWhenClosed() throws IllegalStateException, IllegalArgumentException {
        MyCloseableReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount(), "reserveWhenClosed: L40");

        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount(), "reserveWhenClosed: L44");

        rc.close();
        assertEquals(1, rc.refCount(), "reserveWhenClosed: L47");

        ReferenceOwner b = ReferenceOwner.temporary("b");
        assertThrows(IllegalStateException.class, () -> rc.reserve(b));
        assertEquals(1, rc.refCount(), "reserveWhenClosed: L51");

        assertFalse(rc.tryReserve(b), "reserveWhenClosed: L53");
        assertEquals(1, rc.refCount(), "reserveWhenClosed: L54");

        rc.release(a);
        assertEquals(0, rc.refCount(), "reserveWhenClosed: L57");
        assertEquals(1, rc.performRelease, "reserveWhenClosed: L58");

        assertThrows(IllegalStateException.class, rc::throwExceptionIfReleased);
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

        MyCloseableReferenceCounted() {
        }

        @Override
        protected void performRelease() {
            performRelease++;
        }
    }
}
