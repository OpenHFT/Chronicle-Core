package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import org.junit.Test;

public class AbstractReferenceCountedDiffblueTest {
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

  /**
   * Method under test: {@link AbstractReferenceCounted#unmonitor(ReferenceCounted)}
   */
  @Test
  public void testUnmonitor2() {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted counted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    AbstractReferenceCounted.unmonitor(counted);

    // Assert
    assertTrue(counted.referenceCounted.unmonitored());
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#inThreadPerformRelease()}
   */
  @Test
  public void testInThreadPerformRelease() {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.inThreadPerformRelease();

    // Assert
    assertEquals(1, myCloseableReferenceCounted.performRelease);
  }

  /**
  * Method under test: {@link AbstractReferenceCounted#canReleaseInBackground()}
  */
  @Test
  public void testCanReleaseInBackground() {
    // Arrange, Act and Assert
    assertFalse((new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()).canReleaseInBackground());
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve2() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();
    myCloseableReferenceCounted.singleThreadedCheckDisabled(true);

    // Act
    myCloseableReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractReferenceCountedTest.MyReferenceCounted myReferenceCounted = new AbstractReferenceCountedTest.MyReferenceCounted();

    // Act and Assert
    assertTrue(myReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve2() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractReferenceCountedTest.MyReferenceCounted myReferenceCounted = new AbstractReferenceCountedTest.MyReferenceCounted();

    // Act and Assert
    assertTrue(myReferenceCounted.tryReserve(new AbstractCloseableGptTest.ConcreteCloseable()));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve3() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractReferenceCountedTest.MyReferenceCounted myReferenceCounted = new AbstractReferenceCountedTest.MyReferenceCounted();

    // Act and Assert
    assertTrue(myReferenceCounted.tryReserve(new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve4() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractReferenceCountedTest.MyReferenceCounted myReferenceCounted = new AbstractReferenceCountedTest.MyReferenceCounted();

    // Act and Assert
    assertTrue(myReferenceCounted.tryReserve((new BackgroundResourceReleaserMain()).new BGReferenceCounted()));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve5() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractReferenceCountedTest.MyReferenceCounted myReferenceCounted = new AbstractReferenceCountedTest.MyReferenceCounted();
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    // Act and Assert
    assertTrue(myReferenceCounted
        .tryReserve(new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class))));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve6() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractReferenceCountedTest.MyReferenceCounted myReferenceCounted = new AbstractReferenceCountedTest.MyReferenceCounted();
    Runnable onRelease = mock(Runnable.class);

    // Act and Assert
    assertTrue(myReferenceCounted.tryReserve(new VanillaReferenceCounted(onRelease, Object.class)));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve7() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractReferenceCountedTest.MyReferenceCounted myReferenceCounted = new AbstractReferenceCountedTest.MyReferenceCounted();
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);
    VanillaReferenceCounted a2 = new VanillaReferenceCounted(onRelease2, Object.class);

    Runnable onRelease3 = mock(Runnable.class);

    // Act and Assert
    assertTrue(myReferenceCounted.tryReserve(new DualReferenceCounted(a,
        new DualReferenceCounted(a2, new VanillaReferenceCounted(onRelease3, Object.class)))));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve8() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractReferenceCountedTest.MyReferenceCounted myReferenceCounted = new AbstractReferenceCountedTest.MyReferenceCounted();
    Runnable onRelease = mock(Runnable.class);

    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);
    a.unmonitored(true);
    Runnable onRelease2 = mock(Runnable.class);
    DualReferenceCounted b = new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class));

    Runnable onRelease3 = mock(Runnable.class);

    // Act and Assert
    assertTrue(myReferenceCounted
        .tryReserve(new DualReferenceCounted(new VanillaReferenceCounted(onRelease3, Object.class), b)));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#refCount()}
   */
  @Test
  public void testRefCount() {
    // Arrange, Act and Assert
    assertEquals(1, (new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()).refCount());
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#throwExceptionIfReleased()}
   */
  @Test
  public void testThrowExceptionIfReleased() throws ClosedIllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.throwExceptionIfReleased();

    // Assert that nothing has changed
    assertFalse(myCloseableReferenceCounted.isClosing());
    assertEquals(0, myCloseableReferenceCounted.performRelease);
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#warnAndReleaseIfNotReleased()}
   */
  @Test
  public void testWarnAndReleaseIfNotReleased() throws ClosedIllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.warnAndReleaseIfNotReleased();

    // Assert
    assertTrue(myCloseableReferenceCounted.isClosing());
    assertEquals(1, myCloseableReferenceCounted.performRelease);
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#threadSafetyCheck(boolean)}
   */
  @Test
  public void testThreadSafetyCheck() throws IllegalStateException {
    // Arrange, Act and Assert
    assertTrue((new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()).threadSafetyCheck(true));
    assertTrue((new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()).threadSafetyCheck(false));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#threadSafetyCheck(boolean)}
   */
  @Test
  public void testThreadSafetyCheck2() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();
    myCloseableReferenceCounted.singleThreadedCheckDisabled(true);

    // Act and Assert
    assertTrue(myCloseableReferenceCounted.threadSafetyCheck(true));
  }

  /**
   * Method under test: {@link AbstractReferenceCounted#referenceCountedUnmonitored(boolean)}
   */
  @Test
  public void testReferenceCountedUnmonitored() {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.referenceCountedUnmonitored(true);

    // Assert
    assertTrue(myCloseableReferenceCounted.referenceCounted.unmonitored());
  }
}

