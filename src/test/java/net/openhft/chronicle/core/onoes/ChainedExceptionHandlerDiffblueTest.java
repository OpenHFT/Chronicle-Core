package net.openhft.chronicle.core.onoes;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.LinkedList;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

public class ChainedExceptionHandlerDiffblueTest {
  /**
  * Method under test: {@link ChainedExceptionHandler#ChainedExceptionHandler(ExceptionHandler[])}
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertEquals(1, (new ChainedExceptionHandler(mock(ExceptionHandler.class))).chain().length);
    assertEquals(0, (new ChainedExceptionHandler()).chain().length);
    assertEquals(2,
        (new ChainedExceptionHandler(mock(ExceptionHandler.class), mock(ExceptionHandler.class))).chain().length);
  }

  /**
   * Method under test: {@link ChainedExceptionHandler#on(Class, String, Throwable)}
   */
  @Test
  public void testOn() {
    // Arrange
    ExceptionHandler exceptionHandler = mock(ExceptionHandler.class);
    doNothing().when(exceptionHandler)
        .on(Mockito.<Class<Object>>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    ChainedExceptionHandler chainedExceptionHandler = new ChainedExceptionHandler(exceptionHandler);
    Class<Object> clazz = Object.class;

    // Act
    chainedExceptionHandler.on(clazz, "An error occurred", new Throwable());

    // Assert
    verify(exceptionHandler).on(Mockito.<Class<Object>>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ChainedExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn2() {
    // Arrange
    ExceptionHandler exceptionHandler = mock(ExceptionHandler.class);
    doNothing().when(exceptionHandler).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    ChainedExceptionHandler chainedExceptionHandler = new ChainedExceptionHandler(exceptionHandler);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    // Act
    chainedExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(exceptionHandler).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ChainedExceptionHandler#chain()}
   */
  @Test
  public void testChain() {
    // Arrange, Act and Assert
    assertEquals(1, (new ChainedExceptionHandler(mock(ExceptionHandler.class))).chain().length);
  }
}

