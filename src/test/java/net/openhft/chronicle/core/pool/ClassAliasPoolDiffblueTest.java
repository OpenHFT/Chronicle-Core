package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javax.management.loading.MLet;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.input.DefaultCharAppender;
import org.mockito.Mockito;

public class ClassAliasPoolDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link ClassAliasPool.CAPKey#charAt(int)}
   */
  @Test
  public void testCAPKeyCharAt() throws IndexOutOfBoundsException {
    // Arrange, Act and Assert
    assertEquals('a', (new ClassAliasPool.CAPKey("Name")).charAt(1));
  }

  /**
   * Methods under test: 
   * 
   * <ul>
   *   <li>{@link ClassAliasPool.CAPKey#CAPKey(String)}
   *   <li>{@link ClassAliasPool.CAPKey#toString()}
   * </ul>
   */
  @Test
  public void testCAPKeyConstructor() {
    // Arrange and Act
    ClassAliasPool.CAPKey actualCapKey = new ClassAliasPool.CAPKey("Name");

    // Assert
    assertSame(actualCapKey.value, actualCapKey.toString());
  }

  /**
   * Method under test: {@link ClassAliasPool.CAPKey#equals(Object)}
   */
  @Test
  public void testCAPKeyEquals() {
    // Arrange, Act and Assert
    assertNotEquals(new ClassAliasPool.CAPKey("Name"), null);
    assertNotEquals(new ClassAliasPool.CAPKey("Name"), "Different type to CAPKey");
    assertNotEquals(new ClassAliasPool.CAPKey("Name"), mock(DefaultCharAppender.class));
  }

  /**
   * Methods under test: 
   * 
   * <ul>
   *   <li>{@link ClassAliasPool.CAPKey#equals(Object)}
   *   <li>{@link ClassAliasPool.CAPKey#hashCode()}
   * </ul>
   */
  @Test
  public void testCAPKeyEquals2() {
    // Arrange
    ClassAliasPool.CAPKey capKey = new ClassAliasPool.CAPKey("Name");

    // Act and Assert
    assertEquals(capKey, capKey);
    int expectedHashCodeResult = capKey.hashCode();
    assertEquals(expectedHashCodeResult, capKey.hashCode());
  }

  /**
   * Methods under test: 
   * 
   * <ul>
   *   <li>{@link ClassAliasPool.CAPKey#equals(Object)}
   *   <li>{@link ClassAliasPool.CAPKey#hashCode()}
   * </ul>
   */
  @Test
  public void testCAPKeyEquals3() {
    // Arrange
    ClassAliasPool.CAPKey capKey = new ClassAliasPool.CAPKey("Name");
    ClassAliasPool.CAPKey capKey2 = new ClassAliasPool.CAPKey("Name");

    // Act and Assert
    assertEquals(capKey, capKey2);
    int expectedHashCodeResult = capKey.hashCode();
    assertEquals(expectedHashCodeResult, capKey2.hashCode());
  }

  /**
   * Method under test: {@link ClassAliasPool.CAPKey#equals(Object)}
   */
  @Test
  public void testCAPKeyEquals4() {
    // Arrange
    ClassAliasPool.CAPKey capKey = new ClassAliasPool.CAPKey("java.lang.CharSequence");

    // Act and Assert
    assertNotEquals(capKey, new ClassAliasPool.CAPKey("Name"));
  }

  /**
   * Methods under test: 
   * 
   * <ul>
   *   <li>{@link ClassAliasPool.CAPKey#equals(Object)}
   *   <li>{@link ClassAliasPool.CAPKey#hashCode()}
   * </ul>
   */
  @Test
  public void testCAPKeyEquals5() {
    // Arrange
    ClassAliasPool.CAPKey capKey = new ClassAliasPool.CAPKey("java.lang.CharSequence");

    // Act and Assert
    assertEquals(capKey, "java.lang.CharSequence");
    int expectedHashCodeResult = capKey.hashCode();
    assertEquals(expectedHashCodeResult, "java.lang.CharSequence".hashCode());
  }

  /**
   * Method under test: {@link ClassAliasPool.CAPKey#length()}
   */
  @Test
  public void testCAPKeyLength() {
    // Arrange, Act and Assert
    assertEquals(4, (new ClassAliasPool.CAPKey("Name")).length());
  }

  /**
   * Method under test: {@link ClassAliasPool.CAPKey#subSequence(int, int)}
   */
  @Test
  public void testCAPKeySubSequence() {
    // Arrange, Act and Assert
    assertThrows(UnsupportedOperationException.class, () -> (new ClassAliasPool.CAPKey("Name")).subSequence(1, 3));
  }

  /**
   * Method under test: {@link ClassAliasPool#testPackage(String, Class)}
   */
  @Test
  public void testTestPackage() {
    // Arrange, Act and Assert
    assertFalse(ClassAliasPool.testPackage("Pkg Name", Object.class));
    assertTrue(ClassAliasPool.testPackage("", Object.class));
  }

  /**
  * Method under test: {@link ClassAliasPool#nameFor(Class)}
  */
  @Test
  public void testNameFor() throws IllegalArgumentException {
    // Arrange
    ClassLookup parent = mock(ClassLookup.class);
    when(parent.nameFor(Mockito.<Class<Object>>any())).thenReturn("Name For");
    ClassAliasPool parent2 = new ClassAliasPool(parent);
    ClassAliasPool classAliasPool = new ClassAliasPool(parent2, new MLet());

    // Act and Assert
    assertEquals("Name For", classAliasPool.nameFor(Object.class));
    verify(parent).nameFor(Mockito.<Class<Object>>any());
  }

  /**
   * Method under test: {@link ClassAliasPool#nameFor(Class)}
   */
  @Test
  public void testNameFor2() throws IllegalArgumentException {
    // Arrange
    ClassAliasPool classAliasPool = new ClassAliasPool(null, new MLet());

    // Act and Assert
    assertEquals("java.lang.Object", classAliasPool.nameFor(Object.class));
  }

  /**
   * Method under test: {@link ClassAliasPool#nameFor(Class)}
   */
  @Test
  public void testNameFor3() throws IllegalArgumentException {
    // Arrange
    ClassLookup parent = mock(ClassLookup.class);
    when(parent.nameFor(Mockito.<Class<Object>>any()))
        .thenThrow(new ClassNotFoundRuntimeException(new ClassNotFoundException()));
    ClassAliasPool parent2 = new ClassAliasPool(parent);
    ClassAliasPool parent3 = new ClassAliasPool(parent2, new MLet());

    ClassAliasPool classAliasPool = new ClassAliasPool(parent3, new MLet());

    // Act and Assert
    assertThrows(ClassNotFoundRuntimeException.class, () -> classAliasPool.nameFor(Object.class));
    verify(parent).nameFor(Mockito.<Class<Object>>any());
  }

  /**
   * Method under test: {@link ClassAliasPool#nameFor(Class)}
   */
  @Test
  public void testNameFor4() throws IllegalArgumentException {
    // Arrange
    ClassLookup parent = mock(ClassLookup.class);
    when(parent.nameFor(Mockito.<Class<Object>>any()))
        .thenThrow(new ClassNotFoundRuntimeException(new ClassNotFoundException()));

    ClassAliasPool parent2 = new ClassAliasPool(parent);
    parent2.addAlias(Object.class);
    ClassAliasPool parent3 = new ClassAliasPool(parent2, new MLet());

    ClassAliasPool classAliasPool = new ClassAliasPool(parent3, new MLet());

    // Act and Assert
    assertEquals("Object", classAliasPool.nameFor(Object.class));
  }
}

