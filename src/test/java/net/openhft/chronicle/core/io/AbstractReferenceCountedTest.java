package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class AbstractReferenceCountedTest extends ReferenceCountedTracerContractTest {

    @Test
    public void reserve() throws IllegalStateException, IllegalArgumentException {
        assumeTrue(Jvm.isResourceTracing());

        MyReferenceCounted rc = createReferenceCounted();
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

    @Override
    protected MyReferenceCounted createReferenceCounted() {
        return new MyReferenceCounted();
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