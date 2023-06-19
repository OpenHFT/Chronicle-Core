package net.openhft.chronicle.core.watcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class PlainFileClassifierDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link PlainFileClassifier#classify(String, String)}
  */
  @Test
  public void testClassify() {
    // Arrange, Act and Assert
    assertNull((new PlainFileClassifier()).classify("Base", "Relative"));
  }
}

