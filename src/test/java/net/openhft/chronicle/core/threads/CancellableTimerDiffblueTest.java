package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import java.util.function.Supplier;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

public class CancellableTimerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link CancellableTimer#CancellableTimer(EventLoop)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    DelegatingEventLoop eventLoop = new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)));

    // Act
    new CancellableTimer(eventLoop);

    // Assert
    assertFalse(eventLoop.isAlive());
    assertFalse(eventLoop.isStopped());
  }

  /**
   * Method under test: {@link CancellableTimer#CancellableTimer(EventLoop)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    DelegatingEventLoop eventLoop = new DelegatingEventLoop(
        new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class))));

    // Act
    new CancellableTimer(eventLoop);

    // Assert
    assertFalse(eventLoop.isAlive());
    assertFalse(eventLoop.isStopped());
  }
}

