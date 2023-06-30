package net.openhft.chronicle.core.internal.pom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class InternalPomPropertiesDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link InternalPomProperties#create(String, String)}
  */
  @Test
  public void testCreate() {
    // Arrange, Act and Assert
    assertTrue(InternalPomProperties.create("42", "42").isEmpty());
  }

  /**
   * Method under test: {@link InternalPomProperties#version(String, String)}
   */
  @Test
  public void testVersion() {
    // Arrange, Act and Assert
    assertEquals("unknown", InternalPomProperties.version("42", "42"));
    assertEquals("unknown", InternalPomProperties.version("", ":"));
  }
}

