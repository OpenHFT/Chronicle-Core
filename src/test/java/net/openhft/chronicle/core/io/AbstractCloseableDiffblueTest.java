package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class AbstractCloseableDiffblueTest {
  /**
   * Method under test: {@link AbstractCloseable#close()}
   */
  @Test
  public void testClose() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.close();

    // Assert
    assertTrue(concreteCloseable.isClosed());
  }

  /**
  * Method under test: {@link AbstractCloseable#assertCloseable()}
  */
  @Test
  public void testAssertCloseable() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.assertCloseable();

    // Assert that nothing has changed
    assertFalse(concreteCloseable.isClosed());
    assertFalse(concreteCloseable.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable.Finalizer#finalize()}
   */
  @Test
  public void testFinalizerFinalize() throws Throwable {
    // Arrange
    AbstractCloseable abstractCloseable = mock(AbstractCloseable.class);
    doNothing().when(abstractCloseable).warnAndCloseIfNotClosed();

    // Act
    (abstractCloseable.new Finalizer()).finalize();

    // Assert
    verify(abstractCloseable).warnAndCloseIfNotClosed();
  }

  /**
   * Method under test: {@link AbstractCloseable#isInUserThread()}
   */
  @Test
  public void testIsInUserThread() {
    // Arrange, Act and Assert
    assertTrue((new AbstractCloseableGptTest.ConcreteCloseable()).isInUserThread());
  }

  /**
   * Method under test: {@link AbstractCloseable#throwExceptionIfClosed()}
   */
  @Test
  public void testThrowExceptionIfClosed() throws IllegalStateException {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();
    concreteCloseable.singleThreadedCheckDisabled(true);

    // Act
    concreteCloseable.throwExceptionIfClosed();

    // Assert that nothing has changed
    assertFalse(concreteCloseable.isClosed());
    assertTrue(concreteCloseable.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#throwExceptionIfClosedInSetter()}
   */
  @Test
  public void testThrowExceptionIfClosedInSetter() throws IllegalStateException {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.throwExceptionIfClosedInSetter();

    // Assert that nothing has changed
    assertFalse(concreteCloseable.isClosed());
    assertFalse(concreteCloseable.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#throwExceptionIfClosedInSetter()}
   */
  @Test
  public void testThrowExceptionIfClosedInSetter2() throws IllegalStateException {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();
    concreteCloseable.singleThreadedCheckDisabled(true);

    // Act
    concreteCloseable.throwExceptionIfClosedInSetter();

    // Assert that nothing has changed
    assertFalse(concreteCloseable.isClosed());
    assertTrue(concreteCloseable.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#warnAndCloseIfNotClosed()}
   */
  @Test
  public void testWarnAndCloseIfNotClosed() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.warnAndCloseIfNotClosed();

    // Assert
    assertTrue(concreteCloseable.isClosed());
  }

  /**
   * Method under test: {@link AbstractCloseable#callPerformClose()}
   */
  @Test
  public void testCallPerformClose() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.callPerformClose();

    // Assert
    assertTrue(concreteCloseable.isClosed());
  }

  /**
   * Method under test: {@link AbstractCloseable#isClosing()}
   */
  @Test
  public void testIsClosing() {
    // Arrange, Act and Assert
    assertFalse((new AbstractCloseableGptTest.ConcreteCloseable()).isClosing());
  }

  /**
   * Method under test: {@link AbstractCloseable#isClosed()}
   */
  @Test
  public void testIsClosed() {
    // Arrange, Act and Assert
    assertFalse((new AbstractCloseableGptTest.ConcreteCloseable()).isClosed());
  }

  /**
   * Method under test: {@link AbstractCloseable#shouldPerformCloseInBackground()}
   */
  @Test
  public void testShouldPerformCloseInBackground() {
    // Arrange, Act and Assert
    assertFalse((new AbstractCloseableGptTest.ConcreteCloseable()).shouldPerformCloseInBackground());
  }

  /**
   * Method under test: {@link AbstractCloseable#shouldWaitForClosed()}
   */
  @Test
  public void testShouldWaitForClosed() {
    // Arrange, Act and Assert
    assertFalse((new AbstractCloseableGptTest.ConcreteCloseable()).shouldWaitForClosed());
  }

  /**
   * Method under test: {@link AbstractCloseable#threadSafetyCheck(boolean)}
   */
  @Test
  public void testThreadSafetyCheck() throws IllegalStateException {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.threadSafetyCheck(false);

    // Assert that nothing has changed
    assertFalse(concreteCloseable.isClosed());
    assertFalse(concreteCloseable.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#threadSafetyCheck(boolean)}
   */
  @Test
  public void testThreadSafetyCheck2() throws IllegalStateException {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();
    concreteCloseable.singleThreadedCheckDisabled(true);

    // Act
    concreteCloseable.threadSafetyCheck(true);

    // Assert that nothing has changed
    assertFalse(concreteCloseable.isClosed());
    assertTrue(concreteCloseable.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#disableThreadSafetyCheck()}
   */
  @Test
  public void testDisableThreadSafetyCheck() {
    // Arrange, Act and Assert
    assertFalse((new AbstractCloseableGptTest.ConcreteCloseable()).disableThreadSafetyCheck());
  }

  /**
   * Method under test: {@link AbstractCloseable#disableThreadSafetyCheck()}
   */
  @Test
  public void testDisableThreadSafetyCheck2() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();
    concreteCloseable.singleThreadedCheckDisabled(true);

    // Act and Assert
    assertTrue(concreteCloseable.disableThreadSafetyCheck());
  }

  /**
   * Method under test: {@link AbstractCloseable#disableThreadSafetyCheck(boolean)}
   */
  @Test
  public void testDisableThreadSafetyCheck3() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    AbstractCloseable actualDisableThreadSafetyCheckResult = concreteCloseable.disableThreadSafetyCheck(true);

    // Assert
    assertSame(concreteCloseable, actualDisableThreadSafetyCheckResult);
    assertTrue(actualDisableThreadSafetyCheckResult.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#disableThreadSafetyCheck(boolean)}
   */
  @Test
  public void testDisableThreadSafetyCheck4() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    AbstractCloseable actualDisableThreadSafetyCheckResult = concreteCloseable.disableThreadSafetyCheck(false);

    // Assert
    assertSame(concreteCloseable, actualDisableThreadSafetyCheckResult);
    assertFalse(actualDisableThreadSafetyCheckResult.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#singleThreadedCheckDisabled()}
   */
  @Test
  public void testSingleThreadedCheckDisabled() {
    // Arrange, Act and Assert
    assertFalse((new AbstractCloseableGptTest.ConcreteCloseable()).singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#singleThreadedCheckDisabled(boolean)}
   */
  @Test
  public void testSingleThreadedCheckDisabled2() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.singleThreadedCheckDisabled(true);

    // Assert
    assertTrue(concreteCloseable.singleThreadedCheckDisabled());
  }

  /**
   * Method under test: {@link AbstractCloseable#singleThreadedCheckDisabled(boolean)}
   */
  @Test
  public void testSingleThreadedCheckDisabled3() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.singleThreadedCheckDisabled(false);

    // Assert
    assertFalse(concreteCloseable.singleThreadedCheckDisabled());
  }
}

