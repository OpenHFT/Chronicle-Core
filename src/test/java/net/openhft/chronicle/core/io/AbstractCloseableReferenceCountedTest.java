/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class AbstractCloseableReferenceCountedTest extends ReferenceCountedTracerContractTest  {

    @Test
    public void reserve() throws IllegalStateException, IllegalArgumentException {
        assumeTrue(Jvm.isResourceTracing());
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

    @Test(expected = IllegalStateException.class)
    public void testReserveAfterClose() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted();
        mcrc.close();
        mcrc.reserve(ReferenceOwner.temporary("a"));
    }

    @Test
    public void testTryReserveAfterClose() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted();
        mcrc.close();
        assertFalse(mcrc.tryReserve(ReferenceOwner.temporary("a")));
    }

    @Test
    public void testIsClosedAfterClose() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted();
        assertFalse(mcrc.isClosed());
        mcrc.close();
        assertTrue(mcrc.isClosed());
    }

    @Test
    public void testPerformReleaseClose1() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted() {
            @Override
            protected void performRelease() {
                super.performRelease();
                // isClosed() true as soon as fully released
                assertTrue(isClosed());
                assertTrue(isClosing());
            }
        };
        mcrc.close();
        assertTrue(mcrc.isClosing());
        assertTrue(mcrc.isClosed());
    }

    @Test
    public void testPerformReleaseClose2() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted() {
            @Override
            protected void performRelease() {
                super.performRelease();
                // still reserved so not yet closed
                assertFalse(isClosed());
                assertTrue(isClosing());
            }
        };
        // holds off closing
        mcrc.reserve(ReferenceOwner.TMP);
        mcrc.close();
        assertTrue(mcrc.isClosing());
        assertTrue(mcrc.isClosed());
    }

    @Test
    public void testReleaseLastCloses() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted();
        ReferenceOwner a = ReferenceOwner.temporary("a");
        mcrc.reserve(a);
        assertFalse(mcrc.isClosed());

        // initially its reserved by INIT
        mcrc.release(ReferenceOwner.INIT);
        assertFalse(mcrc.isClosed());

        mcrc.releaseLast(a);
        assertTrue(mcrc.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowExceptionIfClosedWhenClosed() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted();
        mcrc.close();
        mcrc.throwExceptionIfClosed();
    }

    @Test
    public void testThrowExceptionIfClosedWhenNotClosed() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted();
        mcrc.throwExceptionIfClosed();
        // Passes if no exception is thrown.
    }

    @Test
    public void testReserveTransfer() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted();
        ReferenceOwner from = ReferenceOwner.temporary("from");
        ReferenceOwner to = ReferenceOwner.temporary("to");

        mcrc.reserve(from);
        assertEquals(2, mcrc.refCount());
        mcrc.reserveTransfer(from, to);

        try {
            mcrc.release(from);
            fail("Should throw an exception because the reference was transferred");
        } catch (IllegalStateException ignored) {
        }
        mcrc.release(to);
        assertEquals(1, mcrc.refCount());
    }

    @Test
    public void testReserveAndReleaseRefCounts() {
        MyCloseableReferenceCounted mcrc = new MyCloseableReferenceCounted();
        ReferenceOwner owner = ReferenceOwner.temporary("owner");

        int initialRefCount = mcrc.refCount();

        mcrc.reserve(owner);
        assertEquals(initialRefCount + 1, mcrc.refCount());

        mcrc.release(owner);
        assertEquals(initialRefCount, mcrc.refCount());
    }

    @Override
    protected MyCloseableReferenceCounted createReferenceCounted() {
        return new MyCloseableReferenceCounted();
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