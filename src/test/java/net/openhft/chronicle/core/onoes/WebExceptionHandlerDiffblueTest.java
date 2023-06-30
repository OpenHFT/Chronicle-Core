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

public class WebExceptionHandlerDiffblueTest {
  /**
  * Method under test: {@link WebExceptionHandler#WebExceptionHandler(String, ExceptionHandler)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    WebExceptionHandler actualWebExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualWebExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#WebExceptionHandler(String, ExceptionHandler)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    WebExceptionHandler actualWebExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("1.5", new LinkedList<>(), true);

    actualWebExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#WebExceptionHandler(String, ExceptionHandler)}
   */
  @Test
  public void testConstructor3() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    WebExceptionHandler actualWebExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("\\.", new LinkedList<>(), true);

    actualWebExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#WebExceptionHandler(String, ExceptionHandler)}
   */
  @Test
  public void testConstructor4() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    WebExceptionHandler actualWebExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("-_.!~*'()\"", new LinkedList<>(), true);

    actualWebExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#WebExceptionHandler(String, ExceptionHandler)}
   */
  @Test
  public void testConstructor5() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    WebExceptionHandler actualWebExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualWebExceptionHandler.on(logger, null, new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#WebExceptionHandler(String, ExceptionHandler)}
   */
  @Test
  public void testConstructor6() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    WebExceptionHandler actualWebExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    actualWebExceptionHandler.on(new SubstituteLogger("Name", new LinkedList<>(), true), "An error occurred", null);

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#WebExceptionHandler(String, ExceptionHandler)}
   */
  @Test
  public void testConstructor7() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());

    // Act
    WebExceptionHandler actualWebExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    actualWebExceptionHandler.on(logger, "An error occurred", new IOException("java.version", new Throwable()));

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    WebExceptionHandler webExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    // Act
    webExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn2() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    WebExceptionHandler webExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("1.5", new LinkedList<>(), true);

    // Act
    webExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn3() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    WebExceptionHandler webExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("\\.", new LinkedList<>(), true);

    // Act
    webExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn4() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    WebExceptionHandler webExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("-_.!~*'()\"", new LinkedList<>(), true);

    // Act
    webExceptionHandler.on(logger, "An error occurred", new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn5() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    WebExceptionHandler webExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    // Act
    webExceptionHandler.on(logger, null, new Throwable());

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn6() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    WebExceptionHandler webExceptionHandler = new WebExceptionHandler("Properties File", fallBack);

    // Act
    webExceptionHandler.on(new SubstituteLogger("Name", new LinkedList<>(), true), "An error occurred", null);

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }

  /**
   * Method under test: {@link WebExceptionHandler#on(Logger, String, Throwable)}
   */
  @Test
  public void testOn7() {
    // Arrange
    ExceptionHandler fallBack = mock(ExceptionHandler.class);
    doNothing().when(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
    WebExceptionHandler webExceptionHandler = new WebExceptionHandler("Properties File", fallBack);
    SubstituteLogger logger = new SubstituteLogger("Name", new LinkedList<>(), true);

    // Act
    webExceptionHandler.on(logger, "An error occurred", new IOException("java.version", new Throwable()));

    // Assert
    verify(fallBack).on(Mockito.<Logger>any(), Mockito.<String>any(), Mockito.<Throwable>any());
  }
}

