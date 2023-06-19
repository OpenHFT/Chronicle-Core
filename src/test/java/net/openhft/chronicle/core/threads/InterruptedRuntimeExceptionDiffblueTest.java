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
    InterruptedRuntimeException actualInterruptedRuntimeException = new InterruptedRuntimeException("foo");

    // Assert
    assertNull(actualInterruptedRuntimeException.getCause());
    assertEquals(0, actualInterruptedRuntimeException.getSuppressed().length);
    assertEquals("foo", actualInterruptedRuntimeException.getMessage());
    assertEquals("foo", actualInterruptedRuntimeException.getLocalizedMessage());
  }

  /**
   * Method under test: {@link InterruptedRuntimeException#InterruptedRuntimeException(String, Throwable)}
   */
  @Test
  public void testConstructor3() {
    // Arrange
    Throwable throwable = new Throwable();

    // Act
    InterruptedRuntimeException actualInterruptedRuntimeException = new InterruptedRuntimeException("foo", throwable);

    // Assert
    Throwable cause = actualInterruptedRuntimeException.getCause();
    assertSame(throwable, cause);
    Throwable[] suppressed = actualInterruptedRuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("foo", actualInterruptedRuntimeException.getLocalizedMessage());
    assertEquals("foo", actualInterruptedRuntimeException.getMessage());
    assertNull(cause.getLocalizedMessage());
    assertNull(cause.getCause());
    assertNull(cause.getMessage());
    assertSame(suppressed, cause.getSuppressed());
  }

  /**
   * Method under test: {@link InterruptedRuntimeException#InterruptedRuntimeException(Throwable)}
   */
  @Test
  public void testConstructor4() {
    // Arrange
    Throwable throwable = new Throwable();

    // Act
    InterruptedRuntimeException actualInterruptedRuntimeException = new InterruptedRuntimeException(throwable);

    // Assert
    Throwable cause = actualInterruptedRuntimeException.getCause();
    assertSame(throwable, cause);
    Throwable[] suppressed = actualInterruptedRuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("java.lang.Throwable", actualInterruptedRuntimeException.getLocalizedMessage());
    assertEquals("java.lang.Throwable", actualInterruptedRuntimeException.getMessage());
    assertNull(cause.getLocalizedMessage());
    assertNull(cause.getCause());
    assertNull(cause.getMessage());
    assertSame(suppressed, cause.getSuppressed());
  }
}

