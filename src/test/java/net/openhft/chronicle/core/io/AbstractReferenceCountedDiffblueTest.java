package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class AbstractReferenceCountedDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link AbstractReferenceCounted#unmonitor(ReferenceCounted)}
  */
  @Test
  public void testUnmonitor() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted counted = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    AbstractReferenceCounted.unmonitor(counted);

    // Assert that nothing has changed
    assertEquals(1, counted.refCount());
  }
}

