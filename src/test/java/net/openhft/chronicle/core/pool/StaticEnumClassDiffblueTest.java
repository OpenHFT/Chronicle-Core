package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class StaticEnumClassDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link StaticEnumClass#size()}
   */
  @Test
  public void testSize() {
    // Arrange, Act and Assert
    assertEquals(2, (new StaticEnumClass<>(YesNo.class)).size());
  }

  /**
   * Method under test: {@link StaticEnumClass#forIndex(int)}
   */
  @Test
  public void testForIndex() {
    // Arrange, Act and Assert
    assertEquals(YesNo.No, (new StaticEnumClass<>(YesNo.class)).forIndex(1));
  }

  /**
  * Method under test: {@link StaticEnumClass#StaticEnumClass(Class)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    StaticEnumClass<YesNo> actualStaticEnumClass = new StaticEnumClass<>(YesNo.class);

    // Assert
    assertEquals(2, actualStaticEnumClass.size());
    assertSame(YesNo.class, actualStaticEnumClass.type);
  }

  /**
   * Method under test: {@link StaticEnumClass#createMap()}
   */
  @Test
  public void testCreateMap() {
    // Arrange, Act and Assert
    assertTrue((new StaticEnumClass<>(YesNo.class)).createMap().isEmpty());
  }

  /**
   * Method under test: {@link StaticEnumClass#createSet()}
   */
  @Test
  public void testCreateSet() {
    // Arrange, Act and Assert
    assertTrue((new StaticEnumClass<>(YesNo.class)).createSet().isEmpty());
  }
}

