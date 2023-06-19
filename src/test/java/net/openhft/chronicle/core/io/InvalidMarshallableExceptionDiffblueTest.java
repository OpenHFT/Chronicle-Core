package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class InvalidMarshallableExceptionDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link InvalidMarshallableException#InvalidMarshallableException(String)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    InvalidMarshallableException actualInvalidMarshallableException = new InvalidMarshallableException("Msg");

    // Assert
    assertNull(actualInvalidMarshallableException.getCause());
    assertEquals(0, actualInvalidMarshallableException.getSuppressed().length);
    assertEquals("Msg", actualInvalidMarshallableException.getMessage());
    assertEquals("Msg", actualInvalidMarshallableException.getLocalizedMessage());
  }
}

