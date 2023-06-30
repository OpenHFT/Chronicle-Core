package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class MisAlignedAssertionErrorDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: default or parameterless constructor of {@link MisAlignedAssertionError}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    MisAlignedAssertionError actualMisAlignedAssertionError = new MisAlignedAssertionError();

    // Assert
    assertNull(actualMisAlignedAssertionError.getCause());
    assertEquals(0, actualMisAlignedAssertionError.getSuppressed().length);
    assertNull(actualMisAlignedAssertionError.getMessage());
    assertNull(actualMisAlignedAssertionError.getLocalizedMessage());
  }
}

