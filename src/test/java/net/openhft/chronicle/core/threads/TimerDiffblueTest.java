package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.util.function.Supplier;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.time.TimeProvider;
import org.junit.Test;

public class TimerDiffblueTest extends CoreTestCommon {
  /**
  * Method under test: {@link Timer#Timer(EventLoop)}
  */
  @Test
  public void testConstructor() {
    // Arrange
    DelegatingEventLoop eventLoop = new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)));

    // Act
    new Timer(eventLoop);

    // Assert
    assertFalse(eventLoop.isAlive());
    assertFalse(eventLoop.isStopped());
    assertTrue(eventLoop.isClosing());
    assertTrue(eventLoop.isClosed());
  }

  /**
   * Method under test: {@link Timer#Timer(EventLoop, TimeProvider)}
   */
  @Test
  public void testConstructor2() {
    // Arrange
    DelegatingEventLoop eventLoop = new DelegatingEventLoop(new OnDemandEventLoop(mock(Supplier.class)));

    // Act
    new Timer(eventLoop, mock(TimeProvider.class));

    // Assert
    assertFalse(eventLoop.isAlive());
    assertFalse(eventLoop.isStopped());
    assertTrue(eventLoop.isClosing());
    assertTrue(eventLoop.isClosed());
  }
}

