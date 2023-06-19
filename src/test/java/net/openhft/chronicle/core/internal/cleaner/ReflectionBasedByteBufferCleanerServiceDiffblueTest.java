package net.openhft.chronicle.core.internal.cleaner;

import static org.junit.Assert.assertEquals;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.Test;

public class ReflectionBasedByteBufferCleanerServiceDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link ReflectionBasedByteBufferCleanerService#impact()}
   */
  @Test
  public void testImpact() {
    // Arrange, Act and Assert
    assertEquals(ByteBufferCleanerService.Impact.SOME_IMPACT, (new ReflectionBasedByteBufferCleanerService()).impact());
  }

  /**
  * Method under test: default or parameterless constructor of {@link ReflectionBasedByteBufferCleanerService}
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertEquals(ByteBufferCleanerService.Impact.SOME_IMPACT, (new ReflectionBasedByteBufferCleanerService()).impact());
  }
}

