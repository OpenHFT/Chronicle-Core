package net.openhft.chronicle.core.io;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;
import org.mockito.Mockito;

public class ReferenceChangeListenerManagerDiffblueTest {
  /**
  * Method under test: {@link ReferenceChangeListenerManager#notifyAdded(ReferenceOwner)}
  */
  @Test
  public void testNotifyAdded() {
    // Arrange
    ReferenceChangeListener referenceChangeListener = mock(ReferenceChangeListener.class);
    doNothing().when(referenceChangeListener)
        .onReferenceAdded(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any());
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    ReferenceChangeListenerManager referenceChangeListenerManager = new ReferenceChangeListenerManager(
        new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class)));
    referenceChangeListenerManager.add(referenceChangeListener);

    // Act
    referenceChangeListenerManager.notifyAdded(new VanillaReferenceOwner("Name"));

    // Assert
    verify(referenceChangeListener).onReferenceAdded(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any());
  }

  /**
   * Method under test: {@link ReferenceChangeListenerManager#notifyRemoved(ReferenceOwner)}
   */
  @Test
  public void testNotifyRemoved() {
    // Arrange
    ReferenceChangeListener referenceChangeListener = mock(ReferenceChangeListener.class);
    doNothing().when(referenceChangeListener)
        .onReferenceRemoved(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any());
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    ReferenceChangeListenerManager referenceChangeListenerManager = new ReferenceChangeListenerManager(
        new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class)));
    referenceChangeListenerManager.add(referenceChangeListener);

    // Act
    referenceChangeListenerManager.notifyRemoved(new VanillaReferenceOwner("Name"));

    // Assert
    verify(referenceChangeListener).onReferenceRemoved(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any());
  }

  /**
   * Method under test: {@link ReferenceChangeListenerManager#notifyTransferred(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testNotifyTransferred() {
    // Arrange
    ReferenceChangeListener referenceChangeListener = mock(ReferenceChangeListener.class);
    doNothing().when(referenceChangeListener)
        .onReferenceTransferred(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any(),
            Mockito.<ReferenceOwner>any());
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    ReferenceChangeListenerManager referenceChangeListenerManager = new ReferenceChangeListenerManager(
        new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class)));
    referenceChangeListenerManager.add(referenceChangeListener);
    VanillaReferenceOwner from = new VanillaReferenceOwner("Name");

    // Act
    referenceChangeListenerManager.notifyTransferred(from, new VanillaReferenceOwner("Name"));

    // Assert
    verify(referenceChangeListener).onReferenceTransferred(Mockito.<ReferenceCounted>any(),
        Mockito.<ReferenceOwner>any(), Mockito.<ReferenceOwner>any());
  }
}

