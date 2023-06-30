package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import org.junit.Test;

public class ReferenceChangeListenerDiffblueTest {
  /**
  * Method under test: {@link ReferenceChangeListener#onReferenceAdded(ReferenceCounted, ReferenceOwner)}
  */
  @Test
  public void testOnReferenceAdded() {
    // Arrange
    ReferenceCountedContractTest.CounterReferenceChangeListener counterReferenceChangeListener = new ReferenceCountedContractTest.CounterReferenceChangeListener();
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted referenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    counterReferenceChangeListener.onReferenceAdded(referenceCounted, new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(1, counterReferenceChangeListener.referenceAddedCount);
  }

  /**
   * Method under test: {@link ReferenceChangeListener#onReferenceRemoved(ReferenceCounted, ReferenceOwner)}
   */
  @Test
  public void testOnReferenceRemoved() {
    // Arrange
    ReferenceCountedContractTest.CounterReferenceChangeListener counterReferenceChangeListener = new ReferenceCountedContractTest.CounterReferenceChangeListener();
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted referenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    counterReferenceChangeListener.onReferenceRemoved(referenceCounted, new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(1, counterReferenceChangeListener.referenceRemovedCount);
  }

  /**
   * Method under test: {@link ReferenceChangeListener#onReferenceTransferred(ReferenceCounted, ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testOnReferenceTransferred() {
    // Arrange
    ReferenceCountedContractTest.CounterReferenceChangeListener counterReferenceChangeListener = new ReferenceCountedContractTest.CounterReferenceChangeListener();
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted referenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    VanillaReferenceOwner fromOwner = new VanillaReferenceOwner("Name");

    // Act
    counterReferenceChangeListener.onReferenceTransferred(referenceCounted, fromOwner,
        new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(1, counterReferenceChangeListener.referenceTransferredCount);
  }
}

