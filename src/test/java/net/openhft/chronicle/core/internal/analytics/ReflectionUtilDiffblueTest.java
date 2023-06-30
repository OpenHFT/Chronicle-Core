package net.openhft.chronicle.core.internal.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import net.openhft.chronicle.analytics.internal.VanillaAnalyticsBuilder;
import org.junit.Test;

public class ReflectionUtilDiffblueTest {
  /**
   * Method under test: {@link ReflectionUtil#analyticsPresent()}
   */
  @Test
  public void testAnalyticsPresent() {
    // Arrange, Act and Assert
    assertTrue(ReflectionUtil.analyticsPresent());
  }

  /**
  * Method under test: {@link ReflectionUtil#analyticsBuilder(String, String)}
  */
  @Test
  public void testAnalyticsBuilder() {
    // Arrange, Act and Assert
    assertEquals("Api Secret",
        ((VanillaAnalyticsBuilder) ReflectionUtil.analyticsBuilder("42", "Api Secret")).apiSecret());
    assertTrue(
        ((VanillaAnalyticsBuilder) ReflectionUtil.analyticsBuilder("42", "Api Secret")).userProperties().isEmpty());
    assertEquals("https://www.google-analytics.com/mp/collect",
        ((VanillaAnalyticsBuilder) ReflectionUtil.analyticsBuilder("42", "Api Secret")).url());
    assertEquals(TimeUnit.SECONDS,
        ((VanillaAnalyticsBuilder) ReflectionUtil.analyticsBuilder("42", "Api Secret")).timeUnit());
    assertEquals(0, ((VanillaAnalyticsBuilder) ReflectionUtil.analyticsBuilder("42", "Api Secret")).messages());
    assertEquals("42", ((VanillaAnalyticsBuilder) ReflectionUtil.analyticsBuilder("42", "Api Secret")).measurementId());
    assertTrue(
        ((VanillaAnalyticsBuilder) ReflectionUtil.analyticsBuilder("42", "Api Secret")).eventParameters().isEmpty());
    String expectedClientIdFileNameResult = String.join("", System.getProperty("user.home"),
        "/.chronicle.analytics.client.id");
    assertEquals(expectedClientIdFileNameResult,
        ((VanillaAnalyticsBuilder) ReflectionUtil.analyticsBuilder("42", "Api Secret")).clientIdFileName());
  }
}

