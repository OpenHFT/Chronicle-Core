package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class VanillaReferenceCountedDiffblueTest {
  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link VanillaReferenceCounted#VanillaReferenceCounted(Runnable, Class)}
  *   <li>{@link VanillaReferenceCounted#unmonitored(boolean)}
  *   <li>{@link VanillaReferenceCounted#toString()}
  * </ul>
  */
  @Test
  public void testConstructor() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act
    VanillaReferenceCounted actualVanillaReferenceCounted = new VanillaReferenceCounted(onRelease, Object.class);
    actualVanillaReferenceCounted.unmonitored(true);

    // Assert that nothing has changed
    assertEquals("1", actualVanillaReferenceCounted.toString());
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#VanillaReferenceCounted(Runnable, Class)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertFalse((new VanillaReferenceCounted(onRelease, Object.class)).unmonitored());
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#createdHere()}
   */
  @Test
  public void testCreatedHere() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertNull((new VanillaReferenceCounted(onRelease, Object.class)).createdHere());
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted vanillaReferenceCounted = new VanillaReferenceCounted(onRelease, Object.class);

    // Act and Assert
    assertTrue(vanillaReferenceCounted.reservedBy(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted vanillaReferenceCounted = new VanillaReferenceCounted(onRelease, Object.class);

    // Act
    vanillaReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert that nothing has changed
    assertFalse(vanillaReferenceCounted.unmonitored());
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted vanillaReferenceCounted = new VanillaReferenceCounted(onRelease, Object.class);
    VanillaReferenceOwner from = new VanillaReferenceOwner("Name");

    // Act
    vanillaReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name"));

    // Assert that nothing has changed
    assertFalse(vanillaReferenceCounted.unmonitored());
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted vanillaReferenceCounted = new VanillaReferenceCounted(onRelease, Object.class);

    // Act and Assert
    assertTrue(vanillaReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted vanillaReferenceCounted = new VanillaReferenceCounted(onRelease, Object.class);

    // Act
    vanillaReferenceCounted.release(new VanillaReferenceOwner("Name"));

    // Assert
    verify(onRelease).run();
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted vanillaReferenceCounted = new VanillaReferenceCounted(onRelease, Object.class);

    // Act
    vanillaReferenceCounted.releaseLast(new VanillaReferenceOwner("Name"));

    // Assert
    verify(onRelease).run();
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#refCount()}
   */
  @Test
  public void testRefCount() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertEquals(1, (new VanillaReferenceCounted(onRelease, Object.class)).refCount());
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#throwExceptionIfNotReleased()}
   */
  @Test
  public void testThrowExceptionIfNotReleased() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> (new VanillaReferenceCounted(onRelease, Object.class)).throwExceptionIfNotReleased());
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();

    // Act
    (new VanillaReferenceCounted(onRelease, Object.class)).warnAndReleaseIfNotReleased();

    // Assert
    verify(onRelease).run();
  }

  /**
   * Method under test: {@link VanillaReferenceCounted#unmonitored()}
   */
  @Test
  public void testUnmonitored() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertFalse((new VanillaReferenceCounted(onRelease, Object.class)).unmonitored());
  }
}

