package net.openhft.chronicle.core.onoes;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import java.util.LinkedList;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

public class ExceptionHandlerDiffblueTest {
  /**
  * Method under test: {@link ExceptionHandler#ignoresEverything()}
  */
  @Test
  public void testIgnoresEverything() {
    // Arrange and Act
    ExceptionHandler actualIgnoresEverythingResult = ExceptionHandler.ignoresEverything();
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualIgnoresEverythingResult.on(logger, "An error occurred", new Throwable());

    // Assert that nothing has changed
    assertTrue(logger.isDelegateNull());
  }

  /**
   * Method under test: {@link ExceptionHandler#on(Class, String)}
   */
  @Test
  public void testOn() {
    // Arrange
    ChainedExceptionHandler fallBack = mock(ChainedExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    GoogleExceptionHandler googleExceptionHandler = new GoogleExceptionHandler(
        new StackoverflowExceptionHandler(fallBack));

    // Act
    googleExceptionHandler.on(Object.class, "An error occurred");

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ExceptionHandler#on(Class, String, Throwable)}
   */
  @Test
  public void testOn2() {
    // Arrange
    ChainedExceptionHandler fallBack = mock(ChainedExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    GoogleExceptionHandler googleExceptionHandler = new GoogleExceptionHandler(
        new StackoverflowExceptionHandler(fallBack));
    Class<Object> clazz = Object.class;

    // Act
    googleExceptionHandler.on(clazz, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ExceptionHandler#on(Class, Throwable)}
   */
  @Test
  public void testOn3() {
    // Arrange
    ChainedExceptionHandler fallBack = mock(ChainedExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    GoogleExceptionHandler googleExceptionHandler = new GoogleExceptionHandler(
        new StackoverflowExceptionHandler(fallBack));
    Class<Object> clazz = Object.class;

    // Act
    googleExceptionHandler.on(clazz, new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ExceptionHandler#on(Logger, String)}
   */
  @Test
  public void testOn4() {
    // Arrange
    ChainedExceptionHandler fallBack = mock(ChainedExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    GoogleExceptionHandler googleExceptionHandler = new GoogleExceptionHandler(
        new StackoverflowExceptionHandler(fallBack));

    // Act
    googleExceptionHandler.on(new SubstituteLogger("Name", new LinkedList<>(), true), "An error occurred");

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ExceptionHandler#isEnabled(Class)}
   */
  @Test
  public void testIsEnabled() {
    // Arrange
    GoogleExceptionHandler googleExceptionHandler = new GoogleExceptionHandler(
        new StackoverflowExceptionHandler(new RecordingExceptionHandler(LogLevel.ERROR, new HashMap<>(), true)));

    // Act and Assert
    assertTrue(googleExceptionHandler.isEnabled(Object.class));
  }

  /**
   * Method under test: {@link ExceptionHandler#defaultHandler()}
   */
  @Test
  public void testDefaultHandler() {
    // Arrange and Act
    ExceptionHandler actualDefaultHandlerResult = (new GoogleExceptionHandler(
        new StackoverflowExceptionHandler(new RecordingExceptionHandler(LogLevel.ERROR, new HashMap<>(), true))))
            .defaultHandler();
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualDefaultHandlerResult.on(logger, "An error occurred", new Throwable());

    // Assert
    assertTrue(actualDefaultHandlerResult instanceof GoogleExceptionHandler);
  }
}

