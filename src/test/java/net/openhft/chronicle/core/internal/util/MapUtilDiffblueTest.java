package net.openhft.chronicle.core.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.AbstractMap;
import java.util.Map;
import org.junit.Test;

public class MapUtilDiffblueTest {
  /**
  * Method under test: {@link MapUtil#ofUnmodifiable(Map.Entry[])}
  */
  @Test
  public void testOfUnmodifiable() {
    // Arrange, Act and Assert
    assertEquals(1, MapUtil.ofUnmodifiable(new AbstractMap.SimpleEntry<>("42", "42")).size());
    assertTrue(MapUtil.ofUnmodifiable().isEmpty());
  }
}

