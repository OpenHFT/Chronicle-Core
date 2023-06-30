package net.openhft.chronicle.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ClassMetricsDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link ClassMetrics#offset()}
   */
  @Test
  public void testOffset() {
    // Arrange, Act and Assert
    assertEquals(2, (new ClassMetrics(2, 3)).offset());
  }

  /**
  * Method under test: {@link ClassMetrics#length()}
  */
  @Test
  public void testLength() {
    // Arrange, Act and Assert
    assertEquals(3, (new ClassMetrics(2, 3)).length());
  }
}

