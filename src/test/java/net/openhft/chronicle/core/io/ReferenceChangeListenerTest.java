package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class ReferenceChangeListenerTest {

    @Test
    public void testOnReferenceAdded() {
        ReferenceChangeListener listener = mock(ReferenceChangeListener.class);
        ReferenceCounted referenceCounted = mock(ReferenceCounted.class);
        ReferenceOwner referenceOwner = mock(ReferenceOwner.class);

        listener.onReferenceAdded(referenceCounted, referenceOwner);

        verify(listener, times(1)).onReferenceAdded(referenceCounted, referenceOwner);
    }

    @Test
    public void testOnReferenceRemoved() {
        ReferenceChangeListener listener = mock(ReferenceChangeListener.class);
        ReferenceCounted referenceCounted = mock(ReferenceCounted.class);
        ReferenceOwner referenceOwner = mock(ReferenceOwner.class);

        listener.onReferenceRemoved(referenceCounted, referenceOwner);

        verify(listener, times(1)).onReferenceRemoved(referenceCounted, referenceOwner);
    }

    @Test
    public void testOnReferenceTransferred() {
        ReferenceChangeListener listener = mock(ReferenceChangeListener.class);
        ReferenceCounted referenceCounted = mock(ReferenceCounted.class);
        ReferenceOwner fromOwner = mock(ReferenceOwner.class);
        ReferenceOwner toOwner = mock(ReferenceOwner.class);

        listener.onReferenceTransferred(referenceCounted, fromOwner, toOwner);

        verify(listener, times(1)).onReferenceTransferred(referenceCounted, fromOwner, toOwner);
    }
}
