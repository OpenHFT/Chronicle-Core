package net.openhft.chronicle.core.watcher;

import static org.junit.Assert.assertEquals;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class FileSystemWatcherDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link FileSystemWatcher.PathInfo#PathInfo(String, String)}
  */
  @Test
  public void testPathInfoConstructor() {
    // Arrange and Act
    FileSystemWatcher.PathInfo actualPathInfo = new FileSystemWatcher.PathInfo("foo", "foo");

    // Assert
    assertEquals("foo", actualPathInfo.basePath);
    assertEquals("", actualPathInfo.relativePath);
    assertEquals("foo", actualPathInfo.full);
  }

  /**
   * Method under test: {@link FileSystemWatcher.PathInfo#PathInfo(String, String)}
   */
  @Test
  public void testPathInfoConstructor2() {
    // Arrange and Act
    FileSystemWatcher.PathInfo actualPathInfo = new FileSystemWatcher.PathInfo("42", "Full");

    // Assert
    assertEquals("42", actualPathInfo.basePath);
    assertEquals("l", actualPathInfo.relativePath);
    assertEquals("Full", actualPathInfo.full);
  }
}

