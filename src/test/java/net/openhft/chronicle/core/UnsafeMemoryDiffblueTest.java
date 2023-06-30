package net.openhft.chronicle.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UnsafeMemoryDiffblueTest {
  /**
   * Method under test: default or parameterless constructor of {@link UnsafeMemory.ARMMemory}
   */
  @Test
  public void testARMMemoryConstructor() {
    // Arrange, Act and Assert
    assertEquals(0L, (new UnsafeMemory.ARMMemory()).nativeMemoryUsed());
  }

  /**
  * Method under test: default or parameterless constructor of {@link UnsafeMemory}
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertEquals(0L, (new UnsafeMemory()).nativeMemoryUsed());
  }
}

