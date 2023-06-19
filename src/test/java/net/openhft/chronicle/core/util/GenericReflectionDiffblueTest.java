package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import java.lang.reflect.Type;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.mockito.internal.util.reflection.GenericMetadataSupport;

public class GenericReflectionDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link GenericReflection#erase(Type)}
  */
  @Test
  public void testErase() {
    // Arrange, Act and Assert
    assertNull(GenericReflection.erase(null));
  }

  /**
   * Method under test: {@link GenericReflection#getGenericInterfaces(Type)}
   */
  @Test
  public void testGetGenericInterfaces() {
    // Arrange, Act and Assert
    assertThrows(UnsupportedOperationException.class,
        () -> GenericReflection.getGenericInterfaces(new GenericMetadataSupport.TypeVarBoundedType(null)));
    assertEquals(0, GenericReflection.getGenericInterfaces(Object.class).length);
  }

  /**
   * Method under test: {@link GenericReflection#getGenericSuperclass(Type)}
   */
  @Test
  public void testGetGenericSuperclass() {
    // Arrange, Act and Assert
    assertThrows(UnsupportedOperationException.class,
        () -> GenericReflection.getGenericSuperclass(new GenericMetadataSupport.TypeVarBoundedType(null)));
    assertNull(GenericReflection.getGenericSuperclass(Object.class));
  }

  /**
   * Method under test: {@link GenericReflection#getMethodReturnTypes(Type)}
   */
  @Test
  public void testGetMethodReturnTypes() {
    // Arrange, Act and Assert
    assertThrows(UnsupportedOperationException.class,
        () -> GenericReflection.getMethodReturnTypes(new GenericMetadataSupport.TypeVarBoundedType(null)));
    assertEquals(5, GenericReflection.getMethodReturnTypes(Object.class).size());
    assertEquals(33, GenericReflection.getMethodReturnTypes(Class.class).size());
  }
}

