package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.Supplier;
import org.junit.Test;

public class ReferenceCountedTracerDiffblueTest {
  /**
  * Method under test: {@link ReferenceCountedTracer#onReleased(Runnable, Supplier, Class)}
  */
  @Test
  public void testOnReleased() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    Supplier<String> uniqueId = mock(Supplier.class);
    when(uniqueId.get()).thenReturn("Get");

    // Act and Assert
    assertTrue(ReferenceCountedTracer.onReleased(onRelease, uniqueId, Object.class) instanceof TracingReferenceCounted);
    verify(uniqueId).get();
  }

  /**
   * Method under test: {@link ReferenceCountedTracer#onReleased(Runnable, Supplier, Class)}
   */
  @Test
  public void testOnReleased2() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    Supplier<String> uniqueId = mock(Supplier.class);
    when(uniqueId.get()).thenThrow(new ClosedIllegalStateException("init"));

    // Act and Assert
    assertThrows(ClosedIllegalStateException.class,
        () -> ReferenceCountedTracer.onReleased(onRelease, uniqueId, Object.class));
    verify(uniqueId).get();
  }

  /**
   * Method under test: {@link ReferenceCountedTracer#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted vanillaReferenceCounted = new VanillaReferenceCounted(onRelease, Object.class);

    // Act
    vanillaReferenceCounted.throwExceptionIfReleased();

    // Assert that nothing has changed
    assertFalse(vanillaReferenceCounted.unmonitored());
  }

  /**
   * Method under test: {@link ReferenceCountedTracer#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased2() throws ClosedIllegalStateException {
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
   * Method under test: {@link ReferenceCountedTracer#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased3() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    dualReferenceCounted.throwExceptionIfReleased();

    // Assert that nothing has changed
    assertEquals(1, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link ReferenceCountedTracer#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased4() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new TracingReferenceCounted(onRelease2, "42", Object.class));

    // Act
    dualReferenceCounted.throwExceptionIfReleased();

    // Assert that nothing has changed
    assertEquals(1, dualReferenceCounted.refCount());
  }
}

