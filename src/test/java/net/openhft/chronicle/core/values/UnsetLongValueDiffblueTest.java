package net.openhft.chronicle.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UnsetLongValueDiffblueTest {
  /**
  * Methods under test: 
  * 
  * <ul>
  *   <li>{@link UnsetLongValue#UnsetLongValue(long)}
  *   <li>{@link UnsetLongValue#setValue(long)}
  *   <li>{@link UnsetLongValue#getValue()}
  * </ul>
  */
  @Test
  public void testConstructor() throws IllegalStateException {
    // Arrange and Act
    UnsetLongValue actualUnsetLongValue = new UnsetLongValue(42L);
    actualUnsetLongValue.setValue(42L);

    // Assert that nothing has changed
    assertEquals(42L, actualUnsetLongValue.getValue());
  }

  /**
   * Method under test: {@link UnsetLongValue#getVolatileValue(long)}
   */
  @Test
  public void testGetVolatileValue() throws IllegalStateException {
    // Arrange, Act and Assert
    assertEquals(42L, (new UnsetLongValue(42L)).getVolatileValue(42L));
  }

  /**
   * Method under test: {@link UnsetLongValue#addValue(long)}
   */
  @Test
  public void testAddValue() throws IllegalStateException {
    // Arrange, Act and Assert
    assertEquals(42L, (new UnsetLongValue(42L)).addValue(2L));
  }

  /**
   * Method under test: {@link UnsetLongValue#compareAndSwapValue(long, long)}
   */
  @Test
  public void testCompareAndSwapValue() throws IllegalStateException {
    // Arrange, Act and Assert
    assertTrue((new UnsetLongValue(42L)).compareAndSwapValue(1L, 42L));
  }
}

