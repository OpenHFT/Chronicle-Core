package net.openhft.chronicle.core.internal.cleaner;

import static org.junit.Assert.assertEquals;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.Test;

public class Jdk9ByteBufferCleanerServiceDiffblueTest {
  /**
   * Method under test: {@link Jdk9ByteBufferCleanerService#impact()}
   */
  @Test
  public void testImpact() {
    // Arrange, Act and Assert
    assertEquals(ByteBufferCleanerService.Impact.NO_IMPACT, (new Jdk9ByteBufferCleanerService()).impact());
  }

  /**
  * Method under test: default or parameterless constructor of {@link Jdk9ByteBufferCleanerService}
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertEquals(ByteBufferCleanerService.Impact.NO_IMPACT, (new Jdk9ByteBufferCleanerService()).impact());
  }
}

