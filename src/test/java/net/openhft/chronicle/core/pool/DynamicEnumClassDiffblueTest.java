package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.List;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class DynamicEnumClassDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link DynamicEnumClass#get(String)}
   */
  @Test
  public void testGet() {
    // Arrange, Act and Assert
    assertNull((new DynamicEnumClass<>(YesNo.class)).get("foo"));
  }

  /**
   * Method under test: {@link DynamicEnumClass#size()}
   */
  @Test
  public void testSize() {
    // Arrange, Act and Assert
    assertEquals(2, (new DynamicEnumClass<>(YesNo.class)).size());
  }

  /**
   * Method under test: {@link DynamicEnumClass#forIndex(int)}
   */
  @Test
  public void testForIndex() {
    // Arrange, Act and Assert
    assertEquals(YesNo.No, (new DynamicEnumClass<>(YesNo.class)).forIndex(1));
  }

  /**
   * Method under test: {@link DynamicEnumClass#asArray()}
   */
  @Test
  public void testAsArray() {
    // Arrange
    DynamicEnumClass<YesNo> dynamicEnumClass = new DynamicEnumClass<>(YesNo.class);

    // Act
    YesNo[] actualAsArrayResult = dynamicEnumClass.asArray();

    // Assert
    assertSame(dynamicEnumClass.values, actualAsArrayResult);
    assertEquals(2, actualAsArrayResult.length);
    assertEquals(YesNo.Yes, actualAsArrayResult[0]);
    assertEquals(YesNo.No, actualAsArrayResult[1]);
    assertSame(actualAsArrayResult, dynamicEnumClass.values);
  }

  /**
   * Method under test: {@link DynamicEnumClass#createMap()}
   */
  @Test
  public void testCreateMap() {
    // Arrange, Act and Assert
    assertTrue((new DynamicEnumClass<>(YesNo.class)).createMap().isEmpty());
  }

  /**
   * Method under test: {@link DynamicEnumClass#createSet()}
   */
  @Test
  public void testCreateSet() {
    // Arrange, Act and Assert
    assertTrue((new DynamicEnumClass<>(YesNo.class)).createSet().isEmpty());
  }

  /**
   * Method under test: {@link DynamicEnumClass#reset()}
   */
  @Test
  public void testReset() {
    // Arrange
    DynamicEnumClass<YesNo> dynamicEnumClass = new DynamicEnumClass<>(YesNo.class);

    // Act
    dynamicEnumClass.reset();

    // Assert
    assertEquals(2, dynamicEnumClass.size());
    assertNull(dynamicEnumClass.values);
    List<YesNo> yesNoList = dynamicEnumClass.eList;
    assertEquals(2, yesNoList.size());
    assertEquals(YesNo.Yes, yesNoList.get(0));
    assertEquals(YesNo.No, yesNoList.get(1));
  }

  /**
  * Method under test: {@link DynamicEnumClass#DynamicEnumClass(Class)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    DynamicEnumClass<YesNo> actualDynamicEnumClass = new DynamicEnumClass<>(YesNo.class);

    // Assert
    assertEquals(2, actualDynamicEnumClass.size());
    assertNull(actualDynamicEnumClass.values);
    assertSame(YesNo.class, actualDynamicEnumClass.type);
    List<YesNo> yesNoList = actualDynamicEnumClass.eList;
    assertEquals(2, yesNoList.size());
    assertEquals(YesNo.Yes, yesNoList.get(0));
    assertEquals(YesNo.No, yesNoList.get(1));
  }
}

