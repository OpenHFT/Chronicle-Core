package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractReferenceCountedTest extends CoreTestCommon {

    @Test
    public void reserve() throws IllegalStateException {
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

    static class MyReferenceCounted extends AbstractReferenceCounted {
        int performRelease;

        public MyReferenceCounted() {
        }


        @Override
        protected void performRelease() {
            performRelease++;
        }
    }
}