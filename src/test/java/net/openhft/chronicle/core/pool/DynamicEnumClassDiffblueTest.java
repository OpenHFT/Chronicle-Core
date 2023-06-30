package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class DynamicEnumClassDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link DynamicEnumClass#get(String)}
   */
  @Test
  public void testGet() {
    // Arrange, Act and Assert
    assertNull((new DynamicEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).get("foo"));
  }

  /**
   * Method under test: {@link DynamicEnumClass#size()}
   */
  @Test
  public void testSize() {
    // Arrange, Act and Assert
    assertEquals(3, (new DynamicEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).size());
  }

  /**
   * Method under test: {@link DynamicEnumClass#forIndex(int)}
   */
  @Test
  public void testForIndex() {
    // Arrange, Act and Assert
    assertEquals(DynamicEnumClassGptTest.TestEnum.SECOND,
        (new DynamicEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).forIndex(1));
  }

  /**
   * Method under test: {@link DynamicEnumClass#asArray()}
   */
  @Test
  public void testAsArray() {
    // Arrange and Act
    DynamicEnumClassGptTest.TestEnum[] actualAsArrayResult = (new DynamicEnumClass<>(
        DynamicEnumClassGptTest.TestEnum.class)).asArray();

    // Assert
    assertEquals(3, actualAsArrayResult.length);
    assertEquals(DynamicEnumClassGptTest.TestEnum.FIRST, actualAsArrayResult[0]);
    assertEquals(DynamicEnumClassGptTest.TestEnum.SECOND, actualAsArrayResult[1]);
    assertEquals(DynamicEnumClassGptTest.TestEnum.THIRD, actualAsArrayResult[2]);
  }

  /**
   * Method under test: {@link DynamicEnumClass#createMap()}
   */
  @Test
  public void testCreateMap() {
    // Arrange, Act and Assert
    assertTrue((new DynamicEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).createMap().isEmpty());
  }

  /**
   * Method under test: {@link DynamicEnumClass#createSet()}
   */
  @Test
  public void testCreateSet() {
    // Arrange, Act and Assert
    assertTrue((new DynamicEnumClass<>(DynamicEnumClassGptTest.TestEnum.class)).createSet().isEmpty());
  }

  /**
   * Method under test: {@link DynamicEnumClass#reset()}
   */
  @Test
  public void testReset() {
    // Arrange
    DynamicEnumClass<DynamicEnumClassGptTest.TestEnum> dynamicEnumClass = new DynamicEnumClass<>(
        DynamicEnumClassGptTest.TestEnum.class);

    // Act
    dynamicEnumClass.reset();

    // Assert
    assertEquals(3, dynamicEnumClass.size());
  }

  /**
  * Method under test: {@link DynamicEnumClass#DynamicEnumClass(Class)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    DynamicEnumClass<DynamicEnumClassGptTest.TestEnum> actualDynamicEnumClass = new DynamicEnumClass<>(
        DynamicEnumClassGptTest.TestEnum.class);

    // Assert
    assertEquals(3, actualDynamicEnumClass.size());
    assertSame(DynamicEnumClassGptTest.TestEnum.class, actualDynamicEnumClass.type);
  }
}

