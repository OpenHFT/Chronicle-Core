package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ManagedCloseableDiffblueTest {
  /**
  * Method under test: {@link ManagedCloseable#warnAndCloseIfNotClosed()}
  */
  @Test
  public void testWarnAndCloseIfNotClosed() {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();

    // Act
    myCloseableReferenceCounted.warnAndCloseIfNotClosed();

    // Assert
    assertTrue(myCloseableReferenceCounted.isClosing());
    assertEquals(1, myCloseableReferenceCounted.performRelease);
  }

  /**
   * Method under test: {@link ManagedCloseable#warnAndCloseIfNotClosed()}
   */
  @Test
  public void testWarnAndCloseIfNotClosed2() {
    // Arrange
    AbstractCloseableGptTest.ConcreteCloseable concreteCloseable = new AbstractCloseableGptTest.ConcreteCloseable();

    // Act
    concreteCloseable.warnAndCloseIfNotClosed();

    // Assert
    assertTrue(concreteCloseable.isClosed());
  }

  /**
   * Method under test: {@link ManagedCloseable#warnAndCloseIfNotClosed()}
   */
  @Test
  public void testWarnAndCloseIfNotClosed3() {
    // Arrange
    AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted myCloseableReferenceCounted = new AbstractCloseableReferenceCountedTest.MyCloseableReferenceCounted();
    myCloseableReferenceCounted.addReferenceChangeListener(null);

    // Act
    myCloseableReferenceCounted.warnAndCloseIfNotClosed();

    // Assert
    assertTrue(myCloseableReferenceCounted.isClosing());
  }
}

