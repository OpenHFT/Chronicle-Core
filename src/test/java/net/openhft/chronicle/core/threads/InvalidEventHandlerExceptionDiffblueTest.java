package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class InvalidEventHandlerExceptionDiffblueTest {
  /**
  * Method under test: {@link InvalidEventHandlerException#InvalidEventHandlerException()}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    InvalidEventHandlerException actualInvalidEventHandlerException = new InvalidEventHandlerException();

    // Assert
    assertNull(actualInvalidEventHandlerException.getCause());
    assertEquals(0, actualInvalidEventHandlerException.getSuppressed().length);
    assertNull(actualInvalidEventHandlerException.getMessage());
    assertNull(actualInvalidEventHandlerException.getLocalizedMessage());
  }

  /**
   * Method under test: {@link InvalidEventHandlerException#InvalidEventHandlerException(String)}
   */
  @Test
  public void testConstructor2() {
    // Arrange and Act
    InvalidEventHandlerException actualInvalidEventHandlerException = new InvalidEventHandlerException(
        "An error occurred");

    // Assert
    assertNull(actualInvalidEventHandlerException.getCause());
    assertEquals(0, actualInvalidEventHandlerException.getSuppressed().length);
    assertEquals("An error occurred", actualInvalidEventHandlerException.getMessage());
    assertEquals("An error occurred", actualInvalidEventHandlerException.getLocalizedMessage());
  }

  /**
   * Method under test: {@link InvalidEventHandlerException#InvalidEventHandlerException(Throwable)}
   */
  @Test
  public void testConstructor3() {
    // Arrange
    Throwable cause = new Throwable();

    // Act
    InvalidEventHandlerException actualInvalidEventHandlerException = new InvalidEventHandlerException(cause);

    // Assert
    Throwable cause2 = actualInvalidEventHandlerException.getCause();
    assertSame(cause, cause2);
    Throwable[] suppressed = actualInvalidEventHandlerException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("java.lang.Throwable", actualInvalidEventHandlerException.getLocalizedMessage());
    assertEquals("java.lang.Throwable", actualInvalidEventHandlerException.getMessage());
    assertNull(cause2.getLocalizedMessage());
    assertNull(cause2.getCause());
    assertNull(cause2.getMessage());
    assertSame(suppressed, cause2.getSuppressed());
  }
}

