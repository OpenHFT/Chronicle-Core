package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertFalse;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class BackgroundResourceReleaserDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link BackgroundResourceReleaser#isOnBackgroundResourceReleaserThread()}
  */
  @Test
  public void testIsOnBackgroundResourceReleaserThread() {
    // Arrange, Act and Assert
    assertFalse(BackgroundResourceReleaser.isOnBackgroundResourceReleaserThread());
  }
}

