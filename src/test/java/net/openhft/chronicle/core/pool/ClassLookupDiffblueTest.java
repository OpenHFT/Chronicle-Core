package net.openhft.chronicle.core.pool;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Paths;
import javax.management.loading.MLet;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.mockito.Mockito;

public class ClassLookupDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link ClassLookup#wrap()}
  */
  @Test
  public void testWrap() {
    // Arrange
    ClassAliasPool parent = new ClassAliasPool(null);

    // Act and Assert
    assertTrue((new ClassAliasPool(parent, new MLet())).wrap() instanceof ClassAliasPool);
  }

  /**
   * Method under test: {@link ClassLookup#wrap()}
   */
  @Test
  public void testWrap2() throws MalformedURLException {
    // Arrange
    URLStreamHandlerFactory urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
    when(urlStreamHandlerFactory.createURLStreamHandler(Mockito.<String>any())).thenReturn(null);
    URLClassLoader classLoader = new URLClassLoader(
        new URL[]{Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toUri().toURL()}, new MLet(),
        urlStreamHandlerFactory);

    // Act and Assert
    assertTrue((new ClassAliasPool(new ClassAliasPool(null), classLoader)).wrap() instanceof ClassAliasPool);
    verify(urlStreamHandlerFactory).createURLStreamHandler(Mockito.<String>any());
  }

  /**
   * Method under test: {@link ClassLookup#wrap(ClassLoader)}
   */
  @Test
  public void testWrap3() {
    // Arrange
    ClassAliasPool parent = new ClassAliasPool(null);
    ClassAliasPool classAliasPool = new ClassAliasPool(parent, new MLet());

    // Act and Assert
    assertTrue(classAliasPool.wrap(new MLet()) instanceof ClassAliasPool);
  }

  /**
   * Method under test: {@link ClassLookup#wrap(ClassLoader)}
   */
  @Test
  public void testWrap4() throws MalformedURLException {
    // Arrange
    URLStreamHandlerFactory urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
    when(urlStreamHandlerFactory.createURLStreamHandler(Mockito.<String>any())).thenReturn(null);
    URLClassLoader classLoader = new URLClassLoader(
        new URL[]{Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toUri().toURL()}, new MLet(),
        urlStreamHandlerFactory);

    ClassAliasPool classAliasPool = new ClassAliasPool(new ClassAliasPool(null), classLoader);

    // Act and Assert
    assertTrue(classAliasPool.wrap(new MLet()) instanceof ClassAliasPool);
    verify(urlStreamHandlerFactory).createURLStreamHandler(Mockito.<String>any());
  }
}

