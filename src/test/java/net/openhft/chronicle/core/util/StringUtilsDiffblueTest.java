package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import java.io.UnsupportedEncodingException;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class StringUtilsDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link StringUtils#endsWith(CharSequence, String)}
  */
  @Test
  public void testEndsWith() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.endsWith(System.lineSeparator(), "Ends With"));
    assertTrue(StringUtils.endsWith(new StringBuilder(1), ""));
    assertTrue(StringUtils.endsWith("\"", "\""));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual() {
    // Arrange
    String s = System.lineSeparator();

    // Act and Assert
    assertTrue(StringUtils.isEqual(s, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual2() {
    // Arrange
    StringBuilder s = new StringBuilder(1);

    // Act and Assert
    assertFalse(StringUtils.isEqual((CharSequence) s, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual3() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.isEqual((CharSequence) null, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual4() {
    // Arrange
    String s = new String();

    // Act and Assert
    assertFalse(StringUtils.isEqual(s, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual5() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.isEqual(System.lineSeparator(), null));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual6() {
    // Arrange
    StringBuilder s = new StringBuilder(1);

    // Act and Assert
    assertTrue(StringUtils.isEqual((CharSequence) s, new StringBuilder(1)));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual7() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.isEqual((CharSequence) new StringBuilder(1), null));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual8() {
    // Arrange
    String s = new String();

    // Act and Assert
    assertTrue(StringUtils.isEqual(s, new StringBuilder(2)));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual9() {
    // Arrange
    StringBuilder s = new StringBuilder("42");

    // Act and Assert
    assertFalse(StringUtils.isEqual((CharSequence) s, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual10() {
    // Arrange
    StringBuilder s = new StringBuilder("foo");

    // Act and Assert
    assertTrue(StringUtils.isEqual((CharSequence) s, new StringBuilder("foo")));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual11() {
    // Arrange
    String s = Integer.toString(-1);

    // Act and Assert
    assertFalse(StringUtils.isEqual(s, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(CharSequence, CharSequence)}
   */
  @Test
  public void testIsEqual12() {
    // Arrange
    String s = Integer.toString(1);

    // Act and Assert
    assertTrue(StringUtils.isEqual(s, Integer.toString(1)));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(StringBuilder, CharSequence)}
   */
  @Test
  public void testIsEqual13() {
    // Arrange
    StringBuilder s = new StringBuilder(1);

    // Act and Assert
    assertFalse(StringUtils.isEqual(s, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(StringBuilder, CharSequence)}
   */
  @Test
  public void testIsEqual14() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.isEqual((StringBuilder) null, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(StringBuilder, CharSequence)}
   */
  @Test
  public void testIsEqual15() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.isEqual(new StringBuilder(1), null));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(StringBuilder, CharSequence)}
   */
  @Test
  public void testIsEqual16() {
    // Arrange
    StringBuilder s = new StringBuilder(1);

    // Act and Assert
    assertTrue(StringUtils.isEqual(s, new StringBuilder(2)));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(StringBuilder, CharSequence)}
   */
  @Test
  public void testIsEqual17() {
    // Arrange, Act and Assert
    assertTrue(StringUtils.isEqual((StringBuilder) null, null));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(StringBuilder, CharSequence)}
   */
  @Test
  public void testIsEqual18() {
    // Arrange
    StringBuilder s = new StringBuilder("42");

    // Act and Assert
    assertFalse(StringUtils.isEqual(s, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#isEqual(StringBuilder, CharSequence)}
   */
  @Test
  public void testIsEqual19() {
    // Arrange
    StringBuilder s = new StringBuilder("foo");

    // Act and Assert
    assertTrue(StringUtils.isEqual(s, new StringBuilder("foo")));
  }

  /**
   * Method under test: {@link StringUtils#equalsCaseIgnore(CharSequence, CharSequence)}
   */
  @Test
  public void testEqualsCaseIgnore() {
    // Arrange
    String s = System.lineSeparator();

    // Act and Assert
    assertTrue(StringUtils.equalsCaseIgnore(s, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#equalsCaseIgnore(CharSequence, CharSequence)}
   */
  @Test
  public void testEqualsCaseIgnore2() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.equalsCaseIgnore("foo", System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#equalsCaseIgnore(CharSequence, CharSequence)}
   */
  @Test
  public void testEqualsCaseIgnore3() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.equalsCaseIgnore(null, System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#equalsCaseIgnore(CharSequence, CharSequence)}
   */
  @Test
  public void testEqualsCaseIgnore4() {
    // Arrange, Act and Assert
    assertFalse(StringUtils.equalsCaseIgnore("42", System.lineSeparator()));
  }

  /**
   * Method under test: {@link StringUtils#extractChars(String)}
   */
  @Test
  public void testExtractChars() {
    // Arrange and Act
    char[] actualExtractCharsResult = StringUtils.extractChars("foo");

    // Assert
    assertEquals(3, actualExtractCharsResult.length);
    assertEquals('f', actualExtractCharsResult[0]);
    assertEquals('o', actualExtractCharsResult[1]);
    assertEquals('o', actualExtractCharsResult[2]);
  }

  /**
   * Method under test: {@link StringUtils#extractChars(StringBuilder)}
   */
  @Test
  public void testExtractChars2() {
    // Arrange and Act
    char[] actualExtractCharsResult = StringUtils.extractChars(new StringBuilder(1));

    // Assert
    assertEquals(1, actualExtractCharsResult.length);
    assertEquals('\u0000', actualExtractCharsResult[0]);
  }

  /**
   * Method under test: {@link StringUtils#extractBytes(String)}
   */
  @Test
  public void testExtractBytes() {
    // Arrange, Act and Assert
    assertThrows(UnsupportedOperationException.class, () -> StringUtils.extractBytes("foo"));
    assertThrows(UnsupportedOperationException.class, () -> StringUtils.extractBytes(new StringBuilder(1)));
  }

  /**
   * Method under test: {@link StringUtils#newString(char[])}
   */
  @Test
  public void testNewString() {
    // Arrange, Act and Assert
    assertEquals("A A ", StringUtils.newString("A A ".toCharArray()));
  }

  /**
   * Method under test: {@link StringUtils#newStringFromBytes(byte[])}
   */
  @Test
  public void testNewStringFromBytes() throws UnsupportedEncodingException {
    // Arrange, Act and Assert
    assertThrows(UnsupportedOperationException.class,
        () -> StringUtils.newStringFromBytes("AXAXAXAX".getBytes("UTF-8")));
  }

  /**
   * Method under test: {@link StringUtils#firstLowerCase(String)}
   */
  @Test
  public void testFirstLowerCase() {
    // Arrange, Act and Assert
    assertEquals("str", StringUtils.firstLowerCase("Str"));
    assertNull(StringUtils.firstLowerCase(null));
    assertEquals("\"", StringUtils.firstLowerCase("\""));
    assertEquals("", StringUtils.firstLowerCase(""));
  }

  /**
   * Method under test: {@link StringUtils#parseDouble(CharSequence)}
   */
  @Test
  public void testParseDouble() {
    // Arrange, Act and Assert
    assertEquals(0.0d, StringUtils.parseDouble(System.lineSeparator()), 0.0);
    assertEquals(Double.NaN, StringUtils.parseDouble("In"), 0.0);
    assertEquals(42.0d, StringUtils.parseDouble("42"), 0.0);
    assertEquals(Double.POSITIVE_INFINITY, StringUtils.parseDouble("Infinity"), 0.0);
    assertEquals(0.0d, StringUtils.parseDouble(new StringBuilder("foo")), 0.0);
  }

  /**
   * Method under test: {@link StringUtils#parseInt(CharSequence, int)}
   */
  @Test
  public void testParseInt() throws NumberFormatException {
    // Arrange, Act and Assert
    assertThrows(NumberFormatException.class, () -> StringUtils.parseInt(System.lineSeparator(), 1));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseInt(null, 1));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseInt(System.lineSeparator(), 2));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseInt("For input string: \"", 2));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseInt(new StringBuilder(2), 2));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseInt("For input string: \"", 36));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseInt("For input string: \"", 48));
    assertEquals(20328, StringUtils.parseInt("foo", 36));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseInt("Infinity", 36));
  }

  /**
   * Method under test: {@link StringUtils#parseLong(CharSequence, int)}
   */
  @Test
  public void testParseLong() throws NumberFormatException {
    // Arrange, Act and Assert
    assertThrows(NumberFormatException.class, () -> StringUtils.parseLong(System.lineSeparator(), 1));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseLong(null, 1));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseLong(System.lineSeparator(), 2));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseLong("For input string: \"", 2));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseLong(new StringBuilder(2), 2));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseLong("For input string: \"", 36));
    assertThrows(NumberFormatException.class, () -> StringUtils.parseLong("For input string: \"", 48));
    assertEquals(20328L, StringUtils.parseLong("foo", 36));
  }
}

