package net.openhft.chronicle.core.internal.analytics;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class ReflectiveBuilderDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link ReflectiveBuilder#ReflectiveBuilder(String, String)}
   */
  @Test
  public void testConstructor() {
    // Arrange and Act
    ReflectiveBuilder actualReflectiveBuilder = new ReflectiveBuilder("42", "Api Secret");

    // Assert
    assertSame(actualReflectiveBuilder, actualReflectiveBuilder.withReportDespiteJUnit());
  }

  /**
  * Method under test: {@link ReflectiveBuilder#build()}
  */
  @Test
  public void testBuild() {
    // Arrange, Act and Assert
    assertTrue((new ReflectiveBuilder("42", "Api Secret")).build() instanceof ReflectiveAnalytics);
  }
}

