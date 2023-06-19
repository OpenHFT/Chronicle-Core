package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class EnumCacheDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link EnumCache#of(Class)}
  */
  @Test
  public void testOf() {
    // Arrange, Act and Assert
    assertTrue(EnumCache.of(Object.class) instanceof StaticEnumClass);
  }
}

