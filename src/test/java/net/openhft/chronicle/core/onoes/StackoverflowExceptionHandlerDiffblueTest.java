package net.openhft.chronicle.core.onoes;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.util.LinkedList;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

public class StackoverflowExceptionHandlerDiffblueTest {
  /**
  * Method under test: {@link StackoverflowExceptionHandler#StackoverflowExceptionHandler(ExceptionHandler)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    StackoverflowExceptionHandler actualStackoverflowExceptionHandler = new StackoverflowExceptionHandler(fallBack);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualStackoverflowExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link StackoverflowExceptionHandler#StackoverflowExceptionHandler(ExceptionHandler)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    StackoverflowExceptionHandler actualStackoverflowExceptionHandler = new StackoverflowExceptionHandler(fallBack);
    SubstituteLogger logger = new SubstituteLogger("1.5", new LinkedList<>(), true);

    actualStackoverflowExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link StackoverflowExceptionHandler#StackoverflowExceptionHandler(ExceptionHandler)}
   */
  @Test
  public void testConstructor3() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    StackoverflowExceptionHandler actualStackoverflowExceptionHandler = new StackoverflowExceptionHandler(fallBack);
    SubstituteLogger logger = new SubstituteLogger("\\.", new LinkedList<>(), true);

    actualStackoverflowExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link StackoverflowExceptionHandler#StackoverflowExceptionHandler(ExceptionHandler)}
   */
  @Test
  public void testConstructor4() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    StackoverflowExceptionHandler actualStackoverflowExceptionHandler = new StackoverflowExceptionHandler(fallBack);
    SubstituteLogger logger = new SubstituteLogger("-_.!~*'()\"", new LinkedList<>(), true);

    actualStackoverflowExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link StackoverflowExceptionHandler#StackoverflowExceptionHandler(ExceptionHandler)}
   */
  @Test
  public void testConstructor5() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    StackoverflowExceptionHandler actualStackoverflowExceptionHandler = new StackoverflowExceptionHandler(fallBack);
    actualStackoverflowExceptionHandler.on(new SubstituteLogger("Name", new LinkedList<>(), true), "An error occurred",
        null);

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link StackoverflowExceptionHandler#StackoverflowExceptionHandler(ExceptionHandler)}
   */
  @Test
  public void testConstructor6() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    StackoverflowExceptionHandler actualStackoverflowExceptionHandler = new StackoverflowExceptionHandler(fallBack);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualStackoverflowExceptionHandler.on(logger, "An error occurred",
        new IOException("java.version", new Throwable()));

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }
}

