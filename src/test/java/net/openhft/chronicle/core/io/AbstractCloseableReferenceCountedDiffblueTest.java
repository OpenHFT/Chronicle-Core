package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class AbstractCloseableReferenceCountedDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#reserve(ReferenceOwner)}
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
   * Method under test: {@link AbstractCloseableReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve2() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.reserve(new VanillaReferenceOwner(" on "));

    // Assert
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve3() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.reserve(new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted());

    // Assert
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve5() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    // Act
    myCloseableReferenceCounted
        .reserve(new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class)));

    // Assert
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#reserve(ReferenceOwner)}
   */
  @Test
  public void testReserve6() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();
    myCloseableReferenceCounted.singleThreadedCheckDisabled(true);

    // Act
    myCloseableReferenceCounted.reserve(new VanillaReferenceOwner("Name"));

    // Assert
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act and Assert
    assertTrue(myCloseableReferenceCounted.tryReserve(new VanillaReferenceOwner("Name")));
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve2() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act and Assert
    assertTrue(myCloseableReferenceCounted
        .tryReserve(new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()));
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#tryReserve(ReferenceOwner)}
   */
  @Test
  public void testTryReserve4() throws IllegalArgumentException, IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();
    Runnable onRelease = mock(Runnable.class);
    VanillaReferenceCounted a = new VanillaReferenceCounted(onRelease, Object.class);

    Runnable onRelease2 = mock(Runnable.class);

    // Act and Assert
    assertTrue(myCloseableReferenceCounted
        .tryReserve(new DualReferenceCounted(a, new VanillaReferenceCounted(onRelease2, Object.class))));
    assertFalse(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#setClosed()}
   */
  @Test
  public void testSetClosed() {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.setClosed();

    // Assert
    assertTrue(myCloseableReferenceCounted.isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#throwExceptionIfClosed()}
   */
  @Test
  public void testThrowExceptionIfClosed() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();
    myCloseableReferenceCounted.singleThreadedCheckDisabled(true);

    // Act
    myCloseableReferenceCounted.throwExceptionIfClosed();

    // Assert that nothing has changed
    assertFalse(myCloseableReferenceCounted.isClosing());
    assertEquals(0, myCloseableReferenceCounted.performRelease);
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#throwExceptionIfClosedInSetter()}
   */
  @Test
  public void testThrowExceptionIfClosedInSetter() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.throwExceptionIfClosedInSetter();

    // Assert that nothing has changed
    assertFalse(myCloseableReferenceCounted.isClosing());
    assertEquals(0, myCloseableReferenceCounted.performRelease);
  }

  /**
   * Method under test: {@link AbstractCloseableReferenceCounted#throwExceptionIfClosedInSetter()}
   */
  @Test
  public void testThrowExceptionIfClosedInSetter2() throws IllegalStateException {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();
    myCloseableReferenceCounted.singleThreadedCheckDisabled(true);

    // Act
    myCloseableReferenceCounted.throwExceptionIfClosedInSetter();

    // Assert that nothing has changed
    assertFalse(myCloseableReferenceCounted.isClosing());
    assertEquals(0, myCloseableReferenceCounted.performRelease);
  }

  /**
  * Method under test: {@link AbstractCloseableReferenceCounted#isClosed()}
  */
  @Test
  public void testIsClosed() {
    // Arrange, Act and Assert
    assertFalse((new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted()).isClosed());
  }
}

