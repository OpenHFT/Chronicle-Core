package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import javax.management.loading.MLet;
import org.junit.Test;

public class IOToolsDiffblueTest {
  /**
   * Method under test: {@link IOTools#isClosedException(Exception)}
   */
  @Test
  public void testIsClosedException() {
    // Arrange, Act and Assert
    assertFalse(IOTools.isClosedException(new Exception("foo")));
    assertFalse(IOTools.isClosedException(new IOException("foo")));
  }

  /**
   * Method under test: {@link IOTools#shallowDeleteDirWithFiles(File)}
   */
  @Test
  public void testShallowDeleteDirWithFiles() throws IORuntimeException {
    // Arrange, Act and Assert
    assertFalse(
        IOTools.shallowDeleteDirWithFiles(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
    assertFalse(IOTools.shallowDeleteDirWithFiles("/directory"));
  }

  /**
  * Method under test: {@link IOTools#deleteDirWithFiles(File)}
  */
  @Test
  public void testDeleteDirWithFiles() throws IORuntimeException {
    // Arrange, Act and Assert
    assertFalse(IOTools.deleteDirWithFiles(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
    assertFalse(IOTools.deleteDirWithFiles(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile(), 2));
    assertFalse(IOTools.deleteDirWithFiles("Dir", 2));
    assertFalse(IOTools.deleteDirWithFiles("Dirs"));
    assertTrue(IOTools.deleteDirWithFiles());
  }

  /**
   * Method under test: {@link IOTools#urlFor(Class, String)}
   */
  @Test
  public void testUrlFor() throws FileNotFoundException {
    // Arrange, Act and Assert
    assertThrows(FileNotFoundException.class, () -> IOTools.urlFor(Closeable.class, "https://example.org/example"));
    assertThrows(FileNotFoundException.class, () -> IOTools.urlFor(new MLet(), "https://example.org/example"));
  }

  /**
   * Method under test: {@link IOTools#readFile(Class, String)}
   */
  @Test
  public void testReadFile() throws IOException {
    // Arrange, Act and Assert
    assertThrows(FileNotFoundException.class, () -> IOTools.readFile(Closeable.class, "Name"));
    assertEquals(0, IOTools.readFile(Closeable.class, "42").length);
  }

  /**
   * Method under test: {@link IOTools#readAsBytes(InputStream)}
   */
  @Test
  public void testReadAsBytes() throws IOException {
    // Arrange and Act
    byte[] actualReadAsBytesResult = IOTools.readAsBytes(new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8")));

    // Assert
    assertEquals(8, actualReadAsBytesResult.length);
    assertEquals('A', actualReadAsBytesResult[0]);
    assertEquals('X', actualReadAsBytesResult[1]);
    assertEquals('A', actualReadAsBytesResult[2]);
    assertEquals('X', actualReadAsBytesResult[3]);
    assertEquals('A', actualReadAsBytesResult[4]);
    assertEquals('X', actualReadAsBytesResult[5]);
    assertEquals('A', actualReadAsBytesResult[6]);
    assertEquals('X', actualReadAsBytesResult[7]);
  }

  /**
   * Method under test: {@link IOTools#readAsBytes(InputStream)}
   */
  @Test
  public void testReadAsBytes2() throws IOException {
    // Arrange, Act and Assert
    assertEquals(0, IOTools.readAsBytes(new FileInputStream("foo")).length);
  }

  /**
   * Method under test: {@link IOTools#normaliseIOStatus(int)}
   */
  @Test
  public void testNormaliseIOStatus() {
    // Arrange, Act and Assert
    assertEquals(1, IOTools.normaliseIOStatus(1));
  }
}

