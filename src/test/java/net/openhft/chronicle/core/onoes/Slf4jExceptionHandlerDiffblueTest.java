package net.openhft.chronicle.core.onoes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.impl.SimpleLogger;

public class Slf4jExceptionHandlerDiffblueTest {
  /**
  * Method under test: {@link Slf4jExceptionHandler#getLogger(Class)}
  */
  @Test
  public void testGetLogger() {
    // Arrange, Act and Assert
    assertTrue(Slf4jExceptionHandler.getLogger(Object.class) instanceof SimpleLogger);
  }

  /**
   * Method under test: {@link Slf4jExceptionHandler#valueOf(LogLevel)}
   */
  @Test
  public void testValueOf() {
    // Arrange, Act and Assert
    assertEquals(Slf4jExceptionHandler.ERROR, Slf4jExceptionHandler.valueOf(LogLevel.ERROR));
    assertEquals(Slf4jExceptionHandler.WARN, Slf4jExceptionHandler.valueOf(LogLevel.WARN));
    assertEquals(Slf4jExceptionHandler.PERF, Slf4jExceptionHandler.valueOf(LogLevel.PERF));
    assertEquals(Slf4jExceptionHandler.DEBUG, Slf4jExceptionHandler.valueOf(LogLevel.DEBUG));
  }
}

