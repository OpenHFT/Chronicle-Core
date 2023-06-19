package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertFalse;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.values.UnsetLongValue;
import org.junit.Test;

public class QueryCloseableDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link QueryCloseable#isClosing()}
  */
  @Test
  public void testIsClosing() {
    // Arrange, Act and Assert
    assertFalse((new UnsetLongValue(42L)).isClosing());
  }
}

