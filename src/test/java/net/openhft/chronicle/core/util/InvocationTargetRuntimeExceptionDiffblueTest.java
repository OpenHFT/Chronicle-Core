package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.lang.reflect.InvocationTargetException;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class InvocationTargetRuntimeExceptionDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link InvocationTargetRuntimeException#InvocationTargetRuntimeException(Throwable)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    Throwable cause = new Throwable();

    // Act and Assert
    assertSame((new InvocationTargetRuntimeException(cause)).getCause(), cause);
  }

  /**
   * Method under test: {@link InvocationTargetRuntimeException#InvocationTargetRuntimeException(Throwable)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    Throwable throwable = new Throwable();
    InvocationTargetException cause = new InvocationTargetException(throwable, "foo");

    // Act
    InvocationTargetRuntimeException actualInvocationTargetRuntimeException = new InvocationTargetRuntimeException(
        cause);

    // Assert
    Throwable cause2 = actualInvocationTargetRuntimeException.getCause();
    assertSame(throwable, cause2);
    Throwable[] suppressed = actualInvocationTargetRuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("java.lang.Throwable", actualInvocationTargetRuntimeException.getLocalizedMessage());
    assertEquals("java.lang.Throwable", actualInvocationTargetRuntimeException.getMessage());
    assertSame(cause2, cause.getCause());
    assertSame(cause2, cause.getTargetException());
    assertSame(suppressed, cause.getSuppressed());
    assertEquals("foo", cause.getMessage());
    assertEquals("foo", cause.getLocalizedMessage());
  }
}

