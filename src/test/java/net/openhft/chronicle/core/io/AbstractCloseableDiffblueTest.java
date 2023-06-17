package net.openhft.chronicle.core.io;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class AbstractCloseableDiffblueTest {
  /**
   * Method under test: {@link AbstractCloseable.Finalizer#finalize()}
   */
  @Test
  public void testFinalizerFinalize() throws Throwable {
    // Arrange
    AbstractCloseable abstractCloseable = mock(AbstractCloseable.class);
    doNothing().when(abstractCloseable).warnAndCloseIfNotClosed();

    // Act
    (abstractCloseable.new Finalizer()).finalize();

    // Assert
    verify(abstractCloseable).warnAndCloseIfNotClosed();
  }

  /**
  * Method under test: {@link AbstractCloseable#waitForCloseablesToClose(long)}
  */
  @Test
  public void testWaitForCloseablesToClose() {
    // Arrange, Act and Assert
    assertTrue(AbstractCloseable.waitForCloseablesToClose(1L));
  }
}

