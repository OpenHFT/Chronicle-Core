package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.List;
import net.openhft.chronicle.core.UnsafePingPointMain;
import org.junit.Test;

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
   * Method under test: {@link TracingReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reservedBy(new AbstractCloseableGptTest.ConcreteCloseable()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> tracingReferenceCounted
        .reservedBy(new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy4() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reservedBy(new AbstractReferenceCountedTest.MyReferenceCounted()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reservedBy(ReferenceOwner)}
   */
  @Test
  public void testReservedBy5() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> tracingReferenceCounted
        .reservedBy(new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease3, Object.class))));
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
   * Method under test: {@link TracingReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve2() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    tracingReferenceCounted
        .addReferenceChangeListener(new ReferenceCountedContractTest.CounterReferenceChangeListener());

    // Act and Assert
    assertTrue(tracingReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve3() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertTrue(tracingReferenceCounted.tryReserve(new AbstractCloseableGptTest.ConcreteCloseable()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve4() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertTrue(
        tracingReferenceCounted.tryReserve(new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve5() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertTrue(tracingReferenceCounted.tryReserve(new AbstractReferenceCountedTest.MyReferenceCounted()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve6() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);

    // Act and Assert
    assertTrue(tracingReferenceCounted
        .tryReserve(new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease3, Object.class))));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve7() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "init", Object.class);

    // Act and Assert
    assertTrue(tracingReferenceCounted.tryReserve(new AbstractCloseableGptTest.ConcreteCloseable()));
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
   * Method under test: {@link TracingReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.release(new AbstractCloseableGptTest.ConcreteCloseable()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.release(new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease4() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.release(new AbstractReferenceCountedTest.MyReferenceCounted()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#release(ReferenceOwner)}
   */
  @Test
  public void testRelease5() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> tracingReferenceCounted
        .release(new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease3, Object.class))));
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
   * Method under test: {@link TracingReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    AbstractCloseableGptTest.ConcreteCloseable from = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted from = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer4() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    AbstractReferenceCountedTest.MyReferenceCounted from = new AbstractReferenceCountedTest.MyReferenceCounted();

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer5() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);
    DualReferenceCounted from = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease3, Object.class));

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#reserveTransfer(ReferenceOwner, ReferenceOwner)}
   */
  @Test
  public void testReserveTransfer6() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted from = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.reserveTransfer(from, new VanillaReferenceOwner("42")));
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
   * Method under test: {@link TracingReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast2() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.releaseLast(new AbstractCloseableGptTest.ConcreteCloseable()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast3() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> tracingReferenceCounted
        .releaseLast(new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast4() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> (new TracingReferenceCounted(onRelease, "42", Object.class)).releaseLast(null));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast5() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> tracingReferenceCounted.releaseLast(new AbstractReferenceCountedTest.MyReferenceCounted()));
  }

  /**
   * Method under test: {@link TracingReferenceCounted#releaseLast(ReferenceOwner)}
   */
  @Test
  public void testReleaseLast6() throws IllegalStateException {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> tracingReferenceCounted
        .releaseLast(new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease3, Object.class))));
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
   * Method under test: {@link TracingReferenceCounted#throwExceptionIfNotReleased()}
   */
  @Test
  public void testThrowExceptionIfNotReleased2() throws IllegalStateException {
    // Arrange
    UnsafePingPointMain onRelease = mock(UnsafePingPointMain.class);

    // Act and Assert
    assertThrows(ClosedIllegalStateException.class,
        () -> (new TracingReferenceCounted(onRelease, " reserved by ", Object.class)).throwExceptionIfNotReleased());
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
   * Method under test: {@link TracingReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased2() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doNothing().when(onRelease).run();

    TracingReferenceCounted tracingReferenceCounted = new TracingReferenceCounted(onRelease, "42", Object.class);
    tracingReferenceCounted.unmonitored(true);

    // Act
    tracingReferenceCounted.warnAndReleaseIfNotReleased();

    // Assert
    verify(onRelease).run();
  }

  /**
   * Method under test: {@link TracingReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased3() {
    // Arrange
    Runnable onRelease = mock(Runnable.class);
    doThrow(new IllegalStateException("Discarded without being released by ")).when(onRelease).run();

    // Act and Assert
    assertThrows(IllegalStateException.class,
        () -> (new TracingReferenceCounted(onRelease, "42", Object.class)).warnAndReleaseIfNotReleased());
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

