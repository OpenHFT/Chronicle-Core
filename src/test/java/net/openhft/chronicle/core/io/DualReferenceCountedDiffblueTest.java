package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class DualReferenceCountedDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link DualReferenceCounted#DualReferenceCounted(MonitorReferenceCounted, MonitorReferenceCounted)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    // Act
    DualReferenceCounted actualDualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Assert
    assertEquals(1, actualDualReferenceCounted.refCount());
    assertFalse(actualDualReferenceCounted.unmonitored());
  }

  /**
   * Method under test: {@link DualReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    // Act
    (new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class))).warnAndReleaseIfNotReleased();

    // Assert
    verify(onRelease).run();
  }

  /**
   * Method under test: {@link DualReferenceCounted#throwExceptionIfNotReleased()}
   */
  @Test
  public void testThrowExceptionIfNotReleased() throws IllegalStateException {
    // Arrange
    MonitorReferenceCounted a = mock(MonitorReferenceCounted.class);
    when(a.refCount()).thenReturn(3);
    doNothing().when(a).throwExceptionIfNotReleased();
    Runnable onRelease = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease, Object.class));

    // Act
    dualReferenceCounted.throwExceptionIfNotReleased();

    // Assert that nothing has changed
    verify(a).refCount();
    verify(a).throwExceptionIfNotReleased();
    assertEquals(3, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#createdHere()}
   */
  @Test
  public void testCreatedHere() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    // Act and Assert
    assertNull((new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class))).createdHere());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act and Assert
    assertTrue(dualReferenceCounted.reservedBy(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    dualReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act and Assert
    assertTrue(dualReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    doNothing().when(onRelease2).run();
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    dualReferenceCounted.release(new VanillaReferenceOwner("Name"));

    // Assert
    verify(onRelease).run();
    verify(onRelease2).run();
    assertEquals(0, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#releaseLast(ReferenceOwner)}
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
    dualReferenceCounted.releaseLast(new VanillaReferenceOwner("Name"));

    // Assert
    verify(onRelease).run();
    verify(onRelease2).run();
    assertEquals(0, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#refCount()}
   */
  @Test
  public void testRefCount() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    // Act and Assert
    assertEquals(1, (new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class))).refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    dualReferenceCounted.throwExceptionIfReleased();

    // Assert that nothing has changed
    assertEquals(1, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
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
   * Method under test: {@link DualReferenceCounted#unmonitored()}
   */
  @Test
  public void testUnmonitored() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    // Act and Assert
    assertFalse((new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class))).unmonitored());
  }

  /**
   * Method under test: {@link DualReferenceCounted#unmonitored(boolean)}
   */
  @Test
  public void testUnmonitored2() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    dualReferenceCounted.unmonitored(true);

    // Assert
    assertTrue(dualReferenceCounted.unmonitored());
  }
}

