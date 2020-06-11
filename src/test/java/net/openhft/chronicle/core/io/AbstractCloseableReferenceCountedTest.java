package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractCloseableReferenceCountedTest extends CoreTestCommon {

    @Test
    public void reserve() throws IllegalStateException {
        assertTrue(Jvm.isResourceTracing());
        MyCloseableReferenceCounted rc = new MyCloseableReferenceCounted();
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
    public void reserveWhenClosed() throws IllegalStateException {
        MyCloseableReferenceCounted rc = new MyCloseableReferenceCounted();
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