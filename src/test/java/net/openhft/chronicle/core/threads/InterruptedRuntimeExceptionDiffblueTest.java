package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class InterruptedRuntimeExceptionDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link InterruptedRuntimeException#InterruptedRuntimeException()}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    InterruptedRuntimeException actualInterruptedRuntimeException = new InterruptedRuntimeException();

    // Assert
    assertNull(actualInterruptedRuntimeException.getCause());
    assertEquals(0, actualInterruptedRuntimeException.getSuppressed().length);
    assertNull(actualInterruptedRuntimeException.getMessage());
    assertNull(actualInterruptedRuntimeException.getLocalizedMessage());
  }

  /**
   * Method under test: {@link InterruptedRuntimeException#InterruptedRuntimeException(String)}
   */
  @Test
  public void testConstructor2() {
    // Arrange and Act
    InterruptedRuntimeException actualInterruptedRuntimeException = new InterruptedRuntimeException(
        "An error occurred");

    // Assert
    assertNull(actualInterruptedRuntimeException.getCause());
    assertEquals(0, actualInterruptedRuntimeException.getSuppressed().length);
    assertEquals("An error occurred", actualInterruptedRuntimeException.getMessage());
    assertEquals("An error occurred", actualInterruptedRuntimeException.getLocalizedMessage());
  }

  /**
   * Method under test: {@link InterruptedRuntimeException#InterruptedRuntimeException(String, Throwable)}
   */
  @Test
  public void testConstructor3() {
    // Arrange
    Throwable cause = new Throwable();

    // Act
    InterruptedRuntimeException actualInterruptedRuntimeException = new InterruptedRuntimeException("An error occurred",
        cause);

    // Assert
    Throwable cause2 = actualInterruptedRuntimeException.getCause();
    assertSame(cause, cause2);
    Throwable[] suppressed = actualInterruptedRuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("An error occurred", actualInterruptedRuntimeException.getLocalizedMessage());
    assertEquals("An error occurred", actualInterruptedRuntimeException.getMessage());
    assertNull(cause2.getLocalizedMessage());
    assertNull(cause2.getCause());
    assertNull(cause2.getMessage());
    assertSame(suppressed, cause2.getSuppressed());
  }

  /**
   * Method under test: {@link InterruptedRuntimeException#InterruptedRuntimeException(Throwable)}
   */
  @Test
  public void testConstructor4() {
    // Arrange
    Throwable cause = new Throwable();

    // Act
    InterruptedRuntimeException actualInterruptedRuntimeException = new InterruptedRuntimeException(cause);

    // Assert
    Throwable cause2 = actualInterruptedRuntimeException.getCause();
    assertSame(cause, cause2);
    Throwable[] suppressed = actualInterruptedRuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("java.lang.Throwable", actualInterruptedRuntimeException.getLocalizedMessage());
    assertEquals("java.lang.Throwable", actualInterruptedRuntimeException.getMessage());
    assertNull(cause2.getLocalizedMessage());
    assertNull(cause2.getCause());
    assertNull(cause2.getMessage());
    assertSame(suppressed, cause2.getSuppressed());
  }
}

