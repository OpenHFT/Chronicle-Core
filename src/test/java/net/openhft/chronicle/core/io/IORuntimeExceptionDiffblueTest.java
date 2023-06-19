package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class IORuntimeExceptionDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link IORuntimeException#IORuntimeException(String)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    IORuntimeException actualIoRuntimeException = new IORuntimeException("An error occurred");

    // Assert
    assertNull(actualIoRuntimeException.getCause());
    assertEquals(0, actualIoRuntimeException.getSuppressed().length);
    assertEquals("An error occurred", actualIoRuntimeException.getMessage());
    assertEquals("An error occurred", actualIoRuntimeException.getLocalizedMessage());
  }

  /**
   * Method under test: {@link IORuntimeException#IORuntimeException(String, Throwable)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    Throwable thrown = new Throwable();

    // Act
    IORuntimeException actualIoRuntimeException = new IORuntimeException("An error occurred", thrown);

    // Assert
    Throwable cause = actualIoRuntimeException.getCause();
    assertSame(thrown, cause);
    Throwable[] suppressed = actualIoRuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("An error occurred", actualIoRuntimeException.getLocalizedMessage());
    assertEquals("An error occurred", actualIoRuntimeException.getMessage());
    assertNull(cause.getLocalizedMessage());
    assertNull(cause.getCause());
    assertNull(cause.getMessage());
    assertSame(suppressed, cause.getSuppressed());
  }

  /**
   * Method under test: {@link IORuntimeException#IORuntimeException(Throwable)}
   */
  @Test
  public void testConstructor3() {
    // Arrange
    Throwable thrown = new Throwable();

    // Act
    IORuntimeException actualIoRuntimeException = new IORuntimeException(thrown);

    // Assert
    Throwable cause = actualIoRuntimeException.getCause();
    assertSame(thrown, cause);
    Throwable[] suppressed = actualIoRuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("java.lang.Throwable", actualIoRuntimeException.getLocalizedMessage());
    assertEquals("java.lang.Throwable", actualIoRuntimeException.getMessage());
    assertNull(cause.getLocalizedMessage());
    assertNull(cause.getCause());
    assertNull(cause.getMessage());
    assertSame(suppressed, cause.getSuppressed());
  }
}

