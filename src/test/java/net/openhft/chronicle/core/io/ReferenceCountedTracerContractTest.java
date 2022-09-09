package net.openhft.chronicle.core.io;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Any implementor of {@link ReferenceCountedTracer} should implement a test class
 * that extends this or one of its more specific children
 */
public abstract class ReferenceCountedTracerContractTest extends ReferenceCountedContractTest {

    @Override
    protected abstract ReferenceCountedTracer createReferenceCounted();

    @Test
    public void throwIfReleasedWillThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        assertThrows(ClosedIllegalStateException.class, referenceCounted::throwExceptionIfReleased);
    }

    @Test
    public void throwIfReleasedWillNotThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.throwExceptionIfReleased();
    }

    @Test
    public void throwIfNotReleasedWillThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        assertThrows(IllegalStateException.class, referenceCounted::throwExceptionIfNotReleased);
    }

    @Test
    public void throwIfNotReleasedWillNotThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        referenceCounted.throwExceptionIfNotReleased();
    }

    @Test
    public void listenersShouldBeNotifiedOnWarnAndReleaseIfNotReleased() {
        ReferenceCountedTracer rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");

        CounterReferenceChangeListener listener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(listener);
        rc.reserve(a);
        rc.reserve(b);

        expectException("Discarded without being released");
        rc.warnAndReleaseIfNotReleased();
        assertEquals(3, listener.referenceRemovedCount);
    }
}
