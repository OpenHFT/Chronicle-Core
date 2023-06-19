package net.openhft.chronicle.core.watcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.file.Paths;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.mockito.Mockito;

public class ClassifyingWatcherListenerDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link ClassifyingWatcherListener#onExists(String, String, Boolean)}
   */
  @Test
  public void testOnExists() {
    // Arrange
    FileClassifier fileClassifier = mock(FileClassifier.class);
    when(fileClassifier.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));

    ClassifyingWatcherListener classifyingWatcherListener = new ClassifyingWatcherListener();
    classifyingWatcherListener.addClassifier(fileClassifier);

    // Act
    classifyingWatcherListener.onExists("Base", "foo.txt", true);

    // Assert
    verify(fileClassifier).classify(Mockito.<String>any(), Mockito.<String>any());
  }

  /**
   * Method under test: {@link ClassifyingWatcherListener#onExists(String, String, Boolean)}
   */
  @Test
  public void testOnExists2() {
    // Arrange
    FileClassifier fileClassifier = mock(FileClassifier.class);
    when(fileClassifier.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier2 = mock(FileClassifier.class);
    when(fileClassifier2.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));

    ClassifyingWatcherListener classifyingWatcherListener = new ClassifyingWatcherListener();
    classifyingWatcherListener.addClassifier(fileClassifier2);
    classifyingWatcherListener.addClassifier(fileClassifier);

    // Act
    classifyingWatcherListener.onExists("Base", "foo.txt", true);

    // Assert
    verify(fileClassifier2).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier).classify(Mockito.<String>any(), Mockito.<String>any());
  }

  /**
   * Method under test: {@link ClassifyingWatcherListener#onExists(String, String, Boolean)}
   */
  @Test
  public void testOnExists3() {
    // Arrange
    FileClassifier fileClassifier = mock(FileClassifier.class);
    when(fileClassifier.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier2 = mock(FileClassifier.class);
    when(fileClassifier2.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier3 = mock(FileClassifier.class);
    when(fileClassifier3.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier4 = mock(FileClassifier.class);
    when(fileClassifier4.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier5 = mock(FileClassifier.class);
    when(fileClassifier5.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier6 = mock(FileClassifier.class);
    when(fileClassifier6.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier7 = mock(FileClassifier.class);
    when(fileClassifier7.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier8 = mock(FileClassifier.class);
    when(fileClassifier8.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier9 = mock(FileClassifier.class);
    when(fileClassifier9.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier10 = mock(FileClassifier.class);
    when(fileClassifier10.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier11 = mock(FileClassifier.class);
    when(fileClassifier11.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier12 = mock(FileClassifier.class);
    when(fileClassifier12.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier13 = mock(FileClassifier.class);
    when(fileClassifier13.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier14 = mock(FileClassifier.class);
    when(fileClassifier14.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier15 = mock(FileClassifier.class);
    when(fileClassifier15.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier16 = mock(FileClassifier.class);
    when(fileClassifier16.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier17 = mock(FileClassifier.class);
    when(fileClassifier17.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier18 = mock(FileClassifier.class);
    when(fileClassifier18.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier19 = mock(FileClassifier.class);
    when(fileClassifier19.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier20 = mock(FileClassifier.class);
    when(fileClassifier20.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier21 = mock(FileClassifier.class);
    when(fileClassifier21.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier22 = mock(FileClassifier.class);
    when(fileClassifier22.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier23 = mock(FileClassifier.class);
    when(fileClassifier23.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier24 = mock(FileClassifier.class);
    when(fileClassifier24.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier25 = mock(FileClassifier.class);
    when(fileClassifier25.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier26 = mock(FileClassifier.class);
    when(fileClassifier26.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier27 = mock(FileClassifier.class);
    when(fileClassifier27.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier28 = mock(FileClassifier.class);
    when(fileClassifier28.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier29 = mock(FileClassifier.class);
    when(fileClassifier29.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier30 = mock(FileClassifier.class);
    when(fileClassifier30.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier31 = mock(FileClassifier.class);
    when(fileClassifier31.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier32 = mock(FileClassifier.class);
    when(fileClassifier32.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier33 = mock(FileClassifier.class);
    when(fileClassifier33.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier34 = mock(FileClassifier.class);
    when(fileClassifier34.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(
        new PlainFileManager("Base", "Relative", Paths.get(System.getProperty("java.io.tmpdir"), "test.txt")));
    FileClassifier fileClassifier35 = mock(FileClassifier.class);
    when(fileClassifier35.classify(Mockito.<String>any(), Mockito.<String>any())).thenReturn(null);

    ClassifyingWatcherListener classifyingWatcherListener = new ClassifyingWatcherListener();
    classifyingWatcherListener.addClassifier(fileClassifier35);
    classifyingWatcherListener.addClassifier(fileClassifier34);
    classifyingWatcherListener.addClassifier(fileClassifier33);
    classifyingWatcherListener.addClassifier(fileClassifier32);
    classifyingWatcherListener.addClassifier(fileClassifier31);
    classifyingWatcherListener.addClassifier(fileClassifier30);
    classifyingWatcherListener.addClassifier(fileClassifier29);
    classifyingWatcherListener.addClassifier(fileClassifier28);
    classifyingWatcherListener.addClassifier(fileClassifier27);
    classifyingWatcherListener.addClassifier(fileClassifier26);
    classifyingWatcherListener.addClassifier(fileClassifier25);
    classifyingWatcherListener.addClassifier(fileClassifier24);
    classifyingWatcherListener.addClassifier(fileClassifier23);
    classifyingWatcherListener.addClassifier(fileClassifier22);
    classifyingWatcherListener.addClassifier(fileClassifier21);
    classifyingWatcherListener.addClassifier(fileClassifier20);
    classifyingWatcherListener.addClassifier(fileClassifier19);
    classifyingWatcherListener.addClassifier(fileClassifier18);
    classifyingWatcherListener.addClassifier(fileClassifier17);
    classifyingWatcherListener.addClassifier(fileClassifier16);
    classifyingWatcherListener.addClassifier(fileClassifier15);
    classifyingWatcherListener.addClassifier(fileClassifier14);
    classifyingWatcherListener.addClassifier(fileClassifier13);
    classifyingWatcherListener.addClassifier(fileClassifier12);
    classifyingWatcherListener.addClassifier(fileClassifier11);
    classifyingWatcherListener.addClassifier(fileClassifier10);
    classifyingWatcherListener.addClassifier(fileClassifier9);
    classifyingWatcherListener.addClassifier(fileClassifier8);
    classifyingWatcherListener.addClassifier(fileClassifier7);
    classifyingWatcherListener.addClassifier(fileClassifier6);
    classifyingWatcherListener.addClassifier(fileClassifier5);
    classifyingWatcherListener.addClassifier(fileClassifier4);
    classifyingWatcherListener.addClassifier(fileClassifier3);
    classifyingWatcherListener.addClassifier(fileClassifier2);
    classifyingWatcherListener.addClassifier(fileClassifier);
    Paths.get(System.getProperty("java.io.tmpdir"), "test.txt");

    // Act
    classifyingWatcherListener.onExists("Base", "foo.txt", true);

    // Assert
    verify(fileClassifier35).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier34).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier33).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier32).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier31).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier30).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier29).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier28).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier27).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier26).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier25).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier24).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier23).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier22).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier21).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier20).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier19).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier18).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier17).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier16).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier15).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier14).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier13).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier12).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier11).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier10).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier9).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier8).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier7).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier6).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier5).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier4).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier3).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier2).classify(Mockito.<String>any(), Mockito.<String>any());
    verify(fileClassifier).classify(Mockito.<String>any(), Mockito.<String>any());
  }

  /**
   * Method under test: {@link ClassifyingWatcherListener#addClassifier(FileClassifier)}
   */
  @Test
  public void testAddClassifier() {
    // Arrange
    ClassifyingWatcherListener classifyingWatcherListener = new ClassifyingWatcherListener();

    // Act
    classifyingWatcherListener.addClassifier(mock(FileClassifier.class));

    // Assert
    assertEquals(1, classifyingWatcherListener.classifiers.size());
  }

  /**
  * Method under test: default or parameterless constructor of {@link ClassifyingWatcherListener}
  */
  @Test
  public void testConstructor() {
    // Arrange and Act
    ClassifyingWatcherListener actualClassifyingWatcherListener = new ClassifyingWatcherListener();

    // Assert
    assertTrue(actualClassifyingWatcherListener.classifiers.isEmpty());
    assertTrue(actualClassifyingWatcherListener.fileManagerMap.isEmpty());
  }
}

