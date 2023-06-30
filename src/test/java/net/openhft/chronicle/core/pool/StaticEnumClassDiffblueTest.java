package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class StaticEnumClassDiffblueTest {
  /**
   * Method under test: {@link StaticEnumClass#size()}
   */
  @Test
  public void testSize() {
    // Arrange, Act and Assert
    assertEquals(3, (new StaticEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).size());
  }

  /**
   * Method under test: {@link StaticEnumClass#forIndex(int)}
   */
  @Test
  public void testForIndex() {
    // Arrange, Act and Assert
    assertEquals(DynamicEnumClassGptTest.TestEnum.SECOND,
        (new StaticEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).forIndex(1));
  }

  /**
  * Method under test: {@link StaticEnumClass#StaticEnumClass(Class)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    StaticEnumClass<DynamicEnumClassGptTest.TestEnum> actualStaticEnumClass = new StaticEnumClass<>(
        DynamicEnumClassGptTest.TestEnum.class);

    // Assert
    assertEquals(3, actualStaticEnumClass.size());
    assertSame(DynamicEnumClassGptTest.TestEnum.class, actualStaticEnumClass.type);
  }

  /**
   * Method under test: {@link StaticEnumClass#createMap()}
   */
  @Test
  public void testCreateMap() {
    // Arrange, Act and Assert
    assertTrue((new StaticEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).createMap().isEmpty());
  }

  /**
   * Method under test: {@link StaticEnumClass#createSet()}
   */
  @Test
  public void testCreateSet() {
    // Arrange, Act and Assert
    assertTrue((new StaticEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).createSet().isEmpty());
  }
}

