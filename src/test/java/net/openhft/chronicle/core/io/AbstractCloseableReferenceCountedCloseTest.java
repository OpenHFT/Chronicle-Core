package net.openhft.chronicle.core.io;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link AbstractCloseableReferenceCounted#close()}.
 */
public class AbstractCloseableReferenceCountedCloseTest {

    static class TestCloseableReferenceCounted extends AbstractCloseableReferenceCounted {
        int releaseCount;

        @Override
        protected void performRelease() {
            releaseCount++;
        }
    }

    @Test
    public void closeCallsPerformReleaseOnceAndMarksClosed() {
        TestCloseableReferenceCounted rc = new TestCloseableReferenceCounted();
        assertEquals(1, rc.refCount());
        assertFalse(rc.isClosed());
        assertEquals(0, rc.releaseCount);

        rc.close();
        assertTrue(rc.isClosed());
        assertEquals(0, rc.refCount());
        assertEquals(1, rc.releaseCount);

        // subsequent closes should have no effect
        rc.close();
        assertTrue(rc.isClosed());
        assertEquals(1, rc.releaseCount);
    }
}

