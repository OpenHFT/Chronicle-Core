package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;

import net.openhft.chronicle.core.internal.CloseableUtils;
import org.junit.Test;
import org.mockito.Mockito;

public class TracingReferenceCountedDiffblueTest {
  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link TracingReferenceCounted#TracingReferenceCounted(Runnable, String, Class)}
  *   <li>{@link TracingReferenceCounted#unmonitored(boolean)}
  *   <li>{@link TracingReferenceCounted#toString()}
  * </ul>
  */
  @Test
  public void testConstructor() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act
    TracingReferenceCounted actualTracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    actualTracingReferenceCounted.unmonitored(true);

    // Assert
    assertEquals("42 - [INIT]", actualTracingReferenceCounted.toString());
  }

  /**
   * Method under test: {@link TracingReferenceCounted#TracingReferenceCounted(Runnable, String, Class)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertFalse((new TracingReferenceCounted(onRelease, "42", Object.class)).unmonitored());
  }

  /**
   * Method under test: {@link CloseableUtils#asString(Object)}
   */
  @Test
  public void testAsString() {
    // Arrange, Act and Assert
    assertEquals("VanillaReferenceOwner{name='@'} closed=false",
        CloseableUtils.asString(new VanillaReferenceOwner("@")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reservedBy(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve() throws IllegalStateException {
    // Arrange
    ReferenceChangeListener referenceChangeListener = mock(ReferenceChangeListener.class);
    doNothing().when(referenceChangeListener)
        .onReferenceAdded(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any());
    Runnable onRelease = mock(Runnable.class);

    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    tracingReferenceCounted.addReferenceChangeListener(referenceChangeListener);

    // Act
    tracingReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    verify(referenceChangeListener).onReferenceAdded(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any());
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve2() throws IllegalStateException {
    // Arrange
    ReferenceChangeListener referenceChangeListener = mock(ReferenceChangeListener.class);
    doNothing().when(referenceChangeListener)
        .onReferenceAdded(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any());
    ReferenceChangeListener referenceChangeListener2 = mock(ReferenceChangeListener.class);
    doThrow(new NullPointerException("foo")).when(referenceChangeListener2)
        .onReferenceAdded(Mockito.<ReferenceCounted>any(), Mockito.<ReferenceOwner>any());
    Runnable onRelease = mock(Runnable.class);

    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    tracingReferenceCounted.addReferenceChangeListener(referenceChangeListener2);
    tracingReferenceCounted.addReferenceChangeListener(referenceChangeListener);
    DualReferenceCounted id = mock(DualReferenceCounted.class);
    when(id.refCount()).thenThrow(new IllegalArgumentException("foo"));
    when(id.referenceName()).thenThrow(new IllegalArgumentException("foo"));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> tracingReferenceCounted.reserve(id));
    verify(id).referenceName();
  }

  /**
   * Method under test: {@link TracingReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertTrue(tracingReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> tracingReferenceCounted.release(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    VanillaReferenceOwner from = new VanillaReferenceOwner("Name");

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#referencesAsString()}
   */
  @Test
  public void testReferencesAsString() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act
    List<String> actualReferencesAsStringResult = (new TracingReferenceCounted(onRelease, "42", Object.class))
        .referencesAsString();

    // Assert
    assertEquals(1, actualReferencesAsStringResult.size());
    assertEquals("INIT", actualReferencesAsStringResult.get(0));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.releaseLast(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#refCount()}
   */
  @Test
  public void testRefCount() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertEquals(1, (new TracingReferenceCounted(onRelease, "42", Object.class)).refCount());
  }

  /**
   * Method under test: {@link TracingReferenceCounted#throwExceptionIfNotReleased()}
   */
  @Test
  public void testThrowExceptionIfNotReleased() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertThrows(ClosedIllegalStateException.class,
        () -> (new TracingReferenceCounted(onRelease, "42", Object.class)).throwExceptionIfNotReleased());
  }

  /**
   * Method under test: {@link TracingReferenceCounted#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act
    tracingReferenceCounted.throwExceptionIfReleased();

    // Assert that nothing has changed
    assertFalse(tracingReferenceCounted.unmonitored());
  }

  /**
   * Method under test: {@link TracingReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();

    // Act
    (new TracingReferenceCounted(onRelease, "42", Object.class)).warnAndReleaseIfNotReleased();

    // Assert
    verify(onRelease).run();
  }

  /**
   * Method under test: {@link TracingReferenceCounted#unmonitored()}
   */
  @Test
  public void testUnmonitored() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertFalse((new TracingReferenceCounted(onRelease, "42", Object.class)).unmonitored());
  }
}

