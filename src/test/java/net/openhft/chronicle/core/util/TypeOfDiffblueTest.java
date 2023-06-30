package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertThrows;
import org.junit.Test;

public class TypeOfDiffblueTest {
  /**
  * Method under test: default or parameterless constructor of {@link TypeOf}
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertThrows(RuntimeException.class, () -> new TypeOf<>());
  }
}

