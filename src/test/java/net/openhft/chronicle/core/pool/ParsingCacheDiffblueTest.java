package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.Function;
import org.junit.Test;
import org.mockito.Mockito;

public class ParsingCacheDiffblueTest {
  /**
  * Method under test: {@link ParsingCache#ParsingCache(int, Function)}
  */
  @Test
  public void testConstructor() throws IllegalArgumentException {
    // Arrange and Act
    ParsingCache<Object> actualParsingCache = new ParsingCache<>(3, mock(Function.class));

    // Assert
    assertEquals(128, actualParsingCache.interner.length);
    assertFalse(actualParsingCache.toggle);
    assertEquals(7, actualParsingCache.shift);
    assertEquals(Float.MAX_EXPONENT, actualParsingCache.mask);
  }

  /**
   * Method under test: {@link ParsingCache#intern(CharSequence)}
   */
  @Test
  public void testIntern() throws IllegalArgumentException {
    // Arrange
    Function<String, Object> eFunction = mock(Function.class);
    when(eFunction.apply(Mockito.<String>any())).thenReturn("Apply");
    ParsingCache<Object> parsingCache = new ParsingCache<>(3, eFunction);

    // Act and Assert
    assertEquals("Apply", parsingCache.intern(new ClassAliasPool.CAPKey("Name")));
    verify(eFunction).apply(Mockito.<String>any());
  }

  /**
   * Method under test: {@link ParsingCache#intern(CharSequence)}
   */
  @Test
  public void testIntern2() throws IllegalArgumentException {
    // Arrange
    Function<String, Object> eFunction = mock(Function.class);
    when(eFunction.apply(Mockito.<String>any())).thenReturn("Apply");
    ParsingCache<Object> parsingCache = new ParsingCache<>(3, eFunction);

    // Act and Assert
    assertEquals("Apply", parsingCache.intern(new ClassAliasPool.CAPKey("")));
    verify(eFunction).apply(Mockito.<String>any());
  }

  /**
   * Method under test: {@link ParsingCache#intern(CharSequence)}
   */
  @Test
  public void testIntern3() throws IllegalArgumentException {
    // Arrange
    Function<String, Object> eFunction = mock(Function.class);
    when(eFunction.apply(Mockito.<String>any())).thenReturn("Apply");

    // Act and Assert
    assertNull((new ParsingCache<>(3, eFunction)).intern(null));
  }

  /**
   * Method under test: {@link ParsingCache#intern(CharSequence)}
   */
  @Test
  public void testIntern4() throws IllegalArgumentException {
    // Arrange
    Function<String, Object> eFunction = mock(Function.class);
    when(eFunction.apply(Mockito.<String>any())).thenReturn("Apply");

    // Act and Assert
    assertEquals("Apply",
        (new ParsingCache<>(3, eFunction)).intern("net.openhft.chronicle.core.pool.ParsingCache$ParsedData[]"));
    verify(eFunction).apply(Mockito.<String>any());
  }

  /**
   * Method under test: {@link ParsingCache#intern(CharSequence)}
   */
  @Test
  public void testIntern5() throws IllegalArgumentException {
    // Arrange
    Function<String, Object> eFunction = mock(Function.class);
    when(eFunction.apply(Mockito.<String>any())).thenThrow(new IllegalArgumentException("foo"));
    ParsingCache<Object> parsingCache = new ParsingCache<>(3, eFunction);

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> parsingCache.intern(new ClassAliasPool.CAPKey("Name")));
    verify(eFunction).apply(Mockito.<String>any());
  }

  /**
   * Method under test: {@link ParsingCache.ParsedData#ParsedData(String, Object)}
   */
  @Test
  public void testParsedDataConstructor() {
    // Arrange and Act
    ParsingCache.ParsedData<Object> actualParsedData = new ParsingCache.ParsedData<>("String", "42");

    // Assert
    assertEquals("42", actualParsedData.e);
    assertEquals("String", actualParsedData.string);
  }

  /**
   * Method under test: {@link ParsingCache#toggle()}
   */
  @Test
  public void testToggle() throws IllegalArgumentException {
    // Arrange
    ParsingCache<Object> parsingCache = new ParsingCache<>(3, mock(Function.class));

    // Act and Assert
    assertTrue(parsingCache.toggle());
    assertTrue(parsingCache.toggle);
  }

  /**
   * Method under test: {@link ParsingCache#valueCount()}
   */
  @Test
  public void testValueCount() throws IllegalArgumentException {
    // Arrange, Act and Assert
    assertEquals(0, (new ParsingCache<>(3, mock(Function.class))).valueCount());
  }

  /**
   * Method under test: {@link ParsingCache#ParsingCache(int, Function)}
   */
  @Test
  public void testConstructor2() throws IllegalArgumentException {
    // Arrange and Act
    ParsingCache<Object> actualParsingCache = new ParsingCache<>(128, mock(Function.class));

    // Assert
    assertEquals(128, actualParsingCache.interner.length);
    assertFalse(actualParsingCache.toggle);
    assertEquals(7, actualParsingCache.shift);
    assertEquals(Float.MAX_EXPONENT, actualParsingCache.mask);
  }

  /**
   * Method under test: {@link ParsingCache#ParsingCache(int, Function)}
   */
  @Test
  public void testConstructor3() throws IllegalArgumentException {
    // Arrange, Act and Assert
    assertThrows(OutOfMemoryError.class, () -> new ParsingCache<>(1073741824, mock(Function.class)));

  }
}

