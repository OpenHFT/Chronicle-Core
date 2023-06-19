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
    ClassMetrics classMetrics = new ClassMetrics(2, 3);
    assertEquals(2, classMetrics.offset());
    ClassMetrics classMetrics2 = new ClassMetrics(2, 3);
    assertEquals(classMetrics2, classMetrics);
    assertEquals(classMetrics2.hashCode(), classMetrics.hashCode());
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

