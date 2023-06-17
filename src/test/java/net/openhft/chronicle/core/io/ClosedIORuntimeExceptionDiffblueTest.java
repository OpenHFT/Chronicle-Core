package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ClosedIORuntimeExceptionDiffblueTest {
  /**
  * Method under test: {@link ClosedIORuntimeException#ClosedIORuntimeException(String)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    ClosedIORuntimeException actualClosedIORuntimeException = new ClosedIORuntimeException("An error occurred");

    // Assert
    assertNull(actualClosedIORuntimeException.getCause());
    assertEquals(0, actualClosedIORuntimeException.getSuppressed().length);
    assertEquals("An error occurred", actualClosedIORuntimeException.getMessage());
    assertEquals("An error occurred", actualClosedIORuntimeException.getLocalizedMessage());
  }

  /**
   * Method under test: {@link ClosedIORuntimeException#ClosedIORuntimeException(String, Throwable)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    Throwable thrown = new Throwable();

    // Act
    ClosedIORuntimeException actualClosedIORuntimeException = new ClosedIORuntimeException("An error occurred", thrown);

    // Assert
    Throwable cause = actualClosedIORuntimeException.getCause();
    assertSame(thrown, cause);
    Throwable[] suppressed = actualClosedIORuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("An error occurred", actualClosedIORuntimeException.getLocalizedMessage());
    assertEquals("An error occurred", actualClosedIORuntimeException.getMessage());
    assertNull(cause.getLocalizedMessage());
    assertNull(cause.getCause());
    assertNull(cause.getMessage());
    assertSame(suppressed, cause.getSuppressed());
  }
}

