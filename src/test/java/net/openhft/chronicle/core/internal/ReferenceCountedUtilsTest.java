package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.AbstractReferenceCounted;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReferenceCountedUtilsTest {

    @BeforeEach
    public void setUp() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    @AfterEach
    public void tearDown() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    @Test
    public void unmonitorShouldRemoveReference() {
        AbstractReferenceCounted referenceCounted = mock(AbstractReferenceCounted.class);
        when(referenceCounted.refCount()).thenReturn(1);

        ReferenceCountedUtils.add(referenceCounted);
        ReferenceCountedUtils.unmonitor(referenceCounted);

        assertDoesNotThrow(ReferenceCountedUtils::assertReferencesReleased, "Unmonitored references should not be checked");
    }
}
