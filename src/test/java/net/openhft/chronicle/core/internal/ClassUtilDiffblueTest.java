package net.openhft.chronicle.core.internal;

import static org.junit.Assert.assertNull;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class ClassUtilDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ClassUtil#getField0(Class, String, boolean)}
  */
  @Test
  public void testGetField0() {
    // Arrange, Act and Assert
    assertNull(ClassUtil.getField0(Object.class, "foo", false));
    assertNull(ClassUtil.getField0(ClassUtil.class, "Name", false));
  }

  /**
   * Method under test: {@link ClassUtil#getMethod0(Class, String, Class[], boolean)}
   */
  @Test
  public void testGetMethod0() {
    // Arrange
    Class<Object> clazz = Object.class;

    // Act and Assert
    assertNull(ClassUtil.getMethod0(clazz, "foo", new Class[]{Object.class}, false));
  }

  /**
   * Method under test: {@link ClassUtil#getMethod0(Class, String, Class[], boolean)}
   */
  @Test
  public void testGetMethod02() {
    // Arrange
    Class<ClassUtil> clazz = ClassUtil.class;

    // Act and Assert
    assertNull(ClassUtil.getMethod0(clazz, "Name", new Class[]{Object.class}, false));
  }
}

