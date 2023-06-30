package net.openhft.chronicle.core.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.openhft.chronicle.core.io.DualReferenceCounted;
import net.openhft.chronicle.core.io.MonitorReferenceCounted;
import net.openhft.chronicle.core.io.ReferenceCounted;
import org.junit.Test;

public class ReferenceCountedUtilsDiffblueTest {
  /**
  * Method under test: {@link ReferenceCountedUtils#unmonitor(ReferenceCounted)}
  */
  @Test
  public void testUnmonitor() {
    // Arrange
    MonitorReferenceCounted a = mock(MonitorReferenceCounted.class);
    when(a.refCount()).thenReturn(3);
    DualReferenceCounted counted = new DualReferenceCounted(a, mock(MonitorReferenceCounted.class));

    // Act
    ReferenceCountedUtils.unmonitor(counted);

    // Assert that nothing has changed
    verify(a).refCount();
    assertEquals(3, counted.refCount());
  }
}

