package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class ReferenceCountedDiffblueTest {
  /**
  * Method under test: {@link ReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
  */
  @Test
  public void testReserveTransfer() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));
    VanillaReferenceOwner from = new VanillaReferenceOwner("Name");

    // Act
    dualReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(1, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link ReferenceCounted#releaseLast()}
   */
  @Test
  public void testReleaseLast() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    doNothing().when(onRelease2).run();
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    dualReferenceCounted.releaseLast();

    // Assert
    verify(onRelease).run();
    verify(onRelease2).run();
    assertEquals(0, dualReferenceCounted.refCount());
  }
}

