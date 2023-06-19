package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class ClosedIllegalStateExceptionDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ClosedIllegalStateException#ClosedIllegalStateException(String)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    ClosedIllegalStateException actualClosedIllegalStateException = new ClosedIllegalStateException("foo");

    // Assert
    assertNull(actualClosedIllegalStateException.getCause());
    assertEquals(0, actualClosedIllegalStateException.getSuppressed().length);
    assertEquals("foo", actualClosedIllegalStateException.getMessage());
    assertEquals("foo", actualClosedIllegalStateException.getLocalizedMessage());
  }

  /**
   * Method under test: {@link ClosedIllegalStateException#ClosedIllegalStateException(String, Throwable)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    Throwable cause = new Throwable();

    // Act
    ClosedIllegalStateException actualClosedIllegalStateException = new ClosedIllegalStateException("An error occurred",
        cause);

    // Assert
    Throwable cause2 = actualClosedIllegalStateException.getCause();
    assertSame(cause, cause2);
    Throwable[] suppressed = actualClosedIllegalStateException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("An error occurred", actualClosedIllegalStateException.getLocalizedMessage());
    assertEquals("An error occurred", actualClosedIllegalStateException.getMessage());
    assertNull(cause2.getLocalizedMessage());
    assertNull(cause2.getCause());
    assertNull(cause2.getMessage());
    assertSame(suppressed, cause2.getSuppressed());
  }
}

