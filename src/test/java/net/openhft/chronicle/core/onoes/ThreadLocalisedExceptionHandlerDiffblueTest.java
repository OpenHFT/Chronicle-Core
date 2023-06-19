package net.openhft.chronicle.core.onoes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.LinkedList;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

public class ThreadLocalisedExceptionHandlerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ThreadLocalisedExceptionHandler#ThreadLocalisedExceptionHandler(ExceptionHandler)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    ExceptionHandler handler = mock(ExceptionHandler.class);

    // Act
    ThreadLocalisedExceptionHandler actualThreadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        handler);

    // Assert
    assertSame(handler, actualThreadLocalisedExceptionHandler.defaultHandler());
    assertNull(actualThreadLocalisedExceptionHandler.threadLocalHandler());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#on(Class, String, Throwable)}
   */
  @Test
  public void testOn() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));
    threadLocalisedExceptionHandler.defaultHandler(null);
    Class<Object> clazz = Object.class;

    // Act
    threadLocalisedExceptionHandler.on(clazz, "foo", new Throwable());

    // Assert that nothing has changed
    assertNull(threadLocalisedExceptionHandler.threadLocalHandler());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#on(Class, String, Throwable)}
   */
  @Test
  public void testOn2() {
    // Arrange
    ExceptionHandler handler = mock(ExceptionHandler.class);
    doNothing().when(handler).on(Mockito.<Class<Object>>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(handler);
    Class<Object> clazz = Object.class;

    // Act
    threadLocalisedExceptionHandler.on(clazz, "An error occurred", new Throwable());

    // Assert
    verify(handler).on(Mockito.<Class<Object>>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#on(Class, String, Throwable)}
   */
  @Test
  public void testOn3() {
    // Arrange
    ExceptionHandler handler = mock(ExceptionHandler.class);
    doNothing().when(handler).on(Mockito.<Class<Object>>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    ExceptionHandler handler2 = mock(ExceptionHandler.class);
    doNothing().when(handler2).on(Mockito.<Class<Object>>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(handler);
    threadLocalisedExceptionHandler.threadLocalHandler(handler2);
    Class<Object> clazz = Object.class;

    // Act
    threadLocalisedExceptionHandler.on(clazz, "An error occurred", new Throwable());

    // Assert
    verify(handler2).on(Mockito.<Class<Object>>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn4() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));
    threadLocalisedExceptionHandler.defaultHandler(null);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    // Act
    threadLocalisedExceptionHandler.on(logger, "foo", new Throwable());

    // Assert that nothing has changed
    assertTrue(logger.isDelegateNull());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn5() {
    // Arrange
    ExceptionHandler handler = mock(ExceptionHandler.class);
    doNothing().when(handler).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(handler);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    // Act
    threadLocalisedExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(handler).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn6() {
    // Arrange
    ExceptionHandler handler = mock(ExceptionHandler.class);
    doNothing().when(handler).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    ExceptionHandler handler2 = mock(ExceptionHandler.class);
    doNothing().when(handler2).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(handler);
    threadLocalisedExceptionHandler.threadLocalHandler(handler2);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    // Act
    threadLocalisedExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(handler2).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#defaultHandler()}
   */
  @Test
  public void testDefaultHandler() {
    // Arrange
    ExceptionHandler handler = mock(ExceptionHandler.class);
    doNothing().when(handler).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    ExceptionHandler actualDefaultHandlerResult = (new ThreadLocalisedExceptionHandler(handler)).defaultHandler();
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualDefaultHandlerResult.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(handler).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#defaultHandler(ExceptionHandler)}
   */
  @Test
  public void testDefaultHandler2() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));
    ExceptionHandler defaultHandler = mock(ExceptionHandler.class);

    // Act
    ThreadLocalisedExceptionHandler actualDefaultHandlerResult = threadLocalisedExceptionHandler
        .defaultHandler(defaultHandler);

    // Assert
    assertSame(threadLocalisedExceptionHandler, actualDefaultHandlerResult);
    assertSame(defaultHandler, actualDefaultHandlerResult.defaultHandler());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#defaultHandler(ExceptionHandler)}
   */
  @Test
  public void testDefaultHandler3() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));

    ThreadLocalisedExceptionHandler defaultHandler = new ThreadLocalisedExceptionHandler(mock(ExceptionHandler.class));
    defaultHandler.defaultHandler(null);

    // Act and Assert
    assertSame(threadLocalisedExceptionHandler, threadLocalisedExceptionHandler.defaultHandler(defaultHandler));
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#defaultHandler(ExceptionHandler)}
   */
  @Test
  public void testDefaultHandler4() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));

    // Act and Assert
    assertSame(threadLocalisedExceptionHandler, threadLocalisedExceptionHandler.defaultHandler(null));
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#defaultHandler(ExceptionHandler)}
   */
  @Test
  public void testDefaultHandler5() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));
    ChainedExceptionHandler defaultHandler = new ChainedExceptionHandler(mock(ExceptionHandler.class));

    // Act
    ThreadLocalisedExceptionHandler actualDefaultHandlerResult = threadLocalisedExceptionHandler
        .defaultHandler(defaultHandler);

    // Assert
    assertSame(threadLocalisedExceptionHandler, actualDefaultHandlerResult);
    assertSame(defaultHandler, actualDefaultHandlerResult.defaultHandler());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#unwrap(ExceptionHandler)}
   */
  @Test
  public void testUnwrap() {
    // Arrange
    ExceptionHandler eh = mock(ExceptionHandler.class);
    doNothing().when(eh).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    ExceptionHandler actualUnwrapResult = ThreadLocalisedExceptionHandler.unwrap(eh);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualUnwrapResult.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(eh).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#threadLocalHandler(ExceptionHandler)}
   */
  @Test
  public void testThreadLocalHandler() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));

    // Act and Assert
    assertSame(threadLocalisedExceptionHandler,
        threadLocalisedExceptionHandler.threadLocalHandler(mock(ExceptionHandler.class)));
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#resetThreadLocalHandler()}
   */
  @Test
  public void testResetThreadLocalHandler() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));

    // Act
    threadLocalisedExceptionHandler.resetThreadLocalHandler();

    // Assert
    assertNull(threadLocalisedExceptionHandler.threadLocalHandler());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#isEnabled(Class)}
   */
  @Test
  public void testIsEnabled() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));
    threadLocalisedExceptionHandler.defaultHandler(null);

    // Act and Assert
    assertFalse(threadLocalisedExceptionHandler.isEnabled(Object.class));
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#isEnabled(Class)}
   */
  @Test
  public void testIsEnabled2() {
    // Arrange
    ExceptionHandler handler = mock(ExceptionHandler.class);
    when(handler.isEnabled(Mockito.<Class<Object>>any())).thenReturn(true);
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(handler);

    // Act and Assert
    assertTrue(threadLocalisedExceptionHandler.isEnabled(Object.class));
    verify(handler).isEnabled(Mockito.<Class<Object>>any());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#isEnabled(Class)}
   */
  @Test
  public void testIsEnabled3() {
    // Arrange
    ExceptionHandler handler = mock(ExceptionHandler.class);
    when(handler.isEnabled(Mockito.<Class<Object>>any())).thenReturn(true);
    ExceptionHandler handler2 = mock(ExceptionHandler.class);
    when(handler2.isEnabled(Mockito.<Class<Object>>any())).thenReturn(true);

    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(handler);
    threadLocalisedExceptionHandler.threadLocalHandler(handler2);

    // Act and Assert
    assertTrue(threadLocalisedExceptionHandler.isEnabled(Object.class));
    verify(handler2).isEnabled(Mockito.<Class<Object>>any());
  }

  /**
   * Method under test: {@link ThreadLocalisedExceptionHandler#isEnabled(Class)}
   */
  @Test
  public void testIsEnabled4() {
    // Arrange
    ThreadLocalisedExceptionHandler threadLocalisedExceptionHandler = new ThreadLocalisedExceptionHandler(
        mock(ExceptionHandler.class));
    threadLocalisedExceptionHandler.defaultHandler(new GoogleExceptionHandler(mock(ExceptionHandler.class)));

    // Act and Assert
    assertTrue(threadLocalisedExceptionHandler.isEnabled(Object.class));
  }
}

