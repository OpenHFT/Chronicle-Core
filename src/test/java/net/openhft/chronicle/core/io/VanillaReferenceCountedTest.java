package net.openhft.chronicle.core.io;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNull;

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
}
