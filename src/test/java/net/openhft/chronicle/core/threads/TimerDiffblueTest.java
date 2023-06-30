package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import java.util.function.Supplier;
import net.openhft.chronicle.core.time.TimeProvider;
import org.junit.Test;

public class TimerDiffblueTest {
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
  }
}

