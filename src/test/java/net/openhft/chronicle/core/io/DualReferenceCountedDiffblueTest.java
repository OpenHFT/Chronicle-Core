package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.awt.geom.AffineTransform;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImageOp;
import java.awt.image.renderable.RenderableImageProducer;
import org.junit.Test;

public class DualReferenceCountedDiffblueTest {
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
   * Method under test: {@link DualReferenceCounted#DualReferenceCounted(MonitorReferenceCounted, MonitorReferenceCounted)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);

    // Act
    DualReferenceCounted actualDualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

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
   * Method under test: {@link DualReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased2() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);

    // Act
    (new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class))).warnAndReleaseIfNotReleased();

    // Assert
    verify(onRelease).run();
  }

  /**
   * Method under test: {@link DualReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased3() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);

    // Act
    (new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class))).warnAndReleaseIfNotReleased();

    // Assert
    verify(onRelease).run();
  }

  /**
   * Method under test: {@link DualReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased4() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();

    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);
    a.unmonitored(true);
    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);

    // Act
    (new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class))).warnAndReleaseIfNotReleased();

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
   * Method under test: {@link DualReferenceCounted#createdHere()}
   */
  @Test
  public void testCreatedHere2() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);

    // Act and Assert
    assertNull((new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class))).createdHere());
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
   * Method under test: {@link DualReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

    // Act and Assert
    assertTrue(dualReferenceCounted.reservedBy(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link DualReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    RenderableImageOp renderableImageOp = new RenderableImageOp(null, new ParameterBlock());

    RenderableImageProducer onRelease2 = new RenderableImageProducer(renderableImageOp,
        new RenderContext(new AffineTransform()));

    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

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
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

    // Act
    dualReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act
    dualReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve4() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class)));

    // Act
    dualReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve5() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new TracingReferenceCounted(onRelease2, "42", Object.class));

    // Act
    dualReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve6() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));
    Runnable onRelease3 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease3, Object.class);

    Runnable onRelease4 = mock(Runnable.class);

    // Act
    dualReferenceCounted.reserve(new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease4, Object.class)));

    // Assert
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve7() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new TracingReferenceCounted(onRelease2, "net.openhft.chronicle.core.StackTrace", Object.class));

    // Act
    dualReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve8() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new TracingReferenceCounted(onRelease2, "42", Object.class));
    Runnable onRelease3 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease3, Object.class);

    Runnable onRelease4 = mock(Runnable.class);

    // Act
    dualReferenceCounted.reserve(new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease4, Object.class)));

    // Assert
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve9() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));
    Runnable onRelease3 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease3, Object.class);

    Runnable onRelease4 = mock(Runnable.class);
    DualReferenceCounted a3 = new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease4, Object.class));

    Runnable onRelease5 = mock(Runnable.class);

    // Act
    dualReferenceCounted.reserve(new DualReferenceCounted(a3, new VanillaReferenceCounted(onRelease5, Object.class)));

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
   * Method under test: {@link DualReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve2() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

    // Act and Assert
    assertTrue(dualReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve3() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));

    // Act and Assert
    assertTrue(dualReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve4() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class)));

    // Act and Assert
    assertTrue(dualReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve5() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new TracingReferenceCounted(onRelease2, "42", Object.class));

    // Act and Assert
    assertTrue(dualReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve6() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));
    Runnable onRelease3 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease3, Object.class);

    Runnable onRelease4 = mock(Runnable.class);

    // Act and Assert
    assertTrue(dualReferenceCounted
        .tryReserve(new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease4, Object.class))));
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve7() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new TracingReferenceCounted(onRelease2, "42", Object.class));
    Runnable onRelease3 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease3, Object.class);

    Runnable onRelease4 = mock(Runnable.class);

    // Act and Assert
    assertTrue(dualReferenceCounted
        .tryReserve(new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease4, Object.class))));
    assertEquals(2, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve8() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted a = new TracingReferenceCounted(onRelease, "42", Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new VanillaReferenceCounted(onRelease2, Object.class));
    Runnable onRelease3 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease3, Object.class);

    Runnable onRelease4 = mock(Runnable.class);
    DualReferenceCounted a3 = new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease4, Object.class));

    Runnable onRelease5 = mock(Runnable.class);

    // Act and Assert
    assertTrue(dualReferenceCounted
        .tryReserve(new DualReferenceCounted(a3, new VanillaReferenceCounted(onRelease5, Object.class))));
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
   * Method under test: {@link DualReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    doNothing().when(onRelease2).run();
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    doNothing().when(onRelease3).run();
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

    // Act
    dualReferenceCounted.release(new VanillaReferenceOwner("Name"));

    // Assert
    verify(onRelease).run();
    verify(onRelease2).run();
    verify(onRelease3).run();
    assertEquals(0, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    doNothing().when(onRelease2).run();
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);
    doNothing().when(onRelease3).run();
    DualReferenceCounted a3 = new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class)));

    Runnable onRelease4 = mock(Runnable.class);
    doNothing().when(onRelease4).run();
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a3,
        new VanillaReferenceCounted(onRelease4, Object.class));

    // Act
    dualReferenceCounted.release(new VanillaReferenceOwner("Name"));

    // Assert
    verify(onRelease).run();
    verify(onRelease2).run();
    verify(onRelease3).run();
    verify(onRelease4).run();
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
   * Method under test: {@link DualReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    doNothing().when(onRelease2).run();
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    doNothing().when(onRelease3).run();
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

    // Act
    dualReferenceCounted.releaseLast(new VanillaReferenceOwner("Name"));

    // Assert
    verify(onRelease).run();
    verify(onRelease2).run();
    verify(onRelease3).run();
    assertEquals(0, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    doNothing().when(onRelease2).run();
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);
    doNothing().when(onRelease3).run();
    DualReferenceCounted a3 = new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class)));

    Runnable onRelease4 = mock(Runnable.class);
    doNothing().when(onRelease4).run();
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a3,
        new VanillaReferenceCounted(onRelease4, Object.class));

    // Act
    dualReferenceCounted.releaseLast(new VanillaReferenceOwner("Name"));

    // Assert
    verify(onRelease).run();
    verify(onRelease2).run();
    verify(onRelease3).run();
    verify(onRelease4).run();
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
   * Method under test: {@link DualReferenceCounted#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased2() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

    // Act
    dualReferenceCounted.throwExceptionIfReleased();

    // Assert that nothing has changed
    assertEquals(1, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased3() throws ClosedIllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class)));

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
   * Method under test: {@link DualReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));
    VanillaReferenceOwner from = new VanillaReferenceOwner("Name");

    // Act
    dualReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(1, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class)));
    VanillaReferenceOwner from = new VanillaReferenceOwner("Name");

    // Act
    dualReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name"));

    // Assert
    assertEquals(1, dualReferenceCounted.refCount());
  }

  /**
   * Method under test: {@link DualReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer4() throws IllegalStateException {
    // Arrange
    VanillaReferenceCounted a = new VanillaReferenceCounted(null, Object.class);

    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease2, Object.class)));
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
   * Method under test: {@link DualReferenceCounted#unmonitored()}
   */
  @Test
  public void testUnmonitored2() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);

    // Act and Assert
    assertFalse((new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class))).unmonitored());
  }

  /**
   * Method under test: {@link DualReferenceCounted#unmonitored()}
   */
  @Test
  public void testUnmonitored3() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);
    a.unmonitored(true);
    Runnable onRelease2 = mock(Runnable.class);

    // Act and Assert
    assertTrue((new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class))).unmonitored());
  }

  /**
   * Method under test: {@link DualReferenceCounted#unmonitored(boolean)}
   */
  @Test
  public void testUnmonitored4() {
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

  /**
   * Method under test: {@link DualReferenceCounted#unmonitored(boolean)}
   */
  @Test
  public void testUnmonitored5() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted a2 = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a2,
        new VanillaReferenceCounted(onRelease3, Object.class));

    // Act
    dualReferenceCounted.unmonitored(true);

    // Assert
    assertTrue(dualReferenceCounted.unmonitored());
  }

  /**
   * Method under test: {@link DualReferenceCounted#unmonitored(boolean)}
   */
  @Test
  public void testUnmonitored6() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted dualReferenceCounted = new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class)));

    // Act
    dualReferenceCounted.unmonitored(true);

    // Assert
    assertTrue(dualReferenceCounted.unmonitored());
  }
}

