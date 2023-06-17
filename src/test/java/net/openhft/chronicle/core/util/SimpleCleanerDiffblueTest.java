package net.openhft.chronicle.core.util;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class SimpleCleanerDiffblueTest {
  /**
  * Method under test: {@link SimpleCleaner#clean()}
  */
  @Test
  public void testClean() {
    // Arrange
    Runnable thunk = mock(Runnable.class);
    doNothing().when(thunk).run();

    // Act
    (new SimpleCleaner(thunk)).clean();

    // Assert
    verify(thunk).run();
  }
}

