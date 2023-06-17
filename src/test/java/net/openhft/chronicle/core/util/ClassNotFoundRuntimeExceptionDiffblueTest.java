package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ClassNotFoundRuntimeExceptionDiffblueTest {
  /**
  * Method under test: {@link ClassNotFoundRuntimeException#ClassNotFoundRuntimeException(ClassNotFoundException)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    ClassNotFoundException cause = new ClassNotFoundException();

    // Act
    ClassNotFoundRuntimeException actualClassNotFoundRuntimeException = new ClassNotFoundRuntimeException(cause);

    // Assert
    ClassNotFoundException cause2 = actualClassNotFoundRuntimeException.getCause();
    assertSame(cause, cause2);
    Throwable[] suppressed = actualClassNotFoundRuntimeException.getSuppressed();
    assertEquals(0, suppressed.length);
    assertEquals("java.lang.ClassNotFoundException", actualClassNotFoundRuntimeException.getLocalizedMessage());
    assertEquals("java.lang.ClassNotFoundException", actualClassNotFoundRuntimeException.getMessage());
    assertNull(cause2.getLocalizedMessage());
    assertNull(cause2.getException());
    assertNull(cause2.getCause());
    assertNull(cause2.getMessage());
    assertSame(suppressed, cause2.getSuppressed());
  }

  /**
   * Method under test: {@link ClassNotFoundRuntimeException#getCause()}
   */
  @Test
  public void testGetCause() {
    // Arrange
    ClassNotFoundException cause = new ClassNotFoundException();

    // Act and Assert
    assertSame(cause, (new ClassNotFoundRuntimeException(cause)).getCause());
  }
}

