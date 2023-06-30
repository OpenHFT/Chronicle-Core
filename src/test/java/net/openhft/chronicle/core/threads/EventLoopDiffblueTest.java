package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.util.function.Supplier;
import org.junit.Test;

public class EventLoopDiffblueTest {
  /**
  * Method under test: {@link EventLoop#inEventLoop()}
  */
  @Test
  public void testInEventLoop() {
    // Arrange, Act and Assert
    assertFalse(EventLoop.inEventLoop());
  }

  /**
   * Method under test: {@link EventLoop#runsInsideCoreLoop()}
   */
  @Test
  public void testRunsInsideCoreLoop() {
    // Arrange, Act and Assert
    assertTrue((new OnDemandEventLoop(mock(Supplier.class))).runsInsideCoreLoop());
  }
}

