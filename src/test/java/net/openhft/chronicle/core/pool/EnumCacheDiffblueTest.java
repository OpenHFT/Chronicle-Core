package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EnumCacheDiffblueTest {
  /**
   * Method under test: {@link EnumCache#of(Class)}
   */
  @Test
  public void testOf() {
    // Arrange, Act and Assert
    assertTrue(EnumCache.of(Object.class) instanceof StaticEnumClass);
  }

  /**
   * Method under test: {@link EnumCache#get(String)}
   */
  @Test
  public void testGet() {
    // Arrange, Act and Assert
    assertNull((new DynamicEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).get("foo"));
  }

  /**
  * Method under test: {@link EnumCache#type()}
  */
  @Test
  public void testType() {
    // Arrange and Act
    Class<?> actualTypeResult = (new DynamicEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).type();

    // Assert
    assertSame(DynamicEnumClassGptTest.TestEnum.class, actualTypeResult);
  }
}

