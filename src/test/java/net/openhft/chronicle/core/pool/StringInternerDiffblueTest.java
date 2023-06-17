package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class StringInternerDiffblueTest {
  /**
  * Method under test: {@link StringInterner#StringInterner(int)}
  */
  @Test
  public void testConstructor() throws IllegalArgumentException {
    // Arrange and Act
    StringInterner actualStringInterner = new StringInterner(3);

    // Assert
    assertEquals(128, actualStringInterner.capacity());
    assertFalse(actualStringInterner.toggle);
    assertEquals(7, actualStringInterner.shift);
    assertEquals(Float.MAX_EXPONENT, actualStringInterner.mask);
  }

  /**
   * Method under test: {@link StringInterner#StringInterner(int)}
   */
  @Test
  public void testConstructor2() throws IllegalArgumentException {
    // Arrange and Act
    StringInterner actualStringInterner = new StringInterner(128);

    // Assert
    assertEquals(128, actualStringInterner.capacity());
    assertFalse(actualStringInterner.toggle);
    assertEquals(7, actualStringInterner.shift);
    assertEquals(Float.MAX_EXPONENT, actualStringInterner.mask);
  }

  /**
   * Method under test: {@link StringInterner#capacity()}
   */
  @Test
  public void testCapacity() throws IllegalArgumentException {
    // Arrange, Act and Assert
    assertEquals(128, (new StringInterner(3)).capacity());
  }

  /**
   * Method under test: {@link StringInterner#intern(CharSequence)}
   */
  @Test
  public void testIntern() throws IllegalArgumentException {
    // Arrange
    StringInterner stringInterner = new StringInterner(3);
    ClassAliasPool.CAPKey cs = new ClassAliasPool.CAPKey("Name");

    // Act
    String actualInternResult = stringInterner.intern(cs);

    // Assert
    assertSame(cs.value, actualInternResult);
    assertEquals("Name", actualInternResult);
  }

  /**
   * Method under test: {@link StringInterner#index(CharSequence, StringInterner.Changed)}
   */
  @Test
  public void testIndex() throws IllegalArgumentException {
    // Arrange
    StringInterner stringInterner = new StringInterner(3);

    // Act and Assert
    assertEquals(59, stringInterner.index(new ClassAliasPool.CAPKey("Name"), null));
  }

  /**
   * Method under test: {@link StringInterner#index(CharSequence, StringInterner.Changed)}
   */
  @Test
  public void testIndex2() throws IllegalArgumentException {
    // Arrange
    StringInterner stringInterner = new StringInterner(128);

    // Act and Assert
    assertEquals(59, stringInterner.index(new ClassAliasPool.CAPKey("Name"), null));
  }

  /**
   * Method under test: {@link StringInterner#get(int)}
   */
  @Test
  public void testGet() throws IllegalArgumentException {
    // Arrange, Act and Assert
    assertNull((new StringInterner(3)).get(1));
  }

  /**
   * Method under test: {@link StringInterner#toggle()}
   */
  @Test
  public void testToggle() throws IllegalArgumentException {
    // Arrange
    StringInterner stringInterner = new StringInterner(3);

    // Act and Assert
    assertTrue(stringInterner.toggle());
    assertTrue(stringInterner.toggle);
  }

  /**
   * Method under test: {@link StringInterner#valueCount()}
   */
  @Test
  public void testValueCount() throws IllegalArgumentException {
    // Arrange, Act and Assert
    assertEquals(0, (new StringInterner(3)).valueCount());
  }
}

