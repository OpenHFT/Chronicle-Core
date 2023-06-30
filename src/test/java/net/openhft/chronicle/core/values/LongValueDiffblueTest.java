package net.openhft.chronicle.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class LongValueDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link LongValue#getVolatileValue(long)}
  */
  @Test
  public void testGetVolatileValue() throws IllegalStateException {
    // Arrange, Act and Assert
    assertEquals(42L, (new UnsetLongValue(42L)).getVolatileValue(42L));
    assertEquals(1L, (new UnsetLongValue(42L)).getVolatileValue(1L));
    assertEquals(42L, (new UnsetLongValue(42L)).getVolatileValue());
  }

  /**
   * Method under test: {@link LongValue#addAtomicValue(long)}
   */
  @Test
  public void testAddAtomicValue() throws IllegalStateException {
    // Arrange, Act and Assert
    assertEquals(42L, (new UnsetLongValue(42L)).addAtomicValue(2L));
  }

  /**
   * Method under test: {@link LongValue#isClosed()}
   */
  @Test
  public void testIsClosed() {
    // Arrange, Act and Assert
    assertFalse((new UnsetLongValue(42L)).isClosed());
  }
}

