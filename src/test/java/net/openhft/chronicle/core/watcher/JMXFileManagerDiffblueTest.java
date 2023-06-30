package net.openhft.chronicle.core.watcher;

import static org.junit.Assert.assertEquals;
import java.nio.file.Paths;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class JMXFileManagerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link JMXFileManager#type()}
  */
  @Test
  public void testType() {
    // Arrange, Act and Assert
    assertEquals("files",
        (new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt"))).type());
  }

  /**
   * Method under test: {@link JMXFileManager#jmxPath()}
   */
  @Test
  public void testJmxPath() {
    // Arrange, Act and Assert
    assertEquals("chronicle",
        (new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")))
            .jmxPath());
  }

  /**
   * Method under test: {@link JMXFileManager#getBasePath()}
   */
  @Test
  public void testGetBasePath() {
    // Arrange, Act and Assert
    assertEquals("Base",
        (new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")))
            .getBasePath());
  }

  /**
   * Method under test: {@link JMXFileManager#getRelativePath()}
   */
  @Test
  public void testGetRelativePath() {
    // Arrange, Act and Assert
    assertEquals("Relative",
        (new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")))
            .getRelativePath());
  }
}

