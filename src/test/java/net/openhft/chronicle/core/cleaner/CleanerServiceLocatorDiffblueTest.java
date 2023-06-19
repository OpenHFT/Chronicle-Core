package net.openhft.chronicle.core.cleaner;

import static org.junit.Assert.assertTrue;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import org.junit.Test;

public class CleanerServiceLocatorDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link CleanerServiceLocator#cleanerService()}
  */
  @Test
  public void testCleanerService() {
    // Arrange, Act and Assert
    assertTrue(CleanerServiceLocator.cleanerService() instanceof ReflectionBasedByteBufferCleanerService);
  }
}

