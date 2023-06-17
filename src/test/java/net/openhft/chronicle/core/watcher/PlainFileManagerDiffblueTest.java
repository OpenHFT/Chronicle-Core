package net.openhft.chronicle.core.watcher;

import static org.junit.Assert.assertEquals;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class PlainFileManagerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link PlainFileManager#PlainFileManager(String, String, Path)}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    PlainFileManager actualPlainFileManager = new PlainFileManager("Base", "Relative",
        Paths.get(System.getProperty("java.io.tmpdir"), "test.txt"));

    // Assert
    assertEquals("Base", actualPlainFileManager.getBasePath());
    assertEquals("Relative", actualPlainFileManager.getRelativePath());
    assertEquals("text/plain", actualPlainFileManager.getContentType());
  }

  /**
   * Method under test: {@link PlainFileManager#type()}
   */
  @Test
  public void testType() {
    // Arrange, Act and Assert
    assertEquals("files",
        (new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt"))).type());
  }

  /**
   * Method under test: {@link PlainFileManager#getFileSize()}
   */
  @Test
  public void testGetFileSize2() {
    // Arrange and Act
    String actualFileSize = (new PlainFileManager("Base", "Relative",
        Paths.get(System.getProperty("java.io.tmpdir"), " B"))).getFileSize();

    // Assert
    assertEquals(String.join("", "java.nio.file.NoSuchFileException: ", System.getProperty("java.io.tmpdir"), " B"),
        actualFileSize);
  }

  /**
   * Method under test: {@link PlainFileManager#getContentType()}
   */
  @Test
  public void testGetContentType() {
    // Arrange, Act and Assert
    assertEquals("text/plain",
        (new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")))
            .getContentType());
  }
}

