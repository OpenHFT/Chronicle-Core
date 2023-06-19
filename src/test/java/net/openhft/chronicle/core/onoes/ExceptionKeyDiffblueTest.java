package net.openhft.chronicle.core.onoes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class ExceptionKeyDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ExceptionKey#ExceptionKey(LogLevel, Class, String, Throwable)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    Class<Object> clazz = Object.class;

    // Act and Assert
    Throwable throwable = (new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable())).throwable;
    assertNull(throwable.getMessage());
    assertNull(throwable.getCause());
    assertNull(throwable.getLocalizedMessage());
  }

  /**
   * Method under test: {@link ExceptionKey#level()}
   */
  @Test
  public void testLevel() {
    // Arrange
    Class<Object> clazz = Object.class;

    // Act and Assert
    assertEquals(LogLevel.ERROR,
        (new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable())).level());
  }

  /**
   * Method under test: {@link ExceptionKey#clazz()}
   */
  @Test
  public void testClazz() {
    // Arrange
    Class<Object> clazz = Object.class;

    // Act
    Class<?> actualClazzResult = (new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable()))
        .clazz();

    // Assert
    assertSame(Object.class, actualClazzResult);
  }

  /**
   * Method under test: {@link ExceptionKey#message()}
   */
  @Test
  public void testMessage() {
    // Arrange
    Class<Object> clazz = Object.class;

    // Act and Assert
    assertEquals("An error occurred",
        (new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable())).message());
  }

  /**
   * Method under test: {@link ExceptionKey#throwable()}
   */
  @Test
  public void testThrowable() {
    // Arrange
    Class<Object> clazz = Object.class;
    ExceptionKey exceptionKey = new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable());

    // Act and Assert
    assertSame(exceptionKey.throwable, exceptionKey.throwable());
  }

  /**
   * Method under test: {@link ExceptionKey#equals(Object)}
   */
  @Test
  public void testEquals() {
    // Arrange
    Class<Object> clazz = Object.class;

    // Act and Assert
    assertNotEquals(new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable()), null);
  }

  /**
   * Method under test: {@link ExceptionKey#equals(Object)}
   */
  @Test
  public void testEquals2() {
    // Arrange
    Class<Object> clazz = Object.class;

    // Act and Assert
    assertNotEquals(new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable()),
        "Different type to ExceptionKey");
  }

  /**
   * Methods under test: 
   * 
   * <ul>
   *   <li>{@link ExceptionKey#equals(Object)}
   *   <li>{@link ExceptionKey#hashCode()}
   * </ul>
   */
  @Test
  public void testEquals3() {
    // Arrange
    Class<Object> clazz = Object.class;
    ExceptionKey exceptionKey = new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable());

    // Act and Assert
    assertEquals(exceptionKey, exceptionKey);
    int expectedHashCodeResult = exceptionKey.hashCode();
    assertEquals(expectedHashCodeResult, exceptionKey.hashCode());
  }

  /**
   * Method under test: {@link ExceptionKey#equals(Object)}
   */
  @Test
  public void testEquals4() {
    // Arrange
    Class<Object> clazz = Object.class;
    ExceptionKey exceptionKey = new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable());
    Class<Object> clazz2 = Object.class;

    // Act and Assert
    assertNotEquals(exceptionKey, new ExceptionKey(LogLevel.ERROR, clazz2, "An error occurred", new Throwable()));
  }

  /**
   * Method under test: {@link ExceptionKey#equals(Object)}
   */
  @Test
  public void testEquals5() {
    // Arrange
    Class<Object> clazz = Object.class;
    ExceptionKey exceptionKey = new ExceptionKey(null, clazz, "An error occurred", new Throwable());
    Class<Object> clazz2 = Object.class;

    // Act and Assert
    assertNotEquals(exceptionKey, new ExceptionKey(LogLevel.ERROR, clazz2, "An error occurred", new Throwable()));
  }

  /**
   * Method under test: {@link ExceptionKey#equals(Object)}
   */
  @Test
  public void testEquals6() {
    // Arrange
    Class<ExceptionKey> clazz = ExceptionKey.class;
    ExceptionKey exceptionKey = new ExceptionKey(LogLevel.ERROR, clazz, "An error occurred", new Throwable());
    Class<Object> clazz2 = Object.class;

    // Act and Assert
    assertNotEquals(exceptionKey, new ExceptionKey(LogLevel.ERROR, clazz2, "An error occurred", new Throwable()));
  }

  /**
   * Method under test: {@link ExceptionKey#equals(Object)}
   */
  @Test
  public void testEquals7() {
    // Arrange
    Class<Object> clazz = Object.class;
    ExceptionKey exceptionKey = new ExceptionKey(LogLevel.ERROR, clazz, "Not all who wander are lost", new Throwable());
    Class<Object> clazz2 = Object.class;

    // Act and Assert
    assertNotEquals(exceptionKey, new ExceptionKey(LogLevel.ERROR, clazz2, "An error occurred", new Throwable()));
  }
}

