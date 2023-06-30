package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class BackgroundResourceReleaserDiffblueTest {
  /**
  * Method under test: {@link BackgroundResourceReleaser#isOnBackgroundResourceReleaserThread()}
  */
  @Test
  public void testIsOnBackgroundResourceReleaserThread() {
    // Arrange, Act and Assert
    assertFalse(BackgroundResourceReleaser.isOnBackgroundResourceReleaserThread());
  }
}

