package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractReferenceCountedTest {

    @Test
    public void reserve() {
        assertTrue(Jvm.isResourceTracing());
        MyReferenceCounted rc = new MyReferenceCounted();
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
    public void reserveWhenClosed() {
        MyReferenceCounted rc = new MyReferenceCounted();
        assertEquals(1, rc.refCount());

        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount());

        assertFalse(rc.isClosed());

        rc.closeable.close();

        assertEquals(2, rc.refCount());
        assertTrue(rc.isClosed());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        try {
            rc.reserve(b);
            fail();
        } catch (IllegalStateException ignored) {
        }
        assertEquals(2, rc.refCount());

        assertFalse(rc.tryReserve(b));
        assertEquals(2, rc.refCount());

        rc.release(a);
        assertEquals(1, rc.refCount());
        assertEquals(0, rc.performRelease);

        rc.throwExceptionIfReleased();

        rc.releaseLast();
        assertEquals(0, rc.refCount());
        assertEquals(1, rc.performRelease);

        rc.throwExceptionBadResourceOwner();
        try {
            rc.throwExceptionIfClosed();
            fail();
        } catch (IllegalStateException ignored) {

        }
        try {
            rc.throwExceptionIfReleased();
            fail();
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    public void throwExceptionBadResourceOwner() {
        MyReferenceCounted rc = new MyReferenceCounted();
        MyReferenceCounted rc2 = new MyReferenceCounted();
        rc.reserve(rc2);
        rc.throwExceptionBadResourceOwner();

        rc2.closeable.close();
        try {
            rc.throwExceptionBadResourceOwner();
            fail();
        } catch (IllegalStateException ignored) {
        }
        rc.release(rc2);
        rc.releaseLast();
    }

    @Test
    public void isClosed() {
    }

    @Test
    public void throwExceptionIfClosed() {
    }

    static class MyReferenceCounted extends AbstractReferenceCounted {
        final AbstractCloseable closeable;
        int performRelease;

        public MyReferenceCounted() {
            this(new AbstractCloseableTest.MyCloseable());
        }

        public MyReferenceCounted(AbstractCloseable abstractCloseable) {
            super(abstractCloseable);
            closeable = abstractCloseable;
        }

        @Override
        protected void performRelease() {
            performRelease++;
        }
    }
}