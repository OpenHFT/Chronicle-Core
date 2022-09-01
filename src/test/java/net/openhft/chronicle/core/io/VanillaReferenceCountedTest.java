package net.openhft.chronicle.core.io;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class VanillaReferenceCountedTest extends MonitorReferenceCountedContractTest {

    private AtomicInteger onReleasedCallCount;

    @Before
    public void setUp() {
        onReleasedCallCount = new AtomicInteger(0);
    }

    @Override
    protected VanillaReferenceCounted createReferenceCounted() {
        return new VanillaReferenceCounted(onReleasedCallCount::incrementAndGet, VanillaReferenceCounted.class);
    }

    @Test
    public void createdHereWillReturnNull() {
        final VanillaReferenceCounted referenceCounted = createReferenceCounted();
        assertNull(referenceCounted.createdHere());
    }

    /**
     * This is another deviation from the contract, it is supposed to return false
     */
    @Test
    public void reservedByWillThrowWhenResourceHasBeenReleased() {
        final VanillaReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.releaseLast();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertThrows(IllegalStateException.class, () -> referenceCounted.reservedBy(a));
    }

    @Test
    public void reservedByWillReturnTrueWheneverThereAreAnyReferencesHeld() {
        final VanillaReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertTrue(referenceCounted.reservedBy(a));

        ReferenceOwner b = ReferenceOwner.temporary("b");
        assertTrue(referenceCounted.reservedBy(b));

        referenceCounted.releaseLast();
    }
}