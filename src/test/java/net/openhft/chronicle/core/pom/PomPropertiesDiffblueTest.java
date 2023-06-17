package net.openhft.chronicle.core.pom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PomPropertiesDiffblueTest {
  /**
  * Method under test: {@link PomProperties#create(String, String)}
  */
  @Test
  public void testCreate() {
    // Arrange, Act and Assert
    assertTrue(PomProperties.create("42", "42").isEmpty());
  }

  /**
   * Method under test: {@link PomProperties#version(String, String)}
   */
  @Test
  public void testVersion() {
    // Arrange, Act and Assert
    assertEquals("unknown", PomProperties.version("42", "42"));
    assertEquals("unknown", PomProperties.version("", ":"));
  }
}

